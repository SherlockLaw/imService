package com.sherlock.imService.controller;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sherlock.imService.common.Result;
import com.sherlock.imService.service.UserService;

@RestController
@RequestMapping(value="/user")
public class UserController {

	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/register", method={RequestMethod.POST})
	public Result register(@RequestParam String account, @RequestParam String pwd,
			@RequestParam String name, @RequestParam int sex, @RequestParam MultipartFile headPic) {
		return Result.success(userService.register(account, pwd,name,sex,headPic));
	}
	
	@RequestMapping(value="/update", method={RequestMethod.POST})
	public Result update(@RequestParam int id, String name, Integer sex, MultipartFile headPic) {
		return Result.success(userService.update(id, name, sex, headPic));
	}
	@RequestMapping(value="/updatePwd", method={RequestMethod.POST})
	public Result updatePwd(@RequestParam String account, @RequestParam String oldPwd, @RequestParam String newPwd) {
		return Result.success(userService.updatePwd(account, oldPwd, newPwd));
	}
	//返回一个token 用于后续的验权
	@RequestMapping(value="/login", method={RequestMethod.POST})
	public Result login(@RequestParam String account, @RequestParam String pwd) {
		return Result.success(userService.login(account, pwd));
	}
	
	@RequestMapping(value="/search", method={RequestMethod.GET})
	public Result search(@RequestParam String keyword) {
		return Result.success(userService.search(keyword));
	}
	@RequestMapping(value="/getUserById", method={RequestMethod.GET})
	public Result getUserById(@RequestParam Integer userId) {
		return Result.success(userService.getUserById(userId));
	}
	
	@RequestMapping(value="/getUserListByIds", method={RequestMethod.GET})
	public Result getUserListByIds(@RequestParam Integer[] userIds) {
		return Result.success(userService.getUserListByIds(Arrays.asList(userIds)));
	}
}
