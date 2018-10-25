package com.sherlock.imService.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sherlock.imService.constant.MessageConstant.AddFriendConfirmStatusEnum;
import com.sherlock.imService.constant.MessageConstant.GTypeEnum;
import com.sherlock.imService.dao.FriendMapper;
import com.sherlock.imService.dao.FriendReqMapper;
import com.sherlock.imService.entity.po.Friend;
import com.sherlock.imService.entity.po.FriendReq;
import com.sherlock.imService.entity.po.User;
import com.sherlock.imService.entity.vo.FriendReqVO;
import com.sherlock.imService.exception.ServiceException;
import com.sherlock.imService.netty.ImServer;
import com.sherlock.imService.netty.entity.AddFriendConfirmMessage;
import com.sherlock.imService.netty.entity.AddFriendRequestMessage;
import com.sherlock.imService.netty.entity.DeleteFriendMessage;
import com.sherlock.imService.netty.entity.ImMessage;
import com.sherlock.imService.utils.ListUtil;

@Service
public class FriendService {

	@Autowired
	private FriendMapper friendMapper;
	
	@Autowired
	private FriendReqMapper friendReqMapper;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private MessageService messageService;
	
	public void addFriendRequest(int fromUserId,int toUserId) {
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
		FriendReq po = new FriendReq();
		po.setFromUserId(fromUserId);
		po.setToUserId(toUserId);
		po.setStatus(AddFriendConfirmStatusEnum.unverified.getIndex());
		if (friendMapper.isFriends(po)) {
			throw new ServiceException("对方已经是你的好友");
		}
		
		FriendReq srcPo = friendReqMapper.getByUserIds(po);
		if (srcPo!=null){
			if (srcPo.getStatus()==AddFriendConfirmStatusEnum.unverified.getIndex()) {
				throw new ServiceException("已有请求消息，请处理或等待回复");
			} 
//			else if (srcPo.getStatus()==AddFriendConfirmStatusEnum.agree.getIndex()) {
//				throw new ServiceException("双方已经是好友");
//			}
			FriendReq param = new FriendReq();
			BeanUtils.copyProperties(po, param);
			param.setId(srcPo.getId());
			//更新这条记录
			friendReqMapper.update(param);
		} else {
			//添加新纪录
			friendReqMapper.insert(po);
		}
		
		// 将添加好友的请求发送给对方
		AddFriendRequestMessage msg = new AddFriendRequestMessage();
		msg.setFromUserId(fromUserId);
		msg.setRoutId(toUserId);
		msg.setName(fromUser.getName());
		msg.setSex(fromUser.getSex());
		ImServer.addMessageToQueue(msg);
	}
	
	public void delFriend(int fromUserId,int toUserId) {
		if (fromUserId==toUserId) {
			throw new ServiceException("不能删除自己");
		}
		User fromUser = userService.getUserById(fromUserId);
		if (fromUser==null) {
			throw new ServiceException("本人用户不存在");
		}
		if (!userService.existUser(toUserId)) {
			throw new ServiceException("对方用户不存在");
		}
		FriendReq po = new FriendReq();
		po.setFromUserId(fromUserId);
		po.setToUserId(toUserId);
		int count = friendMapper.delete(po);
		if (count==0) {
			throw new ServiceException("对方不是你的好友");
		}
		//发送消息
		DeleteFriendMessage msg1 = new DeleteFriendMessage();
		msg1.setRoutId(toUserId);
		msg1.setFriendId(fromUserId);
		DeleteFriendMessage msg2 = new DeleteFriendMessage();
		msg2.setRoutId(fromUserId);
		msg2.setFriendId(toUserId);
		ImServer.addMessageToQueue(msg1);
		ImServer.addMessageToQueue(msg2);
	}
	public List<User> getFriends(int userId) {
		List<Friend> poList = friendMapper.getFriends(userId);
		List<Integer> uidList = new ArrayList<>(poList.size());
		for (Friend friend : poList) {
			if (friend.getFromUserId()!=userId) {
				uidList.add(friend.getFromUserId());
			}
			if (friend.getToUserId()!=userId) {
				uidList.add(friend.getToUserId());
			}
		}
		return userService.getUserList(uidList);
	}
	
