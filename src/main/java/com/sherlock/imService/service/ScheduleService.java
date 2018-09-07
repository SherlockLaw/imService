package com.sherlock.imService.service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.sherlock.imService.redis.RedisService;

@Lazy(false)
@Component
@Configuration
@PropertySource("classpath:job.properties")
public class ScheduleService implements SchedulingConfigurer {
	private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
	
	@Autowired
	private RedisService redisService;
	
	@Value("${cron_clearMessage}")
	private String cron_clearMessage;
 
	@Bean 
	public ScheduledExecutorService scheduledExecutorService(){
		return Executors.newScheduledThreadPool(5); 
	} 
	@Bean 
	public TaskScheduler taskScheduler(){ 
	return new ConcurrentTaskScheduler(); 
	}
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.addTriggerTask(new Runnable() {
			@Override
			public void run() {
				String label = UUID.randomUUID().toString();
				logger.info("清除过期消息线程开始:"+label);
				// 任务逻辑
				redisService.clearMessage();
				logger.info("清除过期消息线程结束:"+label);
			}
		}, new Trigger() {
			@Override
			public Date nextExecutionTime(TriggerContext triggerContext) {
				// 任务触发，可修改任务的执行周期
				CronTrigger trigger = new CronTrigger(cron_clearMessage);
                Date nextExec = trigger.nextExecutionTime(triggerContext);
                return nextExec;
			}
		});
	}
//	@Scheduled(cron = "0/5 * * * * *")
//    public void scheduled(){
//        logger.info("=====>>>>>使用cron  {}",System.currentTimeMillis());
//    }
}
