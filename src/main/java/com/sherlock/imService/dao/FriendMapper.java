package com.sherlock.imService.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.sherlock.imService.entity.po.Friend;

@Mapper
public interface FriendMapper {
	
	public Integer insert(Friend po);
	
	public List<Friend> getFriends(int userId);
	
	public boolean isFriends(Friend po);
	
}
