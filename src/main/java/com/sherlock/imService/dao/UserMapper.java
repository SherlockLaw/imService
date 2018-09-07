package com.sherlock.imService.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.sherlock.imService.entity.param.UserParam;
import com.sherlock.imService.entity.po.User;

@Mapper
public interface UserMapper {
	
	public Integer insert(User po);
	
	public User getByAccount(String account);
	public User getById(int id);
	
	public List<User> search(String keyword);
	
	public boolean existUser(int id);
	
	public int update(UserParam param);
	public int updatePwd(UserParam param);
	
	public List<User> getUserList(List<Integer> userIdList);
}
