package com.sherlock.imService.netty.entity;

/**
 * 会话组离线消息
 * @author Administrator
 *
 */
public abstract class ConversationOfflineMessage extends ServerCommonMessage{

	protected int fromUserId;//发送方Id
	protected int gid;//会话Id 用户/会话组 的id
	protected int gtype;//会话类型  用户/会话组
	protected int messageType;//消息类型
	
	protected String mid;//消息Id
	protected long time;//发送时间
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
	public int getMessageType() {
		return messageType;
	}
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
}
