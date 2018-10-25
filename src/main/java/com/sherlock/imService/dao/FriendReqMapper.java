package com.sherlock.imService.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.sherlock.imService.entity.po.FriendReq;

@Mapper
public interface FriendReqMapper {
	public FriendReq get(int id);
	public FriendReq getByUserIds(FriendReq po);
	
	public Integer insert(FriendReq po);
	
	public List<FriendReq> getFriendReqs(int userId);
	
	public int update(FriendReq param);
}
