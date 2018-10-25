package com.sherlock.imService.netty.entity;

import com.sherlock.imService.netty.configure.Configure;

/***
 * 客户端需要显示的消息
 * @author Administrator
 *
 */
public abstract class ImMessage extends ConversationOfflineMessage implements Cloneable{
	
	public ImMessage(){
		this.msgType = Configure.MSG_IM;
	}
	
	@Override  
    public ImMessage clone() {  
		ImMessage stu = null;  
        try{  
            stu = (ImMessage)super.clone();  
        }catch(CloneNotSupportedException e) {  
            e.printStackTrace();  
        }  
        return stu;  
    }
}
