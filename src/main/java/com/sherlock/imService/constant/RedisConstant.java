package com.sherlock.imService.constant;

public class RedisConstant {
	//定期清理数据的分布式锁
	public static final String KEY_CLEARMSG = "clearMsg";
	public static final int CLEARMSG_EXPIRE_TIME = 30;//分钟
	public static final int KEEP_MESSAGE_DAY = 15;//保留离线消息的时间/天
	//token过期时间
	public static final int TOKEN_EXPIRE_TIME = 1;// /小时
	//在接收到客户端的TCP包时刷新，选择时间刷新一次
	public static final int TOKEN_NEED_REFRESH = (int) (0.5*3600*1000);// /毫秒
	
	public static final String PREFIX_TOKEN = "token:";
	public static final String PREFIX_USERID = "uId:";
	
	public static final String PREFIX_MSG_FIRST = "msg|routId:"; // 离线消息
	public static final String PREFIX_MSG_SECOND = "|gtype:";
	public static final String PREFIX_MSG_THIRD = "|gid:";
	
	public static final String PREFIX_MSG_GROUP = "msg|groupId:";
	public static final String PREFIX_MSG_ORDER = "order|routId:";
//	public static final String PREFIX_UNREAD = "unread:uId:";
	
	public static final String PREFIX_CONVERSION_UNREAD = "unread|routId:";
	
	public static final String CONVERSION_UNREAD_HASH_SEPERATOR = "_";
	
	public static final String PREFIX_ADDRESS = "addr|uId:";
	
	//路由表
	public static final String ROUT_TABLE = "routTable";
	
	//服务的连接数
	public static final String SERVICE_COUNT = "serviceCount";
	public static final String KEY_CLEAR_SERVICE = "clearService";
	public static final int CLEAR_SERVICE_EXPIRE_TIME = 10;// /秒
}
