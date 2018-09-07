package com.sherlock.imService.remote;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.sherlock.imService.utils.HttpHelper;

@Component
public class IMMessageManager {

	private String bUrl="/inner/message";
	
	public boolean routMessage(String serviceInnerAddr, String jsonMessage){
		StringBuilder sb = new StringBuilder();
		sb.append("http://").append(serviceInnerAddr)
			.append(bUrl).append("/routMessage");
		String url = sb.toString();
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("jsonMessage", jsonMessage);
		String respons = HttpHelper.post(url, paramMap);
		boolean result = (boolean) HttpHelper.convertResult(respons);
		return result;
	}
	
}
