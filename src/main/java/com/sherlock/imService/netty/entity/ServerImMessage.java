package com.sherlock.imService.netty.entity;

import com.sherlock.imService.netty.configure.Configure;

/***
 * 客户端需要显示的消息
 * @author Administrator
 *
 */
public abstract class ServerImMessage extends ConversationOfflineMessage implements Cloneable{
	
	public ServerImMessage(){
		this.msgType = Configure.MSG_IM;
	}
	
	@Override  
    public ServerImMessage clone() {  
		ServerImMessage stu = null;  
        try{  
            stu = (ServerImMessage)super.clone();  
        }catch(CloneNotSupportedException e) {  
            e.printStackTrace();  
        }  
        return stu;  
    }
	public String getLastMsg(){
		if (this instanceof ServerTextMessage) {
			ServerTextMessage msg = (ServerTextMessage) this;
			return msg.getContent();
		}
		if (this instanceof ServerPicMessage) {
			return "[图片]";
		}
		if (this instanceof ServerRetractMessage) {
			ServerRetractMessage msg = (ServerRetractMessage) this;
			return "[消息撤回]";
		}
		return null;
	}
	
	

}
