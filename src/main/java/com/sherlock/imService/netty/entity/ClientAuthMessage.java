package com.sherlock.imService.netty.entity;

import com.sherlock.imService.netty.configure.Configure;

public class ClientAuthMessage extends AbstractMessage{
	private String token;

	public ClientAuthMessage(){
		this.msgType = Configure.MSG_AUTH;
	}
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	
}
