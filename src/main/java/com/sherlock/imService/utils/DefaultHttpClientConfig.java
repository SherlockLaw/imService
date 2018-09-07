package com.sherlock.imService.utils;

public class DefaultHttpClientConfig {
	/**
	 * 单位毫秒
	 * @return #连接不够用的时候等待超时时间
	 */
	public int getConnectionRequestTimeout()
	{
		return 2000;
	}
	
	/**
	 * 连接超时时间
	 * @return
	 */
	public int getConnectionTimeout()
	{
		return 5000;
	}
	
	/**
	 * 默认请求响应时间
	 * @return
	 */
	public int getDefaultSocketTimeout()
	{
		return 5000;
	}
	
	/**
	 * 每个域名的连接数的最大值(所有域名的最大连接数加起来不超过MaxTotal)
	 * @return
	 */
	public int getDefaultMaxPerRoute()
	{
		return 500;
	}
	
	/**
	 * 整个连接池的最大大小
	 * @return
	 */
	public int getMaxTotal()
	{
		return 5000;
	}
}
