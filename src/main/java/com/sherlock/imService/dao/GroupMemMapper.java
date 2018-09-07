package com.sherlock.imService.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.sherlock.imService.entity.param.GroupMemParam;
import com.sherlock.imService.entity.po.GroupMem;

@Mapper
public interface GroupMemMapper {
	public Integer insert(GroupMem po);
	
	public List<GroupMem> getGroupMems(int groupId);
	
	public List<GroupMem> getGroupMemByParam(GroupMemParam param);
	
	public boolean checkMember(GroupMem po);
}
