package com.sherlock.imService.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.sherlock.imService.common.Result;
import com.sherlock.imService.entity.vo.UserVO;
import com.sherlock.imService.exception.ServiceException;
import com.sherlock.imService.redis.RedisService;
import com.sherlock.imService.utils.ReqUtil;

@Component
public class NeedTokenInterceptor implements HandlerInterceptor{

	@Autowired
	private RedisService redisService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String token = request.getHeader("token");
		if (StringUtils.isBlank(token)) {
			token = request.getParameter("token");
		}
		
		if (StringUtils.isBlank(token)) {
			throw new ServiceException(Result.TOKEN_INVALID_CODE, "需要在Header中传输token");
		}
		UserVO userVO = redisService.getUserByToken(token);
		
		if (userVO==null) {
			throw new ServiceException(Result.TOKEN_INVALID_CODE,"token失效");
		}
		//设置userId
		ReqUtil.setUserId(request, userVO.getId());
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
