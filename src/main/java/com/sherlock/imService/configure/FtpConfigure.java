package com.sherlock.imService.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:ftp.properties")
public class FtpConfigure {
	/**
     * 获取ip地址 
     */
    @Value("${FTP_ADDRESS}")
    private String FTP_ADDRESS;
      
    /**
     * 端口号
     */
    @Value("${FTP_PORT}")
    private String FTP_PORT;
      
    /**
     * 用户名
     */
    @Value("${FTP_USERNAME}")
    private String FTP_USERNAME;
      
    /**
     * 密码
     */
    @Value("${FTP_PASSWORD}")
    private String FTP_PASSWORD;
    
    /**基本路径
     */
    @Value("${FTP_BASEPATH}")
    private String FTP_BASEPATH;
      
    /**
     * 下载地址地基础url
     */
    @Value("${IMAGE_BASE_URL}")
    private String IMAGE_BASE_URL;

	public String getFTP_ADDRESS() {
		return FTP_ADDRESS;
	}

	public void setFTP_ADDRESS(String fTP_ADDRESS) {
		FTP_ADDRESS = fTP_ADDRESS;
	}

	public String getFTP_PORT() {
		return FTP_PORT;
	}

	public void setFTP_PORT(String fTP_PORT) {
		FTP_PORT = fTP_PORT;
	}

	public String getFTP_USERNAME() {
		return FTP_USERNAME;
	}

	public void setFTP_USERNAME(String fTP_USERNAME) {
		FTP_USERNAME = fTP_USERNAME;
	}

	public String getFTP_PASSWORD() {
		return FTP_PASSWORD;
	}

	public void setFTP_PASSWORD(String fTP_PASSWORD) {
		FTP_PASSWORD = fTP_PASSWORD;
	}

	public String getFTP_BASEPATH() {
		return FTP_BASEPATH;
	}

	public void setFTP_BASEPATH(String fTP_BASEPATH) {
		FTP_BASEPATH = fTP_BASEPATH;
	}

	public String getIMAGE_BASE_URL() {
		return IMAGE_BASE_URL;
	}

	public void setIMAGE_BASE_URL(String iMAGE_BASE_URL) {
		IMAGE_BASE_URL = iMAGE_BASE_URL;
	}
}
