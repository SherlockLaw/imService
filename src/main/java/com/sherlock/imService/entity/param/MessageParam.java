package com.sherlock.imService.entity.param;

public class MessageParam {

	private String mid;
	private Integer fromUserId;
	private Integer gtype;//在消息列表中的人
	private Integer gid;
	private Integer messageType;//消息类型，文本
	
	private String content;//TODO:文本，暂时放在这里，以后使用应用
	private String imageUrl;//图片url
	private Integer width;
	private Integer height;
	
	private String retractMid;//消息Id
	
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public Integer getFromUserId() {
		return fromUserId;
	}
	public void setFromUserId(Integer fromUserId) {
		this.fromUserId = fromUserId;
	}
	public Integer getGtype() {
		return gtype;
	}
	public void setGtype(Integer gtype) {
		this.gtype = gtype;
	}
	public Integer getGid() {
		return gid;
	}
	public void setGid(Integer gid) {
		this.gid = gid;
	}
	public Integer getMessageType() {
		return messageType;
	}
	public void setMessageType(Integer messageType) {
		this.messageType = messageType;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	public Integer getHeight() {
		return height;
	}
	public void setHeight(Integer height) {
		this.height = height;
	}
	public String getRetractMid() {
		return retractMid;
	}
	public void setRetractMid(String retractMid) {
		this.retractMid = retractMid;
	}

}
