package com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service;

import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.entity.InfoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by ivan on 16/3/10.
 */
@Component
public class LogFunctionService {

    private static final Logger logger = LoggerFactory.getLogger(LogFunctionService.class);

    @Autowired
    private RedisTemplate<String, String> template;

    public List<InfoEntity> getLogFunctionByDate(String date) {
        List<InfoEntity> infoEntities = new ArrayList<InfoEntity>();
        Set<ZSetOperations.TypedTuple<String>> set = template.opsForZSet().rangeWithScores("function:" + date, 0, -1);
        Iterator<ZSetOperations.TypedTuple<String>> it = set.iterator();
        while (it.hasNext()) {
            ZSetOperations.TypedTuple<String> typedTuple = it.next();
            InfoEntity infoEntity = new InfoEntity();
            infoEntity.setDate(date);
            infoEntity.setMessage(typedTuple.getValue());
            infoEntity.setDateL(typedTuple.getScore().toString());
        }
        return infoEntities;
    }

    public List<InfoEntity> getLogFunctionUseTimeByDate(String date) {
        List<InfoEntity> infoEntities = new ArrayList<InfoEntity>();
        Set<ZSetOperations.TypedTuple<String>> set = template.opsForZSet().reverseRangeWithScores("functionList:" + date, 0, 20);
        Iterator<ZSetOperations.TypedTuple<String>> it = set.iterator();
        while (it.hasNext()) {
            ZSetOperations.TypedTuple<String> typedTuple = it.next();
            InfoEntity infoEntity = new InfoEntity();
            String[] strings = typedTuple.getValue().split(".");
            infoEntity.setDate(date);
            infoEntity.setMessage(strings[0]);
            infoEntity.setLogId(strings[1]);
            infoEntity.setDateL(typedTuple.getScore().toString());
        }
        return infoEntities;
    }

    public List<InfoEntity> getLogFunctionUseTimeByDurDate(String begin,String end) {
        List<InfoEntity> infoEntities = new ArrayList<InfoEntity>();
        return infoEntities;
    }
}

