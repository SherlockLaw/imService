package com.sherlock.imService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sherlock.imService.common.Result;
import com.sherlock.imService.service.FriendService;

@RestController
@RequestMapping(value="/friend")
public class FriendController {

	@Autowired
	private FriendService friendService;
	
	@RequestMapping(value="/addFriends",method={RequestMethod.POST})
	public Result addFriends(@RequestParam int fromUserId,@RequestParam int toUserId) {
		return Result.success(friendService.addFriends(fromUserId, toUserId));
	}
	
	@RequestMapping(value="/getFriends",method={RequestMethod.GET})
	public Result getFriends(@RequestParam int userId) {
		return Result.success(friendService.getFriends(userId));
	}
}
