package com.sherlock.imService.mq;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
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
	
	private CountDownLatch latch = new CountDownLatch(1);
	
	@PostConstruct
	void init(){
		connection = getConnection();
		latch.countDown();
	}
	public void waitForInitial(){
		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error("latch等待过程中出现了中断动作");
		}
	}
	@PreDestroy
	void detory() {
		try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Channel getChannel(){
		Channel c = null;
		try {
			c = connection.createChannel();
		} catch (IOException e) {
			logger.error("无法创建MQ channel",e);
		}
		return c;
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

}
