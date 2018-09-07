package com.sherlock.imService.entity.vo;

import com.sherlock.imService.entity.po.User;

public class UserVO extends User{

	private String token;

	private long lastRefreshTime;
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public long getLastRefreshTime() {
		return lastRefreshTime;
	}

	public void setLastRefreshTime(long lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}
	
}
