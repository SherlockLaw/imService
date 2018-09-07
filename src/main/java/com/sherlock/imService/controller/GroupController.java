package com.sherlock.imService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sherlock.imService.common.Result;
import com.sherlock.imService.service.GroupService;

@RestController
@RequestMapping(value="/group")
public class GroupController {

	@Autowired
	private GroupService groupService;
	
	@RequestMapping(value="/addGroup",method={RequestMethod.POST})
	public Result addGroup(String name,MultipartFile headPic,@RequestParam int creatorId,int[] memberIds) {
		return Result.success(groupService.addGroup(name, headPic, creatorId, memberIds));
	}
	
	@RequestMapping(value="/getById",method={RequestMethod.GET})
	public Result getById(@RequestParam int groupId) {
		return Result.success(groupService.getById(groupId));
	}
	
	@RequestMapping(value="/getGroups",method={RequestMethod.GET})
	public Result getGroups(@RequestParam int userId) {
		return Result.success(groupService.getGroups(userId));
	}
}
