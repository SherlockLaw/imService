package com.sherlock.imService.configure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@PropertySource(value = "classpath:redis.properties")//配置文件路径
public class RedisConfig {
	private static Logger logger = LoggerFactory.getLogger(RedisConfig.class);

	@Value("${spring.redis.host}")
	private String host;

	@Value("${spring.redis.port}")
	private int port;

	@Value("${spring.redis.timeout}")
	private int timeout;

	@Value("${spring.redis.password}")
	private String password;

	@Value("${spring.redis.database}")
	private int database;

	@Value("${spring.redis.pool.max-idle}")
	private int maxIdle;

	@Value("${spring.redis.pool.min-idle}")
	private int minIdle;

	/**
	 * redis模板，存储关键字是字符串，值是Jdk序列化
	 * 
	 * @Description:
	 * @param factory
	 * @return
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		logger.info("Redis 配置:"+host+",port:"+port);
		
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(factory);
		//设置序列化方式
		StringRedisSerializer stringSerializer = new StringRedisSerializer();
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
		ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        
		redisTemplate.setKeySerializer(stringSerializer);
		redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
		
		redisTemplate.setHashKeySerializer(stringSerializer);
		redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
		
		redisTemplate.afterPropertiesSet();
		
		//支持事务
		//redisTemplate.setEnableTransactionSupport(true);
		return redisTemplate;
	}
}
