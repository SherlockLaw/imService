package com.sherlock.imService.netty.entity;

import com.sherlock.imService.netty.configure.Configure;
/**
 * 会话组离线指令
 * @author Administrator
 *
 */
public abstract class ConversationOrderMessage extends ConversationOfflineMessage{

	public ConversationOrderMessage(){
		this.msgType = Configure.MSG_CONVERSATION_ORDER;
	}	
}
