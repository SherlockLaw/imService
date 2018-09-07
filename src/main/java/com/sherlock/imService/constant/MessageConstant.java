package com.sherlock.imService.constant;

import com.alibaba.fastjson.JSONObject;
import com.sherlock.imService.exception.ServiceException;
import com.sherlock.imService.netty.configure.Configure;
import com.sherlock.imService.netty.entity.ClientACKMessage;
import com.sherlock.imService.netty.entity.ClientAuthMessage;
import com.sherlock.imService.netty.entity.ClientCommonMessage;
import com.sherlock.imService.netty.entity.ClientHeartMessage;
import com.sherlock.imService.netty.entity.ClientReadMessage;
import com.sherlock.imService.netty.entity.ConversationOrderMessage;
import com.sherlock.imService.netty.entity.OfflineMessage;
import com.sherlock.imService.netty.entity.ServerACKMessage;
import com.sherlock.imService.netty.entity.ServerAddFriendMessage;
import com.sherlock.imService.netty.entity.ServerAddGroupMessage;
import com.sherlock.imService.netty.entity.ServerAuthBackMessage;
import com.sherlock.imService.netty.entity.ServerCommonMessage;
import com.sherlock.imService.netty.entity.ServerImMessage;
import com.sherlock.imService.netty.entity.ServerPicMessage;
import com.sherlock.imService.netty.entity.ServerReadMessage;
import com.sherlock.imService.netty.entity.ServerRetractMessage;
import com.sherlock.imService.netty.entity.ServerTextMessage;

public class MessageConstant {
	public static final int MSGTYPE_TEXT = 1;
	public static final int MSGTYPE_PIC = 2;
	public static final int MSGTYPE_RETRACT = 3;
	
	//指令类型
	public final static byte MSG_SERVER_ACK = 51;//服务端转发客户端确认消息
	public final static byte MSG_SERVER_READ = 52;//服务端转发客户端已读消息
	public enum GTypeEnum{	
		friend(1,"好友"),
		group(2,"群组");
		
		private int index;
		private String text;
		
		GTypeEnum(int sex, String text){
			this.index = sex;
			this.text = text;
		}
		
		public static GTypeEnum getEnum(int index) {
			GTypeEnum[] l = GTypeEnum.values();
			for (GTypeEnum e: l){
				if (e.index==index) {
					return e;
				}
			}
			return null;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}	
	}
	public static ClientCommonMessage getClientCommonMessage(byte msgType, String str){
		ClientCommonMessage msg = null;
		switch (msgType) {
		case Configure.MSG_CLIENT_ACK:
			msg = JSONObject.parseObject(str, ClientACKMessage.class);
			break;
		case Configure.MSG_CLIENT_READ:
			msg = JSONObject.parseObject(str, ClientReadMessage.class);
			break;
		case Configure.MSG_CLIENT_HEARTBEAT:
			msg = JSONObject.parseObject(str, ClientHeartMessage.class);
			break;
		case Configure.MSG_AUTH:
			msg = JSONObject.parseObject(str, ClientAuthMessage.class);
			break;
		default:
			throw new ServiceException("不支持的消息类型");
		}
		return msg;
	}
	public static ServerCommonMessage getServerCommonMessage(String str){
		JSONObject job = JSONObject.parseObject(str);
		byte msgType = job.getByteValue("msgType");
		ServerCommonMessage msg = getServerCommonMessage(msgType, str);
		return msg;
	}
	
	private static ServerCommonMessage getServerCommonMessage(byte msgType, String str) {
		ServerCommonMessage msg = null;
		switch (msgType) {
			case Configure.MSG_IM:
				msg = getImMessage(str);
				break;
			case Configure.MSG_ORDER:
				msg = getConversationOrderMessage(str);
				break;
			case Configure.MSG_AUTH_BACK:
				msg = JSONObject.parseObject(str, ServerAuthBackMessage.class);
				break;
			case Configure.MSG_ADDFRIEND:
				msg = JSONObject.parseObject(str, ServerAddFriendMessage.class);
				break;
			case Configure.MSG_ADDGROUP:
				msg = JSONObject.parseObject(str, ServerAddGroupMessage.class);
				break;
			case Configure.MSG_OFFLINE:
				msg = JSONObject.parseObject(str, OfflineMessage.class);
				break;
			default:
				throw  new ServiceException("不支持的消息类型");
		}
		return msg;
	}
	/**
	 * 生成Im消息对象
	 * @param str
	 * @return
	 */
	private static ServerImMessage getImMessage(String str) {
		JSONObject job = JSONObject.parseObject(str);
		int messageType = job.getIntValue("messageType");
		ServerImMessage msg = getServerImMessage0(str,messageType);
		return msg;
	}
	private static ServerImMessage getServerImMessage0(String str,int messageType){
		ServerImMessage msg = null;
		switch (messageType) {
			case MessageConstant.MSGTYPE_TEXT:
				msg = JSONObject.parseObject(str, ServerTextMessage.class);
				break;
			case MessageConstant.MSGTYPE_PIC:
				msg = JSONObject.parseObject(str, ServerPicMessage.class);
				break;
			case MessageConstant.MSGTYPE_RETRACT:
				msg = JSONObject.parseObject(str, ServerRetractMessage.class);

		}
		return msg;
	}
	/**
	 * 生成指令消息对象
	 * @param str
	 * @return
	 */
	public static ConversationOrderMessage getConversationOrderMessage(String str) {

		JSONObject job = JSONObject.parseObject(str);
		int messageType = job.getIntValue("messageType");
		ConversationOrderMessage msg = getConversationOrderMessage0(str,messageType);
		return msg;
	}
	private static ConversationOrderMessage getConversationOrderMessage0(String str,int messageType){
		ConversationOrderMessage msg = null;
		switch (messageType) {
			case MessageConstant.MSG_SERVER_ACK:
				msg = JSONObject.parseObject(str, ServerACKMessage.class);
				break;
			case MessageConstant.MSG_SERVER_READ:
				msg = JSONObject.parseObject(str, ServerReadMessage.class);
				break;
		}
		return msg;
	}
}
