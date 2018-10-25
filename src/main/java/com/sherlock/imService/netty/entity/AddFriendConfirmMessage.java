package com.sherlock.imService.netty.entity;

import com.sherlock.imService.entity.po.User;
import com.sherlock.imService.netty.configure.Configure;

public class AddFriendConfirmMessage extends OrderMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8046937672934478015L;
	private Integer addFriendReqId;
	private Integer status;
	private User userInfo;
	public AddFriendConfirmMessage(){
		this.msgType = Configure.MSG_ADDFRIEND_CONFIRM;
	}
	public Integer getAddFriendReqId() {
		return addFriendReqId;
	}
	public void setAddFriendReqId(Integer addFriendReqId) {
		this.addFriendReqId = addFriendReqId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public User getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(User userInfo) {
		this.userInfo = userInfo;
	}

}
