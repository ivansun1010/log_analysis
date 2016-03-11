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
 * Created by ivan on 16/3/9.
 */
@Component
public class LogInfoService {

    private static final Logger logger = LoggerFactory.getLogger(LogInfoService.class);

    @Autowired
    private RedisTemplate<String, String> template;

    public String getInfoLogContent(String date, String no) {
        return template.opsForValue().get("INFO:content." + date + "." + no);
    }

    public List<InfoEntity> getInfoLogTime(String date) {
        List<InfoEntity> infoEntities = new ArrayList<InfoEntity>();
        Set<ZSetOperations.TypedTuple<String>> set = template.opsForZSet().rangeWithScores("INFO:infoTime."+date,0,-1);
        Iterator<ZSetOperations.TypedTuple<String>> it = set.iterator();
        while (it.hasNext()) {
            ZSetOperations.TypedTuple<String> typedTuple = it.next();
            InfoEntity infoEntity = new InfoEntity();
            infoEntity.setDate(date);
            infoEntity.setMessage(getInfoLogContent(date,typedTuple.getValue()));
            infoEntity.setDateL(typedTuple.getScore().toString());
        }
        return infoEntities;
    }

    public String getInfoLogNum(String date) {
        return template.opsForValue().get("INFO:num."+date);

    }

}
