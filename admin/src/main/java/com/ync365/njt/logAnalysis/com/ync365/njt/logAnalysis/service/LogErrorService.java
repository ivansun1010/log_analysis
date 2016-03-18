package com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service;

import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.entity.InfoEntity;
import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.utils.DataFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by ivan on 16/3/8.
 */
@Component
public class LogErrorService {

    private static final Logger logger = LoggerFactory.getLogger(LogErrorService.class);


    @Autowired
    private RedisTemplate<String, String> template;

    public List<InfoEntity> getLogErrorByfunction(String date) {
        List<InfoEntity> errorInfos = new ArrayList<InfoEntity>();
        Set<String> keys = template.keys("ERROR:function." + date + "*");
        Iterator<String> it =  keys.iterator();
        while (it.hasNext()){
            InfoEntity errorInfo = new InfoEntity();
            errorInfo.setFunctionName(it.next());
            errorInfo.setNum(template.opsForValue().get("ERROR:function."+date+"."+errorInfo.getFunctionName()));
            errorInfos.add(errorInfo);
        }
        return errorInfos;
    }

    public String getLogErrorNum(){
        return DataFormatUtil.ZSetToJson("ERROR:num.","yyyyMMdd","\\.",template);
    }

    public String getErrorLogContent(String date, String no) {
        return template.opsForValue().get("ERROR:content." + date + "." + no);
    }

    public String getErrorLogNum(String date) {
        return template.opsForValue().get("ERROR:num." + date);
    }
}
