package com.sherlock.imService.mq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.Confirm.SelectOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

@Component
public class MQProducer {
	private static final Logger logger = LoggerFactory.getLogger(MQProducer.class);
	
	@Autowired
	private MQConnectionUtil mqConnectionUtil;
	
	public static final String TASK_QUEUE_NAME="task_queue";
	
	private Channel channel;
	
	private Channel getChannel(){
		if (channel==null || !channel.isOpen()) {
			channel = getChannel0();
		}
		return channel;
		
	}
	private Channel getChannel0() {
		Channel channel = mqConnectionUtil.getChannel();
		try {
			//设置为生产者确认
			SelectOk sok = channel.confirmSelect();
			channel.queueDeclare(TASK_QUEUE_NAME,true,false,false,null);
			channel.addShutdownListener(new ShutdownListener() {
				
				@Override
				public void shutdownCompleted(ShutdownSignalException cause) {
					try {
						channel.close();
					} catch (IOException | TimeoutException e) {
						logger.error("连接断开", e);
					}
				}
			});
			channel.addConfirmListener(new ConfirmListener() {
				@Override
				public void handleAck(long deliveryTag, boolean multiple) throws IOException {
					System.out.println(deliveryTag);	
				}

				@Override
				public void handleNack(long deliveryTag, boolean multiple) throws IOException {
					System.out.println(deliveryTag);	
				}
	        });
			
			channel.addReturnListener(new ReturnListener() {

				@Override
				public void handleReturn(int replyCode,
			            String replyText,
			            String exchange,
			            String routingKey,
			            AMQP.BasicProperties properties,
			            byte[] body) throws IOException {
					StringBuilder sb = new StringBuilder().append("响应状态码-ReplyCode：").append(replyCode)
						.append(", 响应内容-ReplyText：").append(replyText)
						.append(", Exchange:").append(exchange)
						.append(", RouteKey").append(routingKey)
						.append(", 投递失败的消息：").append(new String(body,"UTF-8"));
					System.out.println(sb.toString());
				}
				
			});
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeChannel();  
		}
		return channel;
	}
	private void closeChannel(){
		if (channel==null || !channel.isOpen()) {
			return;
		}
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	public void addMessageToMQ(String jsonMessage) {
		try {
			Channel channel0 = getChannel();

			channel0.basicPublish("", TASK_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, jsonMessage.getBytes());
		} catch (IOException e) {
			logger.error("无法将消息加入MQ", e);
		}

	}
}
