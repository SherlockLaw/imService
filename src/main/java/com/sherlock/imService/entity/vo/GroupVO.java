package com.sherlock.imService.entity.vo;

import java.util.List;

import com.sherlock.imService.entity.po.Group;
import com.sherlock.imService.entity.po.User;

public class GroupVO extends Group{
	private List<Integer> memberIds;
	
	private List<User> memberList;

	public List<Integer> getMemberIds() {
		return memberIds;
	}

	public void setMemberIds(List<Integer> memberIds) {
		this.memberIds = memberIds;
	}

	public List<User> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<User> memberList) {
		this.memberList = memberList;
	}
	
}
