package com.sherlock.imService.mq;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.sherlock.imService.netty.ImServer;

@Component
public class MQWork {
	private static final Logger logger = LoggerFactory.getLogger(MQWork.class);
	private AtomicBoolean isStop = new AtomicBoolean(false);
	
	@Autowired
	private MQConnectionUtil mqConnectionUtil;
	
	@Autowired
	private ImServer imServer;
	
	//控制重连
	private final Semaphore sem = new Semaphore(1);
	
	@PostConstruct
    public void startTakeMessageFromMQTask() {
    	Runnable runnable = new Runnable() {		
			@Override
			public void run() {
				logger.info("消费线程启动");
				while (!isStop.get()) {
					try {
						sem.acquire();
						connectAndHandler();
					} catch (InterruptedException e) {
						logger.error("MQ连接异常：", e);
					}
				}
	
			}
		};
        new Thread(runnable).start();
    }
	
    private void connectAndHandler(){
    	Channel channel = mqConnectionUtil.getChannel();
        try {
			channel.queueDeclare(MQProducer.TASK_QUEUE_NAME, true, false, false, null);
	        //每次从队列获取的数量
	        channel.basicQos(1);
	        channel.addShutdownListener(new ShutdownListener() {
				@Override
				public void shutdownCompleted(ShutdownSignalException cause) {
					//释放信号量，进入重连机制
					sem.release();
				}
			});
	        final Consumer consumer = new DefaultConsumer(channel) {
	        	private Channel channel;
	        	public DefaultConsumer init(Channel channel){
	        		this.channel = channel;
	        		return this;
	        	}
	            @Override
	            public void handleDelivery(String consumerTag,
	                                       Envelope envelope,
	                                       AMQP.BasicProperties properties,
	                                       byte[] body) throws IOException {
	                String jsonMessage = new String(body);
	                System.out.println("startTakeMessageFromMQTask收到消息："+jsonMessage);
	                try {
	                    imServer.sendMessage(jsonMessage);
	                    System.out.println("消息消费完成");
	                }catch (Exception e){
	                    //channel.abort();
	                    logger.error("",e);
	                }finally {
	                    channel.basicAck(envelope.getDeliveryTag(),false);
	                }
	            }
	        }.init(channel);
	        boolean autoAck=false;
	        
	        channel.basicConsume(MQProducer.TASK_QUEUE_NAME, autoAck, consumer);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    }
    
}
