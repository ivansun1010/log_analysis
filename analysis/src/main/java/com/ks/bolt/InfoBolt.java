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


    private static Jedis jedis = new Jedis("192.168.88.24", 6379);

    private static final DateUtils dateUtils = new DateUtils();

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        try {

            List<Object> list = tuple.getValues();

            String date = (String) list.get(0);
            String curDate = date.substring(0, 10).replaceAll("-", "");
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
               // String ragex = "方法名:(.*?),消耗时间:(.*?),当前用户:(.*?),传入参数:(.*?),返回数据:(.*)";
                String ragex = "方法名:(.*?),消耗时间:(.*?),传入参数:(.*?),返回数据:(.*)";
                Pattern p = Pattern.compile(ragex);
                Matcher m = p.matcher(message);
                LOG.info("123123123123");
                if (m.find()) {
                    String functionName = className + "." + m.group(1);
                    String useTime = m.group(2);
//                    String userId = m.group(3);
                    Long durationTime = dateUtils.durationTime(useTime);
                    //日志信息记录
                    Long logInfoNo = jedis.incr("INFO:num." + curDate);//日志总数
                    jedis.set("INFO:content." + curDate + "." + logInfoNo, date + " " + className + " " + message);//INFO日志内容
                    jedis.zadd("INFO:infoTime." + curDate, curDateL, logInfoNo.toString());//日志按时间排序
                    //user信息记录
//                    Long userVisitTime = jedis.incr("user:visitTime." + curDate + "." + userId);//用户当天登录次数
//                    jedis.sadd("user:visitTimeSet." + curDate + "." + userId, curDateL.toString() + "." + logInfoNo.toString());//用户每天访问时间列表
//                    jedis.zincrby("user:function." + curDate + "." + userId, 1, functionName + "." + logInfoNo.toString());//用户访问方法次数

                    //user信息记录
                    Long userVisitTime = jedis.incr("user:visitTime." + curDate + "." + 123);//用户当天登录次数
                    jedis.sadd("user:visitTimeSet." + curDate + "." + 123, curDateL.toString() + "." + logInfoNo.toString());//用户每天访问时间列表
                    jedis.zincrby("user:function." + curDate + "." + 123, 1, functionName + "." + logInfoNo.toString());//用户访问方法次数


                    //function信息记录
                    jedis.zincrby("function:" + curDate, 1, functionName);//功能点访问次数
                    jedis.zadd("functionList:" + curDate, durationTime, functionName + "." + logInfoNo);//功能点访问列表
                    //pv
                    jedis.incr("pv:num." + curDate);
                    //uv
                    if (userVisitTime == 1) {
                        jedis.incr("uv:" + curDate);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    public static void main(String[] args) {
        String message = "方法名:count,消耗时间:0:00:00.182,传入参数:{\"reqSn\":null,\"token\":null,\"data\":{}},返回数据:{\"timestamp\":\"1453797988114\",\"results\":{\"1\":0,\"2\":4,\"3\":0,\"4\":0,\"5\":0,\"6\":0,\"userId\":19552},\"status\":200,\"msg\":\"success\"}";
        String ragex = "方法名:(.*?),消耗时间:(.*?),传入参数:(.*?),返回数据:(.*)";
        Pattern p = Pattern.compile(ragex);
        Matcher m = p.matcher(message);
        if (m.find()) {
            LOG.info("2222222222222222");
            String functionName = m.group(1);
            String useTime = m.group(2);
            String userId = m.group(3);
            System.out.println(userId);
        }
    }
}
