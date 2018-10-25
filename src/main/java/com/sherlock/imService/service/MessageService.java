package com.sherlock.imService.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sherlock.imService.constant.MessageConstant;
import com.sherlock.imService.constant.MessageConstant.GTypeEnum;
import com.sherlock.imService.entity.param.MessageParam;
import com.sherlock.imService.entity.vo.UnreadVO;
import com.sherlock.imService.exception.ServiceException;
import com.sherlock.imService.netty.ImServer;
import com.sherlock.imService.netty.entity.ConversationOfflineMessage;
import com.sherlock.imService.netty.entity.ImMessage;
import com.sherlock.imService.netty.entity.OrderMessage;
import com.sherlock.imService.netty.entity.PicMessage;
import com.sherlock.imService.netty.entity.RetractMessage;
import com.sherlock.imService.netty.entity.TextMessage;
import com.sherlock.imService.redis.RedisService;

@Service
public class MessageService {
	@Autowired
	private RedisService redisService;
	@Autowired
	private GroupMemService groupMemService;
	@Autowired
	private ImServer imServer;

	public String sendMessage(MessageParam param) {
		if (param.getFromUserId()==null) {
			throw new ServiceException("fromUserId不能为空");
		}
		if (param.getGtype()==null) {
			throw new ServiceException("gtype不能为空");
		}
		if (param.getGid()==null) {
			throw new ServiceException("gid不能为空");
		}
		if (param.getMessageType()==null) {
			throw new ServiceException("messageType不能为空");
		}
		/*TODO:消息发送增加ACK确认流程，发送未确认送达前，不算发送成功，1客户端A发送消息给服务器-》2消息队列-》3返回给A
		->4客户端B在线，转5，不在线转8    5服务端发送消息给B-》6B收到后确认返回给服务端-》7服务端给A发送（该消息已投递给B的消息）
		8 给B存离线，转7
		如果7出现A断线的情况，给A存离线，A上线后拉取
		*/
		ImMessage msg = getMessage(param);
		if (GTypeEnum.friend.getIndex() == param.getGtype()) {
			msg.setRoutId(param.getGid());
			addMessageToQueue(msg);
		} else if (GTypeEnum.group.getIndex() == param.getGtype()) {
			/*
			 * 只存储群组消息，没有ACK确认流程，客户端在线时，记录消息最后接收的服务器消息时间）
			 * 离线再上线的情况，将该时间从服务器拉取离线消息，记录离线消息的最后一条消息时间
			 */
			redisService.saveGroupMessage(msg);
			// 推到群组成员
			List<Integer> memIdList = groupMemService.getGroupMemIds(param.getGid());
			for (Integer uid : memIdList) {
				if (param.getFromUserId().intValue() != uid) {
					ImMessage message = msg.clone();
					message.setRoutId(uid);
					addMessageToQueue(message);
				}
			}
		}
		return msg.getMid();
	}
	/**
	 * 其他机器将消息发送到这台机器
	 * @param jsonMessage
	 * @return
	 */
	public boolean routMessage(String jsonMessage){
		imServer.routMessage(jsonMessage);
		return true;
	}
	
