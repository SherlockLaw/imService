package com.sherlock.imService.netty.entity;

import com.sherlock.imService.netty.configure.Configure;

public class ServerAuthBackMessage extends ServerCommonMessage{

	public static final int success = 1;
	public static final int failure = 0;
	
	private int state;//成功：1，失败：0
	
	public ServerAuthBackMessage(){
		this.msgType = Configure.MSG_AUTH_BACK;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}
