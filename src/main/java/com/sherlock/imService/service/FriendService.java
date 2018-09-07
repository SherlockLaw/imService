package com.sherlock.imService.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sherlock.imService.constant.MessageConstant.GTypeEnum;
import com.sherlock.imService.dao.FriendMapper;
import com.sherlock.imService.entity.po.Friend;
import com.sherlock.imService.entity.po.User;
import com.sherlock.imService.exception.ServiceException;
import com.sherlock.imService.netty.ImServer;
import com.sherlock.imService.netty.entity.ServerAddFriendMessage;
import com.sherlock.imService.netty.entity.ServerImMessage;

@Service
public class FriendService {

	@Autowired
	private FriendMapper friendMapper;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private MessageService messageService;
	
	public Integer addFriends(int fromUserId,int toUserId) {
		if (fromUserId==toUserId) {
			throw new ServiceException("不能添加自己为好友");
		}
		User fromUser = userService.getUserById(fromUserId);
		if (fromUser==null) {
			throw new ServiceException("本人用户不存在");
		}
		if (!userService.existUser(toUserId)) {
			throw new ServiceException("对方用户不存在");
		}
		Friend po = new Friend();
		po.setFromUserId(fromUserId);
		po.setToUserId(toUserId);
		if (friendMapper.isFriends(po)) {
			throw new ServiceException("对方已经是你的好友");
		}
		friendMapper.insert(po);
		Integer id = po.getId();
		//将添加好友的消息发送给对方
		ServerAddFriendMessage msg = new ServerAddFriendMessage();
		msg.setFromUserId(fromUserId);
		msg.setRoutId(toUserId);
		msg.setName(fromUser.getName());
		msg.setSex(fromUser.getSex());
		String content = "小瓜皮，我们是朋友了，来聊天吧!";
		ServerImMessage tofromImMessage = messageService.getTextMessage(fromUserId, GTypeEnum.friend.getIndex(), toUserId, content);
		ServerImMessage toIdImMessage = messageService.getTextMessage(fromUserId, GTypeEnum.friend.getIndex(), fromUserId, content);
		try {
			//发一条添加好友消息
			msg.setRoutId(toUserId);
			ImServer.addMessageToQueue(msg);
			//给发起者发送
			tofromImMessage.setRoutId(fromUserId);
			ImServer.addMessageToQueue(tofromImMessage);
			toIdImMessage.setRoutId(toUserId);
			ImServer.addMessageToQueue(toIdImMessage);
		} catch (IllegalStateException e) {
			throw new ServiceException("服务器繁忙");
		}
		return id;
	}
	
	public List<User> getFriends(int userId) {
		List<Friend> friendList = friendMapper.getFriends(userId);
		List<Integer> uidList = new ArrayList<>(friendList.size());
		for (Friend friend : friendList) {
			if (friend.getFromUserId()!=userId) {
				uidList.add(friend.getFromUserId());
			}
			if (friend.getToUserId()!=userId) {
				uidList.add(friend.getToUserId());
			}
		}
		return userService.getUserList(uidList);
	}
	
	public boolean isFriends(int fromUserId,int toUserId){
		Friend po = new Friend();
		po.setFromUserId(fromUserId);
		po.setToUserId(toUserId);
		return friendMapper.isFriends(po);
	}
}
