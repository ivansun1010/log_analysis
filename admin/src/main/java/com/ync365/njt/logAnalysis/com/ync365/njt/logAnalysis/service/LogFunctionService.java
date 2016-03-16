package com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by ivan on 16/3/10.
 */
@Component
public class LogFunctionService {

    private static final Logger logger = LoggerFactory.getLogger(LogFunctionService.class);

    @Autowired
    private RedisTemplate<String, String> template;

    public String getLogFunctionByDate(String date) {
        Set<ZSetOperations.TypedTuple<String>> set = template.opsForZSet().reverseRangeWithScores("function:" + date, 0, 20);
        Iterator<ZSetOperations.TypedTuple<String>> it = set.iterator();
        StringBuilder result = new StringBuilder();
        result.append("[");
        while (it.hasNext()) {
            ZSetOperations.TypedTuple<String> typedTuple = it.next();
            result.append("[\"").append(typedTuple.getValue()).append("\",");
            result.append(typedTuple.getScore()).append("],");
        }
        result.delete(result.length() - 1, result.length());
        result.append("]");
        return result.toString();
    }

    public String getLogFunctionUseTimeByDate(String date) {
        Set<ZSetOperations.TypedTuple<String>> set = template.opsForZSet().reverseRangeWithScores("functionList:" + date, 0, 20);
        Iterator<ZSetOperations.TypedTuple<String>> it = set.iterator();
        StringBuilder result = new StringBuilder();
        result.append("[");
        while (it.hasNext()) {
            ZSetOperations.TypedTuple<String> typedTuple = it.next();
                result.append("[\"").append(typedTuple.getValue()).append("\",");
                result.append(typedTuple.getScore()).append("],");
        }
        result.delete(result.length() - 1, result.length());
        result.append("]");
        return result.toString();
    }


}

