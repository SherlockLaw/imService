package com.sherlock.imService.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.sherlock.imService.entity.po.Group;

@Mapper
public interface GroupMapper {
	public Integer insert(Group po);
	
	public Group getById(int id);
	
	public List<Group> getGroups(int userId);
}
