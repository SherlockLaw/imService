package com.sherlock.imService.entity.po;

public class Friend {
	private Integer id;
	private Integer fromUserId;
	private Integer toUserId;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getFromUserId() {
		return fromUserId;
	}
	public void setFromUserId(Integer fromUserId) {
		this.fromUserId = fromUserId;
	}
	public Integer getToUserId() {
		return toUserId;
	}
	public void setToUserId(Integer toUserId) {
		this.toUserId = toUserId;
	}
	@Override
	public String toString() {
		return "Friend [id=" + id + ", fromUserId=" + fromUserId + ", toUserId=" + toUserId + "]";
	}
	
}
