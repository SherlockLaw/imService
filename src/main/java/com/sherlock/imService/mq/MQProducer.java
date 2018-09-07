package com.sherlock.imService.mq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.AMQP.Basic;
import com.rabbitmq.client.AMQP.Basic.Return;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Confirm.SelectOk;

@Component
public class MQProducer {
	
	@Autowired
	private MQConnectionUtil mqConnectionUtil;
	
	public static final String TASK_QUEUE_NAME="task_queue";
	public static void main(String[] args){
		
	}
    public void addMessageToMQ(String jsonMessage) {
        Channel channel = mqConnectionUtil.getChannel();
		try {
			//设置为生产者确认
			SelectOk sok = channel.confirmSelect();
			channel.queueDeclare(TASK_QUEUE_NAME,true,false,false,null);
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
	        //分发信息
	        channel.basicPublish("",TASK_QUEUE_NAME,
	        		MessageProperties.PERSISTENT_TEXT_PLAIN,jsonMessage.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
	        
		}
        
    }
}
