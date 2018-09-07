package com.sherlock.imService.netty.entity;

public abstract class ServerCommonMessage extends AbstractMessage{
	private int routId;//消息路由目的

	public int getRoutId() {
		return routId;
	}

	public void setRoutId(int routId) {
		this.routId = routId;
	}
}
