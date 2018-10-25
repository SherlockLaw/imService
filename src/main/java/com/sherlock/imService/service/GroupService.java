package com.sherlock.imService.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sherlock.imService.dao.GroupMapper;
import com.sherlock.imService.entity.po.Group;
import com.sherlock.imService.entity.po.User;
import com.sherlock.imService.entity.vo.GroupVO;
import com.sherlock.imService.exception.ServiceException;
import com.sherlock.imService.netty.ImServer;
import com.sherlock.imService.netty.entity.AddGroupMessage;

@Service
public class GroupService {
	@Autowired
	private GroupMapper groupMapper;
	
	@Autowired
	private GroupMemService groupMemService;
	
	@Autowired
	private FileService fileService;
	
	public GroupVO getById(int id){
		Group po = groupMapper.getById(id);
		if (po == null) {
			return null;
		}
		GroupVO vo = new GroupVO();
		BeanUtils.copyProperties(po, vo);
		List<User> userList = groupMemService.getGroupMem(po.getId());
		vo.setMemberList(userList);
		return vo;
	}
	
	public List<GroupVO> getGroups(int userId) {
		List<Group> poList = groupMapper.getGroups(userId);
		List<GroupVO> voList = new ArrayList<>(poList.size());
		for (Group po : poList) {
			GroupVO vo = new GroupVO();
			BeanUtils.copyProperties(po, vo);
			List<Integer> memberIds = groupMemService.getGroupMemIds(po.getId());
			vo.setMemberIds(memberIds);
			voList.add(vo);
		}
		return voList;
	}
	
	@Transactional
	public Integer addGroup(String name,MultipartFile headImage, int creatorId,int[] memberIds) {
		if (headImage.isEmpty()) {
			throw new ServiceException("请添加头像");
		}
		if (memberIds.length==0) {
			throw new ServiceException("成员不能为空");
		}
		Group po = new Group();
		po.setName(name);
		po.setHeadPic(fileService.genHeadPic(headImage));
		po.setCreatorId(creatorId);
		groupMapper.insert(po);
		
		List<Integer> memberIdList = new ArrayList<>(memberIds.length+1);
		for (int i = 0; i < memberIds.length; i++) {
			memberIdList.add(memberIds[i]);
		}
		memberIdList.add(creatorId);
		int groupId = po.getId();
		for (Integer memberId: memberIdList) {
			groupMemService.insert(groupId, memberId);
			AddGroupMessage msg = new AddGroupMessage();
			msg.setGroupId(groupId);
			msg.setRoutId(memberId);
			try {
				ImServer.addMessageToQueue(msg);
			} catch (IllegalStateException e) {
				throw new ServiceException("服务器繁忙");
			}
		}
		return po.getId();
	}
}
