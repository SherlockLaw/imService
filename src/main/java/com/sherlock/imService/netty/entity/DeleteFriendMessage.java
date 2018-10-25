package com.sherlock.imService.netty.entity;

import com.sherlock.imService.netty.configure.Configure;

public class DeleteFriendMessage extends OrderMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1298512553788627984L;
	private Integer friendId;

	public DeleteFriendMessage(){
		this.msgType = Configure.MSG_DELFRIEND;
	}
	public Integer getFriendId() {
		return friendId;
	}

	public void setFriendId(Integer friendId) {
		this.friendId = friendId;
	}
}
