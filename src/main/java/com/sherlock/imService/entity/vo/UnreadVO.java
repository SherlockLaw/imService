package com.sherlock.imService.entity.vo;

import java.io.Serializable;

public class UnreadVO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5919305650316992553L;
	private int count;
	private String lastMsg;
	private long lastTime;
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getLastMsg() {
		return lastMsg;
	}
	public void setLastMsg(String lastMsg) {
		this.lastMsg = lastMsg;
	}
	public long getLastTime() {
		return lastTime;
	}
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
}
