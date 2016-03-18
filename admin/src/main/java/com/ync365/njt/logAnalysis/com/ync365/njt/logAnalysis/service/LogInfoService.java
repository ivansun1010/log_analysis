package com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service;

import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.entity.InfoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;

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
        Set<ZSetOperations.TypedTuple<String>> set = template.opsForZSet().rangeWithScores("INFO:infoTime." + date, 0, -1);
        Iterator<ZSetOperations.TypedTuple<String>> it = set.iterator();
        while (it.hasNext()) {
            ZSetOperations.TypedTuple<String> typedTuple = it.next();
            InfoEntity infoEntity = new InfoEntity();
            infoEntity.setDate(date);
            infoEntity.setMessage(getInfoLogContent(date, typedTuple.getValue()));
            infoEntity.setDateL(typedTuple.getScore().toString());
        }
        return infoEntities;
    }

    public String getInfoLogNum(String date) {
        return template.opsForValue().get("INFO:num." + date);

    }

    public void test() {
        Set<String> set = template.keys("INFO:content.20160126*");
        Map<String, Integer> map = new HashMap<String, Integer>();
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String temp = it.next();
            String value = template.opsForValue().get(temp).substring(0,23);
            if (map.containsKey(value)) map.put(value, map.get(value) + 1);
            else map.put(value, 1);
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() > 1) {
                System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());

            }
        }
    }

}
