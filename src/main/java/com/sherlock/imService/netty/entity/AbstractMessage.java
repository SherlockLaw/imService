package com.sherlock.imService.netty.entity;

import java.io.Serializable;

public abstract class AbstractMessage implements Serializable{

	//消息头结构 startFlag+version+msgType+length
//	protected int startFlag;//开始标志 4字节
//	protected byte version;//协议版本号 1字节
	protected byte msgType;//消息类型 1字节
	//length 这个包的字节长度（4字节）
	public byte getMsgType() {
		return msgType;
	}
	
	public void setMsgType(byte msgType) {
		this.msgType = msgType;
	}
}
