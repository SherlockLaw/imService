package com.sherlock.imService.dubbo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sherlock.imService.netty.ImServer;

@Service
public class MessageServiceImpl implements IMessageService{

	@Autowired
	private ImServer imServer;
	@Override
	public boolean sendMessage(String jsonMessage) {
//		imServer.sendMessage(jsonMessage);
		return true;
	}

}
