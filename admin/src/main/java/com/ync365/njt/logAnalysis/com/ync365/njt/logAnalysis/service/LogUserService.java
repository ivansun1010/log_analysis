package com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service;

import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.entity.InfoEntity;
import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.utils.DataFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ivan on 16/3/10.
 */
@Component
public class LogUserService {

    private static final Logger logger = LoggerFactory.getLogger(LogFunctionService.class);

    @Autowired
    private RedisTemplate<String, String> template;

    public String getPVByDate(String date) {
        return template.opsForValue().get("pv:" + date);
    }

    public String getPV() {
        return DataFormatUtil.ZSetToJson("pv:num.","yyyyMMdd","\\.",template);
    }

    public String getUVByDate(String date) {
        return template.opsForValue().get("uv:" + date);
    }

    public String getUV(){
        return DataFormatUtil.ZSetToJson("uv:","yyyyMMdd",":",template);
    }

    public String getUserVisitTimeByDate(String date, String userId) {
        return template.opsForValue().get("user:visitTime." + date + "." + userId);
    }

    public List<InfoEntity> getUserFucntionByDate(String date, String userId) {
        List<InfoEntity> infoEntities = new ArrayList<InfoEntity>();
        Set<ZSetOperations.TypedTuple<String>> set = template.opsForZSet().rangeWithScores("user:function." + date + "." + userId, 0, -1);
        Iterator<ZSetOperations.TypedTuple<String>> it = set.iterator();
        while (it.hasNext()) {
            ZSetOperations.TypedTuple<String> typedTuple = it.next();
            InfoEntity infoEntity = new InfoEntity();
            String[] strings = typedTuple.getValue().split(".");
            infoEntity.setDate(date);
            infoEntity.setMessage(strings[0]);
            infoEntity.setLogId(strings[1]);
            infoEntity.setUserId(userId);
            infoEntity.setDateL(typedTuple.getScore().toString());
        }
        return infoEntities;
    }

    public List<InfoEntity> getUserFunctionListByDate(String date, String userId) {
        List<InfoEntity> infoEntities = new ArrayList<InfoEntity>();
        Set<ZSetOperations.TypedTuple<String>> set = template.opsForZSet().rangeWithScores("user:function." + date, 0, -1);
        Iterator<ZSetOperations.TypedTuple<String>> it = set.iterator();
        while (it.hasNext()) {
            ZSetOperations.TypedTuple<String> typedTuple = it.next();
            InfoEntity infoEntity = new InfoEntity();
            String[] strings = typedTuple.getValue().split(".");
            infoEntity.setDate(date);
            infoEntity.setMessage(strings[0]);
            infoEntity.setLogId(strings[1]);
            infoEntity.setUserId(userId);
            infoEntity.setNum(typedTuple.getScore().toString());
        }
        return infoEntities;
    }

    public String getUserDateVisitTime(String date){
        Set<ZSetOperations.TypedTuple<String>> set = template.opsForZSet().reverseRangeWithScores("user:dateVisitTime." + date, 0, 20);
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

    public String getUserVisitTimeSet(String date){
        Set<ZSetOperations.TypedTuple<String>> set = template.opsForZSet().rangeWithScores("user:visitTimeSet." + date, 0, -1);
        Iterator<ZSetOperations.TypedTuple<String>> it = set.iterator();
        StringBuilder result = new StringBuilder();
        Map<String,Integer> map = new HashMap<String, Integer>();
        result.append("[");
        while (it.hasNext()) {
            ZSetOperations.TypedTuple<String> typedTuple = it.next();
            map.put(new SimpleDateFormat("HH").format(new Date(Long.parseLong(typedTuple.getValue()))),typedTuple.getScore().intValue());
        }
        for (int i = 0;i<25;i++) {
            int num = map.get(String.valueOf(i))==null?0:map.get(String.valueOf(i));
            result.append("[\"").append(i).append("\",");
            result.append(num).append("],");
        }
        result.delete(result.length() - 1, result.length());
        result.append("]");
        return result.toString();
    }
}
