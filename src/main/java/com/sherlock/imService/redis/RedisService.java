package com.sherlock.imService.redis;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.sherlock.imService.constant.RedisConstant;
import com.sherlock.imService.entity.vo.UnreadVO;
import com.sherlock.imService.entity.vo.UserVO;
import com.sherlock.imService.netty.configure.Configure;
import com.sherlock.imService.netty.entity.ConversationOfflineMessage;
import com.sherlock.imService.netty.entity.ServerImMessage;

@Service
public class RedisService {
	private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	
	public void clearMessage(){
		//分布式锁 
		String lockKey = RedisConstant.KEY_CLEARMSG;
		//为防止服务器之间的微小时间差导致多个服务可以同时执行任务，采取
		Boolean rel = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey);
		if (rel) {
			String label = UUID.randomUUID().toString();
			logger.info("清除过期消息，获得锁:"+label);
			long startTime = System.currentTimeMillis();
			redisTemplate.expire(lockKey, RedisConstant.CLEARMSG_EXPIRE_TIME, TimeUnit.MINUTES);
			String keyPattern =genMsgKey("*","*","*");
			long maxTime = System.currentTimeMillis() - RedisConstant.KEEP_MESSAGE_DAY * 24 * 3600 * 1000;
			Set<String> keySet = redisTemplate.keys(keyPattern);
			for (String key : keySet) {
				redisTemplate.opsForZSet().removeRangeByScore(key, 0d, maxTime);
			}
			long endTime = System.currentTimeMillis();
			logger.info("清除过期消息结束:"+label + "|| 执行时长:"+(endTime-startTime)/1000f +"秒");
			
			redisTemplate.delete(lockKey);
		}
		
	}
	
	/**
	 * 设置用户token
	 * @param user
	 * @return
	 */
	public boolean setToken(UserVO userVO) {
		String token = genToken();
		userVO.setToken(token);
		String tokenKey = token2Key(token);
		String uidKey = userId2Key(userVO.getId());
		//先将用户的token删除
		delTokenByUserId(userVO.getId());
		userVO.setLastRefreshTime(System.currentTimeMillis());
		//设置token-用户
		redisTemplate.opsForValue().set(tokenKey, userVO);
		//设置userId-token
		redisTemplate.opsForValue().set(uidKey, token);
		//设置超时时间
		redisTemplate.expire(tokenKey, RedisConstant.TOKEN_EXPIRE_TIME, TimeUnit.HOURS);
		redisTemplate.expire(uidKey, RedisConstant.TOKEN_EXPIRE_TIME, TimeUnit.HOURS);
		return true;
	}
	public boolean clearTokenAndUserInfo(int userId) {
		delTokenByUserId(userId);
		delUserByUserId(userId);
		return true;
	}
	public void resetTokenTime(String token){
		String tokenKey = token2Key(token);
		UserVO userVO = (UserVO) redisTemplate.opsForValue().get(tokenKey);
		long curTime = System.currentTimeMillis();
		if(userVO.getLastRefreshTime()+RedisConstant.TOKEN_NEED_REFRESH < curTime){
			logger.info(userVO.getId()+"token延时设置");
			String uidKey = userId2Key(userVO.getId());
			redisTemplate.expire(tokenKey, RedisConstant.TOKEN_EXPIRE_TIME, TimeUnit.HOURS);
			redisTemplate.expire(uidKey, RedisConstant.TOKEN_EXPIRE_TIME, TimeUnit.HOURS);
			userVO.setLastRefreshTime(curTime);
			redisTemplate.opsForValue().set(tokenKey, userVO);
			logger.info(userVO.getId()+"token延时设置完成");
		}
	}
	
	private String genToken(){
		return UUID.randomUUID().toString().replace("-", "");
	}
	/**
	 * 校验token有效性
	 * @param token
	 * @return
	 */
	public boolean checkToken(String token) {
		if (StringUtils.isBlank(token)) {
			return false;
		}
		return redisTemplate.hasKey(token);
	}
	/**
	 * 根据token获取用户
	 * @param token
	 * @return
	 */
	public UserVO getUserByToken(String token) {
		String tokenKey = token2Key(token);
		UserVO userVO = (UserVO) redisTemplate.opsForValue().get(tokenKey);
		return userVO;
	}
	
	/**
	 * 把userId转换为redis中存储用户信息的key
	 * @param userId
	 * @return
	 */
	private String userId2Key(int userId){
		String key = RedisConstant.PREFIX_USERID+userId;
		return key;
	}
	/**
	 * 把token转换为rendis中的key
	 * @param token
	 * @return
	 */
	private String token2Key(String token){
		String key = RedisConstant.PREFIX_TOKEN+token;
		return key;
	}
	
	private String getTokenByUserId(int userId){
		String token = (String) redisTemplate.opsForValue().get(userId2Key(userId));
		return token;
	}
	
	private void delTokenByUserId(int userId){
		String token = getTokenByUserId(userId);
		if (token!=null) {
			redisTemplate.delete(token2Key(token));
		}
	}
	private void delUserByUserId(int userId){
		String uidKey = userId2Key(userId);
		if (uidKey!=null) {
			redisTemplate.delete(uidKey);
		}
	}
	/**
	 * 保存离线消息
	 * @param msg
	 */
	public void saveConversationOfflineMessage(ConversationOfflineMessage msg){	
		//保存未读
		redisTemplate.opsForZSet().add(
				genMsgKey(msg.getRoutId(),msg.getGtype(),msg.getGid()), msg, msg.getTime());
	}
	public void addUnreadCount(ServerImMessage msg) {
		//未读数加一
//		redisTemplate.opsForValue().increment(genUnreadKey(msg.getToId()), 1);
		redisTemplate.opsForHash()
		.increment(genConversationUnreadKey(msg.getRoutId()),
				///获取会话的hash
				genConversationUnreadHashString(msg.getGtype(),msg.getGid()), 1);
	}
	
	/**
	 * 保存群组消息
	 * @param msg
	 */
	public void saveGroupMessage(ServerImMessage msg){
		String key = genGroupMsgKey(msg.getGid());
		redisTemplate.opsForZSet().add(key, msg,msg.getTime());
	}