	private void addMessageToQueue(ImMessage msg){
		try {
			ImServer.addMessageToQueue(msg);
		} catch (IllegalStateException e) {
			throw new ServiceException("服务器繁忙,请重新发送消息");
		}
	}
	/**
	 * 生成文本消息
	 * @param fromUserId
	 * @param gtype
	 * @param gid
	 * @param content
	 * @return
	 */
	public TextMessage getTextMessage(int fromUserId,int gtype, int gid,String content){
		TextMessage msg = new TextMessage();
		msg.setContent(content);
		setCommonMsg(msg, fromUserId, gtype, gid, MessageConstant.MSGTYPE_TEXT,null);
		return msg;
	}
	/**
	 * 所有im消息的共同设置
	 * @param msg
	 * @param fromUserId
	 * @param gtype
	 * @param gid
	 * @param messageType
	 */
	private void setCommonMsg(ImMessage msg, int fromUserId,int gtype, int gid,int messageType,String mid){
		msg.setFromUserId(fromUserId);
		msg.setGtype(gtype);
		msg.setGid(gid);
		msg.setMessageType(messageType);
		msg.setMid(mid);
		if (StringUtils.isBlank(msg.getMid())) {
			msg.setMid(genMid());
		}
		msg.setTime(System.currentTimeMillis());
	}
	/**
	 * 生成消息
	 */
	private ImMessage getMessage(MessageParam param){
		ImMessage msg;
		switch (param.getMessageType()) {
		case MessageConstant.MSGTYPE_TEXT:{
			if (param.getContent()==null) {
				throw new ServiceException("文本不能为空");
			}
			msg = new TextMessage();
			TextMessage message = (TextMessage) msg;
			message.setContent(param.getContent());
			break;
		}
		case MessageConstant.MSGTYPE_PIC:{
			if (param.getImageUrl()==null) {
				throw new ServiceException("图片路径不能为空");
			}
			if (param.getWidth()==null) {
				throw new ServiceException("图片宽度不能为空");
			}
			if (param.getHeight()==null) {
				throw new ServiceException("图片高度不能为空");
			}
			msg = new PicMessage();
			PicMessage message = (PicMessage) msg;
			message.setUrl(param.getImageUrl());
			message.setWidth(param.getWidth());
			message.setHeigth(param.getHeight());
			break;
		}
		case MessageConstant.MSGTYPE_RETRACT:
		{
			if (StringUtils.isBlank(param.getRetractMid())) {
				throw new ServiceException("retractMid(需要撤回的消息mid)不能为空");
			}
			msg = new RetractMessage();
			RetractMessage message = (RetractMessage) msg;
			message.setRetractMid(param.getRetractMid());
			break;
		}
		default:
			throw new ServiceException("消息类型不正确");
		}
		// 由于没有唯一的gid,对于单对单的聊天，不同的用户的消息的gid不同
		int gid = param.getGid();
		if (GTypeEnum.friend.getIndex() == param.getGtype()) {
			// 对于接收方来说，gid是发送方的id
			gid = param.getFromUserId();
		}
		setCommonMsg(msg, param.getFromUserId(), param.getGtype(), gid, param.getMessageType(),param.getMid());
		return msg;
	}
	private String genMid(){
		return UUID.randomUUID().toString().replace("-", "");
	}
	public Map<String,UnreadVO> getUnreadCountMap(int userId) {
		return redisService.getUnreadCountMap(userId);
	}
	
	public void deleteUnreadCountMap(int userId, Map<String, UnreadVO> map) {
		redisService.deleteUnreadCountMap(userId,map);
	}
	
	public List<ConversationOfflineMessage> getConversationOfflineMessage(int userId, int gtype, int gid,Long lastMsgTime){
		if (GTypeEnum.friend.getIndex()==gtype) {
			return redisService.getConversationOfflineMessage(userId, gtype, gid);
		} else if (GTypeEnum.group.getIndex()==gtype) {
			if(!groupMemService.checkMember(gid, userId)){
				throw new ServiceException("非群成员不能拉取群历史消息");
			}
			return redisService.getGroupMessage(gid, lastMsgTime);
		}
		throw new ServiceException("会话类型不正确");
		
	}
	
	public void deleteConversationOfflineMessage(int userId, int gtype, int gid,long lastMsgTime){
		if (GTypeEnum.friend.getIndex()!=gtype) {
			throw new ServiceException("只能删除好友之间的消息");
		}
		redisService.deleteConversationOfflineMessage(userId, gtype, gid, lastMsgTime);
	}
	public List<OrderMessage> getOfflineOrderMessage(int userId) {
		return redisService.getOfflineOrderMessage(userId);
	}
	
	public void delOfflineOrderMessage(int userId,long time) {
		redisService.delOfflineOrderMessage(userId, time);
	}
}
