package com.sherlock.imService.netty.entity;

public abstract class ClientCommonMessage extends AbstractMessage{

	protected String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
