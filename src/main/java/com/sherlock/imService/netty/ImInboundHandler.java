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
import com.sherlock.imService.netty.entity.ACKMessage;
import com.sherlock.imService.netty.entity.AbstractMessage;
import com.sherlock.imService.netty.entity.AddFriendConfirmMessage;
import com.sherlock.imService.netty.entity.AuthBackMessage;
import com.sherlock.imService.netty.entity.ClientAuthMessage;
import com.sherlock.imService.netty.entity.ClientHeartMessage;
import com.sherlock.imService.netty.entity.ImMessage;
import com.sherlock.imService.netty.entity.OfflineMessage;
import com.sherlock.imService.netty.entity.ReadMessage;
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
	private static AttributeKey<String> tokenAttr = AttributeKey.valueOf("token");
	
	private ImServer imServer;
	
	public ImInboundHandler(ImServer imServer){
		this.imServer = imServer;
	}
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
    	logger.info("收到来自客户端的消息:"+msg.getClass().getSimpleName()+JSONObject.toJSONString(msg));
    	
    	AbstractMessage message = (AbstractMessage) msg;
    	clientMessageHandler(imServer, ctx, message,null);
    }
    
    public static void clientMessageHandler(ImServer imServer, ChannelHandlerContext ctx, 
    		AbstractMessage message,DatagramPacket packet){
    	InetSocketAddress sender = null;
    	if (!Configure.isTcp) {
    		sender = packet.sender();
    	}
    	//如果是认证信息
    	if (message instanceof ClientAuthMessage) {
    		ClientAuthMessage msg= (ClientAuthMessage) message;
    		//先校验token
        	if (StringUtils.isBlank(msg.getToken())) {
        		logger.info("客户端token没有传输");
    			ctx.disconnect();
    			return;
    		}
    		UserVO user = imServer.redisService.getUserByToken(msg.getToken());
    		if (Configure.isTcp) {
    			setUserId(ctx, user.getId()+"");
    			setToken(ctx, msg.getToken());
    			userIdContextMap.put(user.getId()+"", ctx);
    			//设置连接所在的机器
    			imServer.redisService.addUserToRoutTable(user.getId(), 
    					imServer.zookeeperService.getInnerIpPort(imServer.zookeeperService.getServiceName()));
    			imServer.redisService.addServiceCount(imServer.zookeeperService.getServiceName());
			} else {
				logger.debug("认证时的地址："+sender);
			}
			AuthBackMessage authBackMessage = new AuthBackMessage();
			authBackMessage.setState(AuthBackMessage.success);
			ImEncoder.write(ctx, authBackMessage,sender);
			return;
		}
    	String token = null;
    	if (Configure.isTcp && (token=getToken(ctx))==null) {
    		logger.info("客户端未认证");
    		AuthBackMessage authBackMessage = new AuthBackMessage();
			authBackMessage.setState(AuthBackMessage.failure);
			ImEncoder.write(ctx, authBackMessage,sender);
			ctx.close();
			return;
		}
    	/**
    	 * 认证通过后的逻辑
    	 */
    	Integer userId = null;
    	if (!Configure.isTcp) {
    		userId = Integer.valueOf(getUserId(ctx));
    		boolean offline = setClientInetSocketAddress(imServer.redisService, userId, sender);
//    		if (offline) {
//				OfflineMessage offlineMessage = new OfflineMessage();
//				ImEncoder.write(ctx, offlineMessage,sender);
//			}
		}
    	
    	//刷新token的过期时间
    	imServer.redisService.resetTokenTime(token);
    	//如果是心跳包
    	if (message instanceof ClientHeartMessage) {
		}
    	
    	if(message instanceof ACKMessage) {
    		//消息确认消息
    		ACKMessage msgSrc = (ACKMessage) message;
    		ImServer.addMessageToQueue(msgSrc);
    		return;
    	}
    	if (message instanceof ReadMessage) {
    		//已读消息
    		ReadMessage msgSrc = (ReadMessage) message;
    		ImServer.addMessageToQueue(msgSrc);
    		return;
		}
//    	if (message instanceof AddFriendConfirmMessage) {
//    		//添加好友，接收者确认消息
//    		AddFriendConfirmMessage msg = (AddFriendConfirmMessage) message;
//    		imServer.friendService.addFriends(msg.getFromUserId(), msg.getToUserId(), msg.getStatus());
//    		return;
//		}
    }
      
    @Override  
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  
            throws Exception {
    	logger.error("TCP连接发生异常：",cause);
        ctx.close();  
    }
    
    public boolean sendMessage(int userId,ImMessage msg){
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
    private static String getToken(ChannelHandlerContext ctx){
    	return ctx.channel().attr(tokenAttr).get();
    }
    private static void setToken(ChannelHandlerContext ctx,String token) {
    	ctx.channel().attr(tokenAttr).set(token);
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
    		imServer.redisService.deleteUserFromRoutTable(Integer.valueOf(userId));    			
    		imServer.redisService.deleteServiceCount(imServer.zookeeperService.getServiceName());
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