//	/**
//	 * 保存消息确认消息
//	 * @param msg
//	 */
//	public void saveOrderMessage(ConversationOrderMessage msg){
//		String key = genOrderMsgKey(msg.getRoutId());
//		redisTemplate.opsForSet().add(key, msg);
//	}
//	public List<ConversationOrderMessage> getOrderMessage(int routId, int gtype, int gid,long lastMsgTime){
//		String key = genOrderMsgKey(routId);
//		Set<Object> msgSet =redisTemplate.opsForSet().members(key);
//		Iterator<Object> it = msgSet.iterator();
//		List<ConversationOrderMessage> list = new ArrayList<>(msgSet.size());
//		while (it.hasNext()) {
//			ConversationOrderMessage msg = (ConversationOrderMessage) it.next();
//			list.add(msg);
//		}
//		redisTemplate.delete(key);
//		return list;
//	}
//	public void deleteOrderMessage(int routId, int gtype, int gid,long lastMsgTime){
//		String key = genOrderMsgKey(routId);
//		redisTemplate.opsForZSet().removeRangeByScore(key, 0, lastMsgTime);
//	}
	/**
	 * 拉取群组离线消息
	 * @param gid
	 * @param lastMsgTime
	 * @return
	 */
	public List<ConversationOfflineMessage> getGroupMessage(int gid,long lastMsgTime){
		String key = genGroupMsgKey(gid);
		Set<Object> msgSet =redisTemplate.opsForZSet().rangeByScore(key, lastMsgTime, Long.MAX_VALUE);
		List<ConversationOfflineMessage> list = OfflineMsgFromSetToList(msgSet);
		return list;
	}
	private String genGroupMsgKey(int gid){
		StringBuilder sb = new StringBuilder();
		sb.append(RedisConstant.PREFIX_MSG_GROUP).append(gid);
		String key = sb.toString();
		return key;
	}
	private String genOrderMsgKey(int routId){
		StringBuilder sb = new StringBuilder();
		sb.append(RedisConstant.PREFIX_MSG_ORDER).append(routId);
		String key = sb.toString();
		return key;
	}
	//离线消息key
	private String genMsgKey(int routId, int gtype, int gid){
		StringBuilder sb = new StringBuilder();
		sb.append(RedisConstant.PREFIX_MSG_FIRST).append(routId)
		.append(RedisConstant.PREFIX_MSG_SECOND).append(gtype)
		.append(RedisConstant.PREFIX_MSG_THIRD).append(gid);
		String key = sb.toString();
		return key;
	}
	private String genMsgKey(String routId, String gtype, String gid){
		StringBuilder sb = new StringBuilder();
		sb.append(RedisConstant.PREFIX_MSG_FIRST).append(routId)
		.append(RedisConstant.PREFIX_MSG_SECOND).append(gtype)
		.append(RedisConstant.PREFIX_MSG_THIRD).append(gid);
		String key = sb.toString();
		return key;
	}
