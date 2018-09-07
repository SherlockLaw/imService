package com.sherlock.imService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sherlock.imService.common.Result;
import com.sherlock.imService.service.GroupMemService;

@RestController
@RequestMapping(value="/groupMem")
public class GroupMemController {
	@Autowired
	private GroupMemService groupMemService;
	
	@RequestMapping(value="/getGroupMem",method={RequestMethod.GET})
	public Result getGroupMem(@RequestParam int groupId) {
		return Result.success(groupMemService.getGroupMemIds(groupId));
	}
}
