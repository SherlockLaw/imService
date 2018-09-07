package com.sherlock.imService.configure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix="interceptor")
@PropertySource(value = "classpath:interceptor.properties")
public class InterceptorConfig {
    
    private String[] guestInclude;

	public String[] getGuestInclude() {
		return guestInclude;
	}

	public void setGuestInclude(String[] guestInclude) {
		this.guestInclude = guestInclude;
	}
}
