package com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by ivan on 16/3/8.
 */
@Configuration
public class ApplicationContext {

    /**
     *
     * @param redisHostName
     * @return
     */
   @Bean
    public JedisConnectionFactory jedisConnectionFactory(@Value("${redis.hostName}") String redisHostName,@Value("${redis.port}") Integer port){
       JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
       jedisConnectionFactory.setHostName(redisHostName);
       jedisConnectionFactory.setPort(port);
       jedisConnectionFactory.setUsePool(true);
       return jedisConnectionFactory;
   }
    @Bean
    public RedisTemplate redisTemplate(@Value("${redis.hostName}") String redisHostName,@Value("${redis.port}") Integer port) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(jedisConnectionFactory(redisHostName,port));
        return redisTemplate;
    }
}
