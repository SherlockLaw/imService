package com.sherlock.imService.netty.entity;

import com.sherlock.imService.netty.configure.Configure;

public class AddGroupMessage extends OrderMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8134141333686905110L;
	private Integer groupId;
	
	public AddGroupMessage(){
		this.msgType = Configure.MSG_ADDGROUP;
	}
	
	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	
}