	public List<FriendReqVO> getFriendReqs(int userId) {
		List<FriendReq> poList = friendReqMapper.getFriendReqs(userId);
		List<Integer> uidList = new ArrayList<>(poList.size());
		List<FriendReqVO> voList = new ArrayList<>(poList.size());
		for (FriendReq po : poList) {
			Integer uId = po.getFromUserId()==userId ? po.getToUserId() : po.getFromUserId();
			uidList.add(uId);
			FriendReqVO vo = new FriendReqVO();
			vo.setId(po.getId());
			vo.setFromUserId(po.getFromUserId());
			vo.setUserId(uId);
			vo.setStatus(po.getStatus());
			voList.add(vo);
		}
		Map<Integer, User> userMap = ListUtil.list2Map2(userService.getUserList(uidList), "id", User.class);
		for (FriendReqVO vo : voList) {
			User user = userMap.get(vo.getUserId());
			if (user!=null) {
				vo.setHeadPic(user.getHeadPic());
				vo.setName(user.getName());
				vo.setSex(user.getSex());
			}
		}
		return voList;
	}
	
	public boolean isFriends(int fromUserId,int toUserId){
		Friend po = new Friend();
		po.setFromUserId(fromUserId);
		po.setToUserId(toUserId);
		return friendMapper.isFriends(po);
	}

	public boolean confirmFriendRequest(int id, int status) {
		AddFriendConfirmStatusEnum e = AddFriendConfirmStatusEnum.getEnum(status);
		if (e==null || e==AddFriendConfirmStatusEnum.unverified) {
			throw new ServiceException("提交状态不正确");
		}
		{
			FriendReq param = new FriendReq();
			param.setId(id);
			param.setStatus(status);
			friendReqMapper.update(param);
		}
		FriendReq po = friendReqMapper.get(id);
		//发送好友的确认信息
		AddFriendConfirmMessage fromMsg = getCommonAddFriendConfirmMessage(po.getFromUserId(), id, status);
		AddFriendConfirmMessage toMsg = getCommonAddFriendConfirmMessage(po.getToUserId(),id, status);
		if (e==AddFriendConfirmStatusEnum.agree) {
			addFriendDBHandler(po.getFromUserId(),po.getToUserId());
			//设置用户信息
			fromMsg.setUserInfo(userService.getUserById(po.getToUserId()));
			toMsg.setUserInfo(userService.getUserById(po.getFromUserId()));
			//发送第一条IM消息
			sendAddFriendMsg(po.getFromUserId(),po.getToUserId());
		}
		ImServer.addMessageToQueue(fromMsg);
		ImServer.addMessageToQueue(toMsg);
		return true;
	}
	private AddFriendConfirmMessage getCommonAddFriendConfirmMessage(int routId,int addFriendReqId,int status){
		AddFriendConfirmMessage msg = new AddFriendConfirmMessage();
		msg.setRoutId(routId);
		msg.setAddFriendReqId(addFriendReqId);
		msg.setStatus(status);
		return msg;
	}
	private Integer addFriendDBHandler(int fromUserId,int toUserId){
		Friend po = new Friend();
		po.setFromUserId(fromUserId);
		po.setToUserId(toUserId);
		if (friendMapper.isFriends(po)) {
			throw new ServiceException("对方已经是你的好友");
		}
		friendMapper.insert(po);
		Integer id = po.getId();
		return id;
	}
	public void sendAddFriendMsg(int fromUserId,int toUserId) {
		//发送IM即时消息
		String content = "小瓜皮，我们是朋友了，来聊天吧!";
		ImMessage tofromImMessage = messageService.getTextMessage(toUserId, GTypeEnum.friend.getIndex(), toUserId, content);
		ImMessage toImMessage = messageService.getTextMessage(toUserId, GTypeEnum.friend.getIndex(), fromUserId, content);
		try {
			//给发起者发送
			tofromImMessage.setRoutId(fromUserId);
			ImServer.addMessageToQueue(tofromImMessage);
			toImMessage.setRoutId(toUserId);
			ImServer.addMessageToQueue(toImMessage);
		} catch (IllegalStateException e) {
			throw new ServiceException("服务器繁忙");
		}
	}
}
