package com.sherlock.imService.interceptor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoginInterceptor implements HandlerInterceptor{
	private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("请求：" + request.getRequestURI());
		Map<String, String[]> paramMap = request.getParameterMap();
		if (!paramMap.isEmpty()) {
			sb.append("?");
		}
		for (String key : paramMap.keySet()) {
			sb.append(key).append("=").append(paramMap.get(key)[0]).append("&");
		}
		sb.append("\n");
		sb.append("Content-Type：" + request.getContentType()).append("\n");

		sb.append("User-Agent：" + request.getHeader("User-Agent")).append("\n");
		sb.append("**************************************************").append("\n");
		logger.info(sb.toString());
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		
	}
}
