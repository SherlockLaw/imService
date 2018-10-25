package com.sherlock.imService.mq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sherlock.imService.configure.MQConfigure;

@Component
public class MQConnectionUtil {
	private Logger logger = LoggerFactory.getLogger(MQConnectionUtil.class);

	@Autowired
	private MQConfigure mqConfigure;
	
	private Connection connection;
	
	@PreDestroy
	void detory() {
		if (connection == null || !connection.isOpen()) {
			return;
		}
		try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Connection getConnection() {
		//创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ相关信息
        factory.setHost(mqConfigure.getHost());
        factory.setUsername(mqConfigure.getUserName());
      	factory.setPassword(mqConfigure.getPassword());
      	factory.setPort(mqConfigure.getPort());
        //创建一个新的连接
        Connection connection = null;
		try {
			connection = factory.newConnection();
		} catch (IOException | TimeoutException e) {
			logger.error("连接MQ出现了问题",e);
		}
        return connection;
	}
	
	public Channel getChannel(){
		//获取连接
		if (connection==null || !connection.isOpen()) {
			connection = getConnection();
		}
		Channel c = null;
		try {
			c = connection.createChannel();
		} catch (IOException e) {
			logger.error("无法创建MQ channel",e);
		}
		return c;
	}
}