//	//个人未读数key
//	private String genUnreadKey(int userId){
//		String key = RedisConstant.PREFIX_UNREAD+userId;
//		return key;
//	}
	//每个会话未读数key
	private String genConversationUnreadKey(int routId){
		StringBuilder sb = new StringBuilder();
		sb.append(RedisConstant.PREFIX_CONVERSION_UNREAD).append(routId);
		String key = sb.toString();
		return key;
	}
	
	private String genConversationUnreadHashString(int gtype, int gid){
		StringBuilder sb = new StringBuilder();
		sb.append(gtype).append(RedisConstant.CONVERSION_UNREAD_HASH_SEPERATOR).append(gid);
		String key = sb.toString();
		return key;
	}
	//
//	/**
//	 * 返回用户未读数
//	 * @param userId
//	 * @return
//	 */
//	public int getUnreadCount(int userId){
//		String key = genUnreadKey(userId);
//		String str = (String) redisTemplate.opsForValue().get(key);
//		int unreadCount = 0;
//		if (str!=null) {
//			unreadCount = Integer.valueOf(str);
//		}
//		return unreadCount;
//	}
	/**
	 * 获取用户每个会话的未读数
	 * @param userId
	 * @return
	 */
	public Map<String,UnreadVO> getUnreadCountMap(int routId) {
		Map<Object,Object> conUnreadMap;
		String unreadKey = genConversationUnreadKey(routId);
		if (!redisTemplate.hasKey(unreadKey)) {
			return new HashMap<>();
		}
		conUnreadMap = redisTemplate.opsForHash().entries(unreadKey);
		Set<Map.Entry<Object, Object>> entrySet = conUnreadMap.entrySet();
		Map<String,UnreadVO> map = new HashMap<>(entrySet.size());
		for (Map.Entry<Object, Object> entry : entrySet) {
			String key = entry.getKey().toString();
			Integer value = (Integer) entry.getValue();
			UnreadVO vo = new UnreadVO();
			vo.setCount(value);
			String[] v = key.split("_");
			int gtype = Integer.valueOf(v[0]);
			int gid = Integer.valueOf(v[1]);
			//获取最后的未读消息，写入最后一条消息内容，最后接收消息时间
			// TODO: 考虑存储最后一条消息
			String msgKey = genMsgKey(routId, gtype, gid);
			long count = redisTemplate.opsForZSet().zCard(msgKey);
			Set<Object> msgSet = redisTemplate.opsForZSet().range(msgKey, count-1, count);
			Iterator<Object> it = msgSet.iterator();
			if (it.hasNext()) {
				ServerImMessage msg = (ServerImMessage) it.next();
				vo.setLastMsg(msg.getLastMsg());
				vo.setLastTime(msg.getTime());
			}
			map.put(key, vo);
		}
		return map;
	}
	public void deleteUnreadCountMap(int routId, Map<String, UnreadVO> map) {
		String unreadKey = genConversationUnreadKey(routId);
		Set<Map.Entry<String, UnreadVO>> entrySet = map.entrySet();
		for (Map.Entry<String, UnreadVO> entry : entrySet) {
			UnreadVO unreadVO = entry.getValue();
			redisTemplate.watch(unreadKey);
			Integer count = (Integer) redisTemplate.opsForHash().get(unreadKey, entry.getKey());
			redisTemplate.multi();
			if (count==unreadVO.getCount().intValue()) {
				redisTemplate.opsForHash().delete(unreadKey, entry.getKey());
			} else {
				redisTemplate.opsForHash().increment(unreadKey, entry.getKey(), -unreadVO.getCount());
			}
			redisTemplate.exec();
		}
	}
	/**
	 * 获取会话离线消息
	 * @return
	 */
	public List<ConversationOfflineMessage> getConversationOfflineMessage(int routId, int gtype, int gid) {
		String key = genMsgKey(routId, gtype, gid);
		Set<Object> msgSet = redisTemplate.opsForZSet().range(key, 0, Long.MAX_VALUE);
		List<ConversationOfflineMessage> list = OfflineMsgFromSetToList(msgSet);
		//为避免消息丢失，需要等待客户端确认消息已经接收到之后再做删除
//		redisTemplate.opsForZSet().removeRange(key, 0, msgSet.size());
		return list;
	}
	/**
	 * 删除上一次客户端拉取的IM离线消息（接收到客户端的ACK之后执行）
	 * @param routId
	 * @param gtype
	 * @param gid
	 * @param lastMsgTime
	 */
	public void deleteConversationOfflineMessage(int routId, int gtype, int gid,long lastMsgTime){
		String key = genMsgKey(routId, gtype, gid);
//		long count = redisTemplate.opsForZSet().count(key, 0, lastMsgTime);
		redisTemplate.opsForZSet().removeRangeByScore(key, 0, lastMsgTime);
//		// 目标会话未读数操作
//		redisTemplate.opsForHash().increment(genConversationUnreadKey(routId),
//					genConversationUnreadHashString(gtype, gid), count);
	}
	
	private List<ConversationOfflineMessage> OfflineMsgFromSetToList(Set<Object> msgSet){
		Iterator<Object> it = msgSet.iterator();
		List<ConversationOfflineMessage> list = new ArrayList<>(msgSet.size());
		while (it.hasNext()) {
			ConversationOfflineMessage msg = (ConversationOfflineMessage) it.next();
			list.add(msg);
		}
		return list;
	}
	/**
	 * 设置用户地址（UDP）
	 * @param userId
	 * @param addr
	 * @return
	 */
	public boolean setUserAddr(int userId,InetSocketAddress addr){
		String key = getUserAddrKey(userId);
		Object oldAddr =  redisTemplate.opsForValue().get(key);
		boolean isOffline = false;
		/**
		 * 两个地址不相同，表示不是同一个登陆，可能登陆了两次
		 */
		if(oldAddr==null || !addr.equals(oldAddr)){
			isOffline = true;
			redisTemplate.opsForValue().set(key, addr);
		}
		redisTemplate.expire(key, Configure.MAX_IDLETIME*Configure.MAX_HEATBEAT_COUNT_ON_CONNECT, TimeUnit.SECONDS);
		return isOffline;
		
	}
	/**
	 * 获取用户地址（UDP）
	 * @param userId
	 * @return
	 */
	public InetSocketAddress getUserAddr(int userId) {
		String key = getUserAddrKey(userId);
		InetSocketAddress addr = (InetSocketAddress) redisTemplate.opsForValue().get(key);
		return addr;
	}
	private String getUserAddrKey (int userId){
		StringBuilder sb = new StringBuilder();
		sb.append(RedisConstant.PREFIX_ADDRESS).append(userId);
		String key = sb.toString();
		return key;
	}
	
	/**
	 * 将用户所在的服务加入路由表
	 * @param uId
	 * @param serviceName
	 */
	public void addUserToRoutTable(int uId, String serviceInnerAddr){
		redisTemplate.opsForHash().put(RedisConstant.ROUT_TABLE, uId+"", serviceInnerAddr);
	}
	public void deleteUserFromRoutTable(int uId){
		redisTemplate.opsForHash().delete(RedisConstant.ROUT_TABLE, uId+"");
	}
	public String getServiceInnerAddrFromRoutTable(int uId){
		String serviceInnerAddr = (String) redisTemplate.opsForHash().get(RedisConstant.ROUT_TABLE, uId+"");
		return serviceInnerAddr;
	}
	
	/**
	 * 设置服务的连接数
	 * @param serviceName
	 */
	public void addServiceCount(String serviceName){
		redisTemplate.opsForHash().increment(RedisConstant.SERVICE_COUNT, serviceName, 1l);
	}
	public Map<String,Integer> getAllService(){
		Map<Object, Object> remap = redisTemplate.opsForHash().entries(RedisConstant.SERVICE_COUNT);
		Set<Map.Entry<Object, Object>> entrySet = remap.entrySet();
		Map<String,Integer> map = new HashMap<>(entrySet.size());
		for (Map.Entry<Object, Object> entry : entrySet) {
			String serviceName = entry.getKey().toString();
			Integer count = (Integer) entry.getValue();
			map.put(serviceName, count);
		}
		return map;
	}
	/**
	 * 服务的连接数-1
	 * @param serviceName
	 */
	public void deleteServiceCount(String serviceName){
		redisTemplate.opsForHash().increment(RedisConstant.SERVICE_COUNT, serviceName, -1l);
	}
	/**
	 * 清除挂掉的服务
	 * @param liveServices
	 */
	public void clearDeadService(List<String> liveServices){
		//分布式锁 
		String lockKey = RedisConstant.KEY_CLEAR_SERVICE;
		Boolean rel = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey);
		if (rel) {
			//一段时间内，redis只能执行一次清除动作
			redisTemplate.expire(lockKey, RedisConstant.CLEAR_SERVICE_EXPIRE_TIME, TimeUnit.SECONDS);
			Set<Object> serviceNameSet = redisTemplate.opsForHash().keys(RedisConstant.SERVICE_COUNT);
			serviceNameSet.removeAll(liveServices);
			
			if (!serviceNameSet.isEmpty()) {
				redisTemplate.opsForHash().delete(RedisConstant.SERVICE_COUNT, serviceNameSet.toArray());
			}
		}	
	}
}
