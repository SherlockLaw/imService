package com.sherlock.imService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sherlock.imService.common.Result;
import com.sherlock.imService.zookeeper.ZookeeperService;

@RestController
@RequestMapping(value="/server")
public class ServerController {
	
	@Autowired
	private ZookeeperService zookeeperService;
	
	@RequestMapping(value="/getConnectServer", method={RequestMethod.GET})
	public Result getConnectServer() {
		return Result.success(zookeeperService.getConnectServer());
	}
}
