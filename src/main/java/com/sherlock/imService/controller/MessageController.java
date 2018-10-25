package com.sherlock.imService.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.sherlock.imService.common.Result;
import com.sherlock.imService.entity.param.MessageParam;
import com.sherlock.imService.entity.vo.UnreadVO;
import com.sherlock.imService.service.MessageService;

@RestController
@RequestMapping("/message")
public class MessageController {

	@Autowired
	private MessageService messageService;
	
	@RequestMapping(value="/sendMessage", method={RequestMethod.POST})
	public Result sendMessage(MessageParam param){
		return Result.success(messageService.sendMessage(param));
	}
	
	@RequestMapping(value="/getUnreadCountMap", method={RequestMethod.GET})
	public Result getUnreadCountMap(@RequestParam Integer userId) {
		return Result.success(messageService.getUnreadCountMap(userId));
	}
	@RequestMapping(value="/deleteUnreadCountMap", method={RequestMethod.POST})
	public Result deleteUnreadCountMap(@RequestParam Integer userId, String map) {
		JSONObject job = JSONObject.parseObject(map);
		Set<Map.Entry<String, Object>> entrySet = job.entrySet();
		Map<String, UnreadVO> map1 = new HashMap<>();
		for (Map.Entry<String, Object> entry : entrySet) {
			UnreadVO unreadVO = JSONObject.toJavaObject((JSONObject)entry.getValue(), UnreadVO.class);
			if(unreadVO.getCount()>0) {
				map1.put(entry.getKey(), unreadVO);
			}
		}
		messageService.deleteUnreadCountMap(userId, map1);
		return Result.success("");
	}
	/**
	 * 获取离线IM消息
	 * @param userId
	 * @param gtype
	 * @param gid
	 * @param lastMsgTime
	 * @return
	 */
	@RequestMapping(value="/getConversationOfflineMessage", method={RequestMethod.GET})
	public Result getConversationOfflineMessage(@RequestParam Integer userId,@RequestParam Integer gtype,@RequestParam Integer gid, Long lastMsgTime) {
		return Result.success(messageService.getConversationOfflineMessage(userId, gtype, gid,lastMsgTime));
	}
	/**
	 * 删除离线IM消息
	 * @param userId
	 * @param gtype
	 * @param gid
	 * @param lastMsgTime
	 * @return
	 */
	@RequestMapping(value="/deleteConversationOfflineMessage", method={RequestMethod.POST})
	public Result deleteConversationOfflineMessage(@RequestParam Integer userId,@RequestParam Integer gtype,@RequestParam Integer gid,@RequestParam Long lastMsgTime) {
		messageService.deleteConversationOfflineMessage(userId, gtype, gid,lastMsgTime);
		return Result.success("删除成功");
	}
	
	/**
	 * 获取离线IM消息
	 * @param userId
	 * @param gtype
	 * @param gid
	 * @param lastMsgTime
	 * @return
	 */
	@RequestMapping(value="/getOfflineOrderMessage", method={RequestMethod.GET})
	public Result getOfflineOrderMessage(@RequestParam int userId) {
		return Result.success(messageService.getOfflineOrderMessage(userId));
	}
	/**
	 * 删除离线IM消息
	 * @param userId
	 * @param gtype
	 * @param gid
	 * @param lastMsgTime
	 * @return
	 */
	@RequestMapping(value="/delOfflineOrderMessage", method={RequestMethod.POST})
	public Result delOfflineOrderMessage(@RequestParam int userId, @RequestParam Long lastMsgTime) {
		messageService.delOfflineOrderMessage(userId, lastMsgTime);
		return Result.success("删除成功");
	}
}
