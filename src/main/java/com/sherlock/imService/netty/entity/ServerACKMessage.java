package com.sherlock.imService.netty.entity;

import com.sherlock.imService.constant.MessageConstant;

public class ServerACKMessage extends ConversationOrderMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2309078945412579618L;
	private String ackMid;
	public ServerACKMessage(){
		this.messageType = MessageConstant.MSG_SERVER_ACK;
	}
	public String getAckMid() {
		return ackMid;
	}
	public void setAckMid(String ackMid) {
		this.ackMid = ackMid;
	}
}
