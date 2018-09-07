package com.sherlock.imService.netty.entity;

import com.sherlock.imService.netty.configure.Configure;

public class ServerAddFriendMessage extends ServerCommonMessage{
	private Integer fromUserId;
	private String name;
	private Integer sex;
	private String headPic;
	public ServerAddFriendMessage(){
		this.msgType = Configure.MSG_ADDFRIEND;
	}
	public Integer getFromUserId() {
		return fromUserId;
	}
	public void setFromUserId(Integer fromUserId) {
		this.fromUserId = fromUserId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getSex() {
		return sex;
	}
	public void setSex(Integer sex) {
		this.sex = sex;
	}
	public String getHeadPic() {
		return headPic;
	}
	public void setHeadPic(String headPic) {
		this.headPic = headPic;
	}
}
