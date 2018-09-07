package com.sherlock.imService.netty.entity;

import com.sherlock.imService.constant.MessageConstant;

public class ServerPicMessage extends ServerImMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8850305598606791473L;
	private String url;
	private Integer width;
	private Integer heigth;
	
	public ServerPicMessage(){
		this.messageType = MessageConstant.MSGTYPE_PIC;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeigth() {
		return heigth;
	}

	public void setHeigth(Integer heigth) {
		this.heigth = heigth;
	}
}
