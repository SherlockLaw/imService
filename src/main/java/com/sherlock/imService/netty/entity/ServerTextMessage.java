package com.sherlock.imService.netty.entity;

import com.sherlock.imService.constant.MessageConstant;

public class ServerTextMessage extends ServerImMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6666239155994870711L;
	private String content;
	public ServerTextMessage(){
		this.messageType = MessageConstant.MSGTYPE_TEXT;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
