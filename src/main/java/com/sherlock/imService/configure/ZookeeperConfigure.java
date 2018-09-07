package com.sherlock.imService.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:zk.properties")
public class ZookeeperConfigure {

	@Value("${session_timeout}")
	private int SESSION_TIMEOUT;
	
	@Value("${base_path}")
	private String BASE_PATH;
	
	@Value("${service_name}")
	private String SERVICE;
	
	@Value("${socket_port}")
	private int SOCKET_PORT;
	
	//客户端连接的地址
	@Value("${imAddress_out}")
	private String imAddress_out;
	
	//内部调用地址
	@Value("${imAddress_inner}")
	private String imAddress_inner;
	
//	public static final String ZK_REGISTRY_PATH = ZK_BASE_PATH;
//	public static final String registryAddress = null;
	
	//host1:port1,host2:port2,host3:port3
	@Value("${connectingString}")
	private String connectingString;

	
	public int getSESSION_TIMEOUT() {
		return SESSION_TIMEOUT;
	}

	public String getBASE_PATH() {
		return BASE_PATH;
	}

	public String getSERVICE() {
		return SERVICE;
	}

	public String getConnectingString() {
		return connectingString;
	}

	public String getImAddress_out() {
		return imAddress_out;
	}

	public String getImAddress_inner() {
		return imAddress_inner;
	}

	public int getSOCKET_PORT() {
		return SOCKET_PORT;
	}

}
