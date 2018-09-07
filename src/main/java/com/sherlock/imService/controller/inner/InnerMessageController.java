package com.sherlock.imService.controller.inner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sherlock.imService.common.Result;
import com.sherlock.imService.service.MessageService;

@RestController
@RequestMapping("/inner/message")
public class InnerMessageController {

	@Autowired
	private MessageService messageService;
	
	@RequestMapping(value="/routMessage", method={RequestMethod.POST})
	public Result routMessage(String jsonMessage){
		return Result.success(messageService.routMessage(jsonMessage));
	}
}
