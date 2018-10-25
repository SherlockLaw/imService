package com.sherlock.imService.netty.entity;

import com.sherlock.imService.constant.MessageConstant;

public class ReadMessage extends ConversationOrderMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2937724115009244835L;
	private String readMid;
	public ReadMessage(){
		this.messageType = MessageConstant.MSG_SERVER_READ;
	}
	public String getReadMid() {
		return readMid;
	}
	public void setReadMid(String readMid) {
		this.readMid = readMid;
	}
}

