package com.sherlock.imService.netty.entity;

import com.sherlock.imService.netty.configure.Configure;

public class ClientACKMessage extends ClientCommonMessage{
	private int routId;//消息路由目的

	private String ackMid;
	private int fromUserId;//发送方Id
	private int gid;//会话Id 用户/会话组 的id
	private int gtype;//会话类型  用户/会话组
	private long time;//发送时间
	
	public ClientACKMessage(){
		this.msgType = Configure.MSG_CLIENT_ACK;
	}
	
	public String getAckMid() {
		return ackMid;
	}

	public void setAckMid(String ackMid) {
		this.ackMid = ackMid;
	}

	public int getRoutId() {
		return routId;
	}

	public void setRoutId(int routId) {
		this.routId = routId;
	}
	public int getFromUserId() {
		return fromUserId;
	}
	public void setFromUserId(int fromUserId) {
		this.fromUserId = fromUserId;
	}
	public int getGid() {
		return gid;
	}
	public void setGid(int gid) {
		this.gid = gid;
	}
	public int getGtype() {
		return gtype;
	}
	public void setGtype(int gtype) {
		this.gtype = gtype;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
}
