package com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by ivan on 16/3/8.
 */
@Component
public class LogErrorService {

    private static final Logger logger = LoggerFactory.getLogger(LogErrorService.class);


    @Autowired
    private RedisTemplate<String,String> template;

    public String getLogErrorByDateRange(){
        return template.opsForValue().get("ERROR:1");
    }
}
