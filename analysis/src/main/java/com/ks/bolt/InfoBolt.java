package com.ks.bolt;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.ks.utils.DateUtils;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ivan on 16/3/3.
 */
public class InfoBolt extends BaseBasicBolt {

    public static Logger LOG = Logger.getLogger(InfoBolt.class);

    private static final long serialVersionUID = 689316581965807841L;


    private static Jedis jedis = new Jedis("172.10.3.212", 6379);

    private static final DateUtils dateUtils = new DateUtils();

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {

        List<Object> list = tuple.getValues();

        String date = (String) list.get(0);
        String curDateTime = date.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
        String curDate = date.substring(0, 10);
        Long curDateL = null;
        try {
            curDateL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String level = (String) list.get(1);
        String className = (String) list.get(2);
        String threadId = (String) list.get(3);
        String message = (String) list.get(4);
        if (level.equals("INFO ")) {
            String ragex = "方法名:(.*?),消耗时间:(.*?),当前用户:(.*?),传入参数:(.*?),返回数据:(.*)";
            Pattern p = Pattern.compile(ragex);
            Matcher m = p.matcher(message);
            if (m.find()) {
                String functionName = className + m.group(1);
                String useTime = m.group(2);
                String userId = m.group(3);
                Long durationTime = dateUtils.durationTime(useTime);
                //日志信息记录
                Long logInfoNo = jedis.incr("INFO:num." + curDate);//日志总数
                jedis.set("INFO:content." + curDate + "." + logInfoNo, date + " " + className + " " + message);//INFO日志内容
                jedis.zadd("INFO:infoTime." + curDate, curDateL, logInfoNo.toString());//日志按时间排序
                //user信息记录
                Long userVisitTime = jedis.incr("user:visitTime." + curDate + "." + userId);//用户当天登录次数
                jedis.sadd("user:visitTimeSet." + curDate + "." + userId, curDateL.toString());//用户每天访问时间列表
                jedis.zadd("user:visitInfo." + curDate, curDateL, logInfoNo.toString());//用户访问日志信息
                Long countUserfunc = jedis.incr("user:func." + curDate + "." + userId + "." + functionName);//用户访问方法次数
                jedis.sadd("user:func." + curDate + "." + userId + "." + functionName,curDateL.toString());//用户访问方法次数
                //function信息记录
                Long countFunction = jedis.incr("function:" + curDate + "." + functionName);//功能点访问次数
                jedis.zadd("functionList:" + curDate + "." + functionName, durationTime, userId);//功能点访问列表
                jedis.zadd("functionTime:" + functionName + "." + curDateTime, durationTime, message);//方法耗时
                //pv
                jedis.incr("pv:" + curDate);
                //uv
                if (userVisitTime.equals(1)) {
                    jedis.incr("uv:" + curDate);
                }
            }

        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    public static void main(String[] args) {
        try {
            Long curDateL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").parse("2016-02-03 09:31:09:862").getTime();
            System.out.println(curDateL);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
