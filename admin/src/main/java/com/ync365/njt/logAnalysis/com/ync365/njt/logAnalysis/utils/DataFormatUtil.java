package com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ivan on 16/3/11.
 */
@Component
public class DataFormatUtil {

    private static final Logger logger = LoggerFactory.getLogger(DataFormatUtil.class);


    public String ZSetToJson(String key, String dateFormat, String splitBy, RedisTemplate<String, String> template) {
        Set<String> set = template.keys(key + "*");
        Iterator<String> it = set.iterator();
        StringBuilder result = new StringBuilder();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        result.append("[");
        List<String> list = new ArrayList<String>();
        while (it.hasNext()) {
            String temp = it.next();
            String date = temp.split(splitBy)[1];
            list.add(date);
        }
        Collections.sort(list);
        for (String date : list) {
            try {
                result.append("[").append(simpleDateFormat.parse(date).getTime()).append(",");
                result.append(template.opsForValue().get(key + date)).append("],");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        result.delete(result.length() - 1, result.length());
        result.append("]");
        return result.toString();

    }
}
