package com.sherlock.imService.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.sherlock.imService.entity.vo.UserVO;
import com.sherlock.imService.netty.codec.ImEncoder;
import com.sherlock.imService.netty.configure.Configure;
import com.sherlock.imService.netty.entity.ClientACKMessage;
import com.sherlock.imService.netty.entity.ClientAuthMessage;
import com.sherlock.imService.netty.entity.ClientCommonMessage;
import com.sherlock.imService.netty.entity.ClientHeartMessage;
import com.sherlock.imService.netty.entity.ClientReadMessage;
import com.sherlock.imService.netty.entity.OfflineMessage;
import com.sherlock.imService.netty.entity.ServerACKMessage;
import com.sherlock.imService.netty.entity.ServerAuthBackMessage;
import com.sherlock.imService.netty.entity.ServerImMessage;
import com.sherlock.imService.netty.entity.ServerReadMessage;
import com.sherlock.imService.redis.RedisService;
import com.sherlock.imService.zookeeper.ZookeeperService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

public class ImInboundHandler extends ChannelInboundHandlerAdapter {
	private static Log logger = LogFactory.getLog(ImInboundHandler.class);
	
	private static ConcurrentHashMap<String, ChannelHandlerContext> userIdContextMap = new ConcurrentHashMap<>();
	
	private static AttributeKey<String> userIdAttr = AttributeKey.valueOf("userId"); 
	
	private RedisService redisService;
	private ZookeeperService zookeeperService;
	@Autowired
	private ImServer imServer;
	
	public ImInboundHandler(RedisService redisService,ZookeeperService zookeeperService){
		this.redisService = redisService;
		this.zookeeperService = zookeeperService;
	}
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
    	logger.info("收到来自客户端的消息:"+msg.getClass().getSimpleName()+JSONObject.toJSONString(msg));
    	
    	ClientCommonMessage message = (ClientCommonMessage) msg;
    	clientMessageHandler(redisService, zookeeperService, ctx, message,null);
    }
    
    public static void clientMessageHandler(RedisService redisService,ZookeeperService zookeeperService, ChannelHandlerContext ctx, ClientCommonMessage message,DatagramPacket packet){
    	//先校验token
    	if (StringUtils.isBlank(message.getToken())) {
    		logger.info("客户端token没有传输");
			ctx.disconnect();
			return;
		}
    	UserVO user = redisService.getUserByToken(message.getToken());
    	if (user==null) {
    		logger.info("客户端token无效,token"+message.getToken());
    		//TODO:给客户端发送无效的消息
    		
			ctx.disconnect();
			return;
		}
    	InetSocketAddress sender = null;
    	if (!Configure.isTcp) {
    		sender = packet.sender();
    		boolean offline = 
    		setClientInetSocketAddress(redisService, user.getId(), sender);
    		if (offline) {
				OfflineMessage offlineMessage = new OfflineMessage();
				ImEncoder.write(ctx, offlineMessage,sender);
			}
		}
    	//如果是认证信息
    	if (message instanceof ClientAuthMessage) {
    		if (Configure.isTcp) {
    			setUserId(ctx, user.getId()+"");
    			userIdContextMap.put(user.getId()+"", ctx);
    			//设置连接所在的机器
    			redisService.addUserToRoutTable(user.getId(), 
    					zookeeperService.getInnerIpPort(zookeeperService.getServiceName()));
    			redisService.addServiceCount(zookeeperService.getServiceName());
			} else {
				logger.debug("认证时的地址："+sender);
			}
			ServerAuthBackMessage authBackMessage = new ServerAuthBackMessage();
			authBackMessage.setState(ServerAuthBackMessage.success);
			ImEncoder.write(ctx, authBackMessage,sender);
			return;
		}
    	//未认证
    	if (Configure.isTcp && getUserId(ctx)==null) {
    		ServerAuthBackMessage authBackMessage = new ServerAuthBackMessage();
			authBackMessage.setState(ServerAuthBackMessage.failure);
			ImEncoder.write(ctx, authBackMessage,sender);
			return;
		}
    	//刷新token的过期时间
    	redisService.resetTokenTime(message.getToken());
    	//如果是心跳包
    	if (message instanceof ClientHeartMessage) {
		}
    	//客户端确认消息
    	if(message instanceof ClientACKMessage) {
    		ClientACKMessage msgSrc = (ClientACKMessage) message;
    		ServerACKMessage msgTo = new ServerACKMessage();
    		msgTo.setAckMid(msgSrc.getAckMid());
    		msgTo.setRoutId(msgSrc.getRoutId());
    		
    		msgTo.setGid(msgSrc.getGid());
    		msgTo.setGtype(msgSrc.getGtype());
    		msgTo.setTime(msgSrc.getTime());
    		
    		//发送给另一个客户端（原始消息的发送方）
    		ImServer.addMessageToQueue(msgTo);
    	} else if (message instanceof ClientReadMessage) {
    		ClientReadMessage msgSrc = (ClientReadMessage) message;
    		ServerReadMessage msgTo = new ServerReadMessage();
    		msgTo.setReadMid(msgSrc.getReadMid());
    		msgTo.setRoutId(msgSrc.getRoutId());
    		
    		msgTo.setGid(msgSrc.getGid());
    		msgTo.setGtype(msgSrc.getGtype());
    		msgTo.setTime(msgSrc.getTime());
    		
    		//发送给另一个客户端（原始消息的发送方）
    		ImServer.addMessageToQueue(msgTo);
		}
    }
      
    @Override  
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  
            throws Exception {
    	logger.error("TCP连接发生异常：",cause);
        ctx.close();  
    }
    
    public boolean sendMessage(int userId,ServerImMessage msg){
    	ChannelHandlerContext ctx = userIdContextMap.get(userId+"");
    	if (ctx==null) {
			return false;
		}
    	ctx.write(msg);
    	ctx.flush();
    	return true;
    }
    
    private static String getUserId(ChannelHandlerContext ctx){
    	return ctx.channel().attr(userIdAttr).get();
    }
    private static void setUserId(ChannelHandlerContext ctx,String userId) {
    	ctx.channel().attr(userIdAttr).set(userId);
    }
    public static InetSocketAddress getClientInetSocketAddress(RedisService redisService,int userId){
    	return redisService.getUserAddr(userId);
    }
    public static boolean setClientInetSocketAddress(RedisService redisService,int userId,InetSocketAddress addr) {
    	return redisService.setUserAddr(userId, addr);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	//移除channel
    	String userId = getUserId(ctx);
    	if (userId!=null) {
    		logger.info("用户连接断开,urserId:"+userId);
    		userIdContextMap.remove(userId);
    		redisService.deleteUserFromRoutTable(Integer.valueOf(userId));
    		redisService.deleteServiceCount(zookeeperService.getServiceName());
		}
        ctx.fireChannelActive();
    }
    public static ChannelHandlerContext getContextByUserId(int userId){
    	return userIdContextMap.get(userId+"");
    }
    
    @Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			switch (e.state()) {
            case READER_IDLE:
            	logger.info("长时间未收到心跳消息，即将断开连接");
            	ctx.close();
                break;
			default:
				break;
			}
//			ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}
}  
