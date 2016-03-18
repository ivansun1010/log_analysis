package com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service;

import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.entity.InfoEntity;
import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.utils.DataFormatUtil;
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


}
