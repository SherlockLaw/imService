package com.sherlock.imService.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.sherlock.imService.configure.ZookeeperConfigure;
import com.sherlock.imService.constant.MessageConstant;
import com.sherlock.imService.constant.MessageConstant.GTypeEnum;
import com.sherlock.imService.mq.MQProducer;
import com.sherlock.imService.netty.codec.ImEncoder;
import com.sherlock.imService.netty.codec.ImMessageCodec;
import com.sherlock.imService.netty.configure.Configure;
import com.sherlock.imService.netty.entity.ConversationOrderMessage;
import com.sherlock.imService.netty.entity.ACKMessage;
import com.sherlock.imService.netty.entity.RoutMessage;
import com.sherlock.imService.netty.entity.ImMessage;
import com.sherlock.imService.netty.entity.OrderMessage;
import com.sherlock.imService.netty.udp.UdpServerHandler;
import com.sherlock.imService.redis.RedisService;
import com.sherlock.imService.remote.IMMessageManager;
import com.sherlock.imService.service.FriendService;
import com.sherlock.imService.zookeeper.ZookeeperService;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;  

@Component
public class ImServer {
	
	private static final Logger logger = LoggerFactory.getLogger(ImServer.class);
	@Autowired
	private static MQProducer mqProducer;
	
	public static Channel channel;
	
    @Autowired
    public RedisService redisService;
    
    @Autowired
    private IMMessageManager iMMessageManager;
    
    @Autowired
    public ZookeeperService zookeeperService;
    
    @Autowired
    public FriendService friendService;
    
    @Autowired
    private ZookeeperConfigure zookeeperConfigure;
    
    public ImServer(@Autowired MQProducer mqProducer){
    	ImServer.mqProducer = mqProducer;
    }
    
    @PostConstruct
	public void init() {
		new Thread(){
			private ImServer instance;
			@Override
			public void run() {
				if (Configure.isTcp) {
					instance.runTCP();
				} else {
					instance.runUDP();
				}
			}
			public Thread init(ImServer instance){
				this.instance = instance;
				return this;
			}
		}.init(this).start();
	}
    
	/**
	 * TCP方式
	 */
    public void runTCP(){  
    	/***
         * NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器，
         * Netty提供了许多不同的EventLoopGroup的实现用来处理不同传输协议。 在这个例子中我们实现了一个服务端的应用，
         * 因此会有2个NioEventLoopGroup会被使用。 第一个经常被叫做‘boss’，用来接收进来的连接。
         * 第二个经常被叫做‘worker’，用来处理已经被接收的连接， 一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上。
         * 如何知道多少个线程已经被使用，如何映射到已经创建的Channels上都需要依赖于EventLoopGroup的实现，
         * 并且可以通过构造函数来配置他们的关系。
         */
        EventLoopGroup bossGroup=new NioEventLoopGroup(1);  
        //workerGroup作为worker，处理boss接收的连接的流量和将接收的连接注册进入这个worker  
        EventLoopGroup workerGroup=new NioEventLoopGroup();  
        try {
            //ServerBootstrap 是一个启动NIO服务的辅助启动类 你可以在这个服务中直接使用Channel
            ServerBootstrap b=new ServerBootstrap();
            ImServer imServer = this;
            //这一步是必须的，如果没有设置group将会报java.lang.IllegalStateException: group not set异常
            b.group(bossGroup, workerGroup)  
            //指定使用NioServerSocketChannel产生一个Channel用来接收连接  
            .channel(NioServerSocketChannel.class)  
            /***
             * 这里的事件处理类经常会被用来处理一个最近的已经接收的Channel。 ChannelInitializer是一个特殊的处理类，
             * 他的目的是帮助使用者配置一个新的Channel。
             * 也许你想通过增加一些处理类比如NettyServerHandler来配置一个新的Channel
             * 或者其对应的ChannelPipeline来实现你的网络程序。 当你的程序变的复杂时，可能你会增加更多的处理类到pipline上，
             * 然后提取这些匿名类到最顶层的类上。
             */
            .childHandler(new ChannelInitializer<SocketChannel>() {  
                public void initChannel(SocketChannel ch) throws Exception {  
                    //ChannelPipeline用于存放管理ChannelHandel  
                    //ChannelHandler用于处理请求响应的业务逻辑相关代码
                    ch.pipeline()
                    //设置服务端的心跳检测
                    .addLast(new IdleStateHandler(Configure.MAX_IDLETIME, 0, 0, TimeUnit.SECONDS))
                    //编解码器
                    .addLast(new ImMessageCodec())
                    //正常的业务处理
                    .addLast(new ImInboundHandler(imServer))
                    .addLast(new ImOutboundHandler());  
                };  
            })  
            //对Channel进行一些配置  
            //注意以下是socket的标准参数  
            //BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。  
            //Option是为了NioServerSocketChannel设置的，用来接收传入连接的  
            .option(ChannelOption.SO_BACKLOG, 128)  
            //是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。  
            //childOption是用来给父级ServerChannel之下的Channels设置参数的  
            .childOption(ChannelOption.SO_KEEPALIVE, true);  
            // Bind and start to accept incoming connections.  
            ChannelFuture f=b.bind(zookeeperConfigure.getSOCKET_PORT()).sync();  
            // Wait until the server socket is closed.  
            // In this example, this does not happen, but you can do that to gracefully  
            // shut down your server.  
            //sync()会同步等待连接操作结果，用户线程将在此wait()，直到连接操作完成之后，线程被notify(),用户代码继续执行  
            //closeFuture()当Channel关闭时返回一个ChannelFuture,用于链路检测 
            
            f.channel().closeFuture().sync();  
        } catch (InterruptedException e) {
        	logger.error("Socket服务无法启动",e);
			System.exit(-1);
		}finally{  
            //资源优雅释放  
            bossGroup.shutdownGracefully();  
            workerGroup.shutdownGracefully();  
        }  
    }
    /**
     * UDP方式
     */
    public void runUDP() {
    	EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST,true)
                    .handler(new UdpServerHandler(this));
            channel = bootstrap.bind(zookeeperConfigure.getSOCKET_PORT()).sync().channel();
            channel.closeFuture().await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
    
