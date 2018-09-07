package com.sherlock.imService.netty.entity;

import com.sherlock.imService.netty.configure.Configure;

public class ClientAuthMessage extends ClientCommonMessage{

	public ClientAuthMessage(){
		this.msgType = Configure.MSG_AUTH;
	}
	
}
