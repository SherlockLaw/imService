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
	
	@RequestMapping(value="/addFriendRequest",method={RequestMethod.POST})
	public Result addFriendRequest(@RequestParam int fromUserId,@RequestParam int toUserId) {
		friendService.addFriendRequest(fromUserId, toUserId);
		return Result.success("");
	}
	@RequestMapping(value="/delFriend",method={RequestMethod.POST})
	public Result delFriend(@RequestParam int fromUserId,@RequestParam int toUserId) {
		friendService.delFriend(fromUserId, toUserId);
		return Result.success("");
	}
//	@RequestMapping(value="/addFriends",method={RequestMethod.POST})
//	public Result addFriends(@RequestParam int fromUserId,@RequestParam int toUserId) {
//		return Result.success(friendService.addFriends(fromUserId, toUserId));
//	}
	
	@RequestMapping(value="/getFriends",method={RequestMethod.GET})
	public Result getFriends(@RequestParam int userId) {
		return Result.success(friendService.getFriends(userId));
	}
	
	@RequestMapping(value="/getFriendRequests",method={RequestMethod.GET})
	public Result getFriendRequests(@RequestParam int userId) {
		return Result.success(friendService.getFriendReqs(userId));
	}
	
	@RequestMapping(value="/confirmFriendRequest",method={RequestMethod.POST})
	public Result confirmFriendRequest(@RequestParam int id, int status) {
		return Result.success(friendService.confirmFriendRequest(id, status));
	}
}
