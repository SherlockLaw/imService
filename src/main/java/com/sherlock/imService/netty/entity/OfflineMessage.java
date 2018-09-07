package com.sherlock.imService.netty.entity;

import com.sherlock.imService.netty.configure.Configure;

public class OfflineMessage extends ServerCommonMessage{

	public OfflineMessage(){
		this.msgType = Configure.MSG_OFFLINE;
	}
}
