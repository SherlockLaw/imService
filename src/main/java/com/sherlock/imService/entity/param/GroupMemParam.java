package com.sherlock.imService.entity.param;

import java.util.List;

import com.sherlock.imService.entity.po.GroupMem;

public class GroupMemParam extends GroupMem{
	private List<Integer> groupIds;

	public List<Integer> getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(List<Integer> groupIds) {
		this.groupIds = groupIds;
	}
}
