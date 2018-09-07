package com.sherlock.imService.utils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ReqUtil {
	public static final String userId = "userId";
	
	public static int getUserId() {
	    Object userIdStr = getRequest().getAttribute(userId);
        if (userIdStr==null) {
            return 0;
        }
        return (Integer)userIdStr;
	}
	public static void setUserId(HttpServletRequest request,int userId){
		request.setAttribute(ReqUtil.userId, userId);
	}
	
	private static  HttpServletRequest getRequest() {
		if (RequestContextHolder.getRequestAttributes() == null) {
    		return null;
    	}
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}
}