    public static boolean addMessageToQueue(RoutMessage msg) throws IllegalStateException{
    	mqProducer.addMessageToMQ(JSONObject.toJSONString(msg));
		return true;
    }
    
    /**
     * 通过本服务的连接发送
     * @param msg
     */
	private void sendOnLocal(RoutMessage msg) {
		ChannelHandlerContext ctx = ImInboundHandler.getContextByUserId(msg.getRoutId());
		if (ctx == null || ctx.isRemoved()) {
			offlineHandler(msg);
			return;
		}
		ctx.writeAndFlush(msg);
	}
    /**
     * 通过全局服务发送
     * @param msg
     */
    private void sendOnGlobal(RoutMessage msg){
    	if (Configure.isTcp) {
			ChannelHandlerContext ctx = ImInboundHandler.getContextByUserId(msg.getRoutId());
			if (ctx==null || ctx.isRemoved()) {
				//查询rout目标的连接所在机器
				String serviceInnerAddr = redisService.getServiceInnerAddrFromRoutTable(msg.getRoutId());
				if (isLocalHandler(serviceInnerAddr)) {
					offlineHandler(msg);
				} else {
					//非本服务，需要转发
					iMMessageManager.routMessage(serviceInnerAddr, JSONObject.toJSONString(msg));
				}
				return;
			}
			ChannelFuture cf = ctx.writeAndFlush(msg);
//			cf.addListener(new ChannelFutureListener() {
//				@Override
//				public void operationComplete(ChannelFuture future) throws Exception {
//					if(!future.isSuccess()){
//						//没有发送成功
//						offlineHandler(msg);
//					}
//				}
//			});
		} else{
			InetSocketAddress addr = redisService.getUserAddr(msg.getRoutId());
			if (addr==null) {
				offlineHandler(msg);
				return;
			}
			ImEncoder.writeByUDP(null, msg, addr);
		}
    }
    private boolean isLocalHandler(String serviceInnerAddr){
		//未登陆或者连接是本服务
		if (StringUtils.isBlank(serviceInnerAddr) ||
				serviceInnerAddr.equals(zookeeperService.getLocalInnerIpPort())) {
			return true;
		}
		return false;
	}
    public void sendMessage(String jsonMessage) {
    	//转换为对象
    	RoutMessage msg = MessageConstant.getServerCommonMessage(jsonMessage);
    	sendOnGlobal(msg);
    }
    
    public void routMessage(String jsonMessage) {
    	//转换为对象
    	RoutMessage msg = MessageConstant.getServerCommonMessage(jsonMessage);
    	sendOnLocal(msg);
    }
    
    private void offlineHandler(RoutMessage msg){
    	//好友信息存离线
		if (msg instanceof ImMessage) {
			ImMessage message = (ImMessage) msg;
			if (message.getGtype()==GTypeEnum.friend.getIndex()) {
				redisService.saveConversationOfflineMessage(message);
				//给发送方发送ACK消息
				ACKMessage ackMessage = new ACKMessage();
				ackMessage.setGid(message.getGid());
				ackMessage.setGtype(message.getGtype());
				ackMessage.setAckMid(message.getMid());
				ackMessage.setRoutId(message.getFromUserId());
				ackMessage.setTime(System.currentTimeMillis());
				addMessageToQueue(ackMessage);
			}
			redisService.addUnreadCount(message);
		} else if (msg instanceof ConversationOrderMessage) {//指令消息
			ConversationOrderMessage message = (ConversationOrderMessage) msg;
			redisService.saveConversationOfflineMessage(message);
		} else if (msg instanceof OrderMessage) {
			OrderMessage message = (OrderMessage) msg;
			redisService.saveOfflineOrderMessage(message);
		}
    }
}  
