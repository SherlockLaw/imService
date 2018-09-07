package com.sherlock.imService.netty.entity;

import com.sherlock.imService.netty.configure.Configure;

public class ServerAddGroupMessage extends ServerCommonMessage{

	private Integer groupId;
	
	public ServerAddGroupMessage(){
		this.msgType = Configure.MSG_ADDGROUP;
	}
	
	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	
}
