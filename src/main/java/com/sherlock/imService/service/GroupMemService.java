package com.sherlock.imService.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sherlock.imService.dao.GroupMemMapper;
import com.sherlock.imService.entity.param.GroupMemParam;
import com.sherlock.imService.entity.po.GroupMem;
import com.sherlock.imService.entity.po.User;

@Service
public class GroupMemService {

	@Autowired
	private GroupMemMapper groupMemMapper;
	
	@Autowired
	private UserService userService;
	
	public Integer insert(int groupId, int userId) {
		GroupMem po = new GroupMem();
		po.setGroupId(groupId);
		po.setUserId(userId);
		return groupMemMapper.insert(po);
	}
	
	public List<User> getGroupMem(int groupId){
		List<GroupMem> groupMems = groupMemMapper.getGroupMems(groupId);
		List<Integer> uids = new ArrayList<>(groupMems.size());
		for (GroupMem po : groupMems) {
			uids.add(po.getUserId());
		}
		List<User> list = userService.getUserListByIds(uids);
		return list;
	}
	
	public List<Integer> getGroupMemIds(int groupId){
		List<GroupMem> groupMems = groupMemMapper.getGroupMems(groupId);
		List<Integer> list = new ArrayList<>(groupMems.size());
		for (GroupMem po : groupMems) {
			list.add(po.getUserId());
		}
		return list;
	}
	public List<Integer> getGroupMemIdsByParam(GroupMemParam param){
		List<GroupMem> groupMems = groupMemMapper.getGroupMemByParam(param);
		List<Integer> list = new ArrayList<>(groupMems.size());
		for (GroupMem po : groupMems) {
			list.add(po.getUserId());
		}
		return list;
	}
	public boolean checkMember(int groupId, int userId) {
		GroupMem po = new GroupMem();
		po.setGroupId(groupId);
		po.setUserId(userId);
		return groupMemMapper.checkMember(po);
	}
}
