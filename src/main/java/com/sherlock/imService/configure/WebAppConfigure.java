package com.sherlock.imService.configure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.sherlock.imService.interceptor.LoginInterceptor;
import com.sherlock.imService.interceptor.NeedTokenInterceptor;

@Configuration
public class WebAppConfigure extends WebMvcConfigurerAdapter{
	@Autowired
	private LoginInterceptor loginInterceptor;
	
	@Autowired
	private NeedTokenInterceptor needTokenInterceptor;
	
	@Autowired
	private InterceptorConfig interceptorConfig;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(loginInterceptor).addPathPatterns("/**");
		registry.addInterceptor(needTokenInterceptor).addPathPatterns("/**")
				.excludePathPatterns("/inner/**")
				.excludePathPatterns(interceptorConfig.getGuestInclude());
		super.addInterceptors(registry);
	}
}
