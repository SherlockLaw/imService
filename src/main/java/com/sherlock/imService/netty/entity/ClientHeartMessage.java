package com.sherlock.imService.netty.entity;

import com.sherlock.imService.netty.configure.Configure;

public class ClientHeartMessage extends AbstractMessage{
	public ClientHeartMessage(){
		this.msgType = Configure.MSG_CLIENT_HEARTBEAT;
	}
}
