package com.ks.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.ks.utils.DateUtils;
import redis.clients.jedis.Jedis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ivan on 16/3/17.
 */
public class SplitLogTestBolt implements IRichBolt {


    private static final long serialVersionUID = -8159690997705696712L;
    private OutputCollector collector;
    private Jedis jedis;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        jedis = new Jedis("192.168.88.24", 6379);
    }

    @Override
    public void execute(Tuple input) {
        try {
            String data = input.getString(0);
            if (data != null && data.length() > 0) {
                String ragex = "(.*?)\\[(.*?)\\]\\[(.*?)\\]\\[(.*?)\\] - (.*)";
                Pattern p = Pattern.compile(ragex);
                Matcher m = p.matcher(data);
                if (m.matches()) {
                    String date = m.group(1);
                    String level = m.group(2);
                    String className = m.group(3);
                    String threadId = m.group(4);
                    String message = m.group(5);
                    if (level.equals("ERROR")) {
                        errorDo(date, level, className, threadId, message);
                    }
                    if (level.equals("INFO ")) {
                        infoDo(date, level, className, threadId, message);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            collector.ack(input);
        }
        collector.ack(input);
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    private void infoDo(String date, String level, String className, String threadId, String message) {
        try {
            String curDate = date.substring(0, 10).replaceAll("-", "");
            Long curDateL = null;
            Long toHourL = null;
            Date temp;
            try {
                temp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").parse(date);
                curDateL = temp.getTime();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(temp);
                calendar.set(Calendar.MINUTE,0);
                calendar.set(Calendar.SECOND,0);
                calendar.set(Calendar.MILLISECOND,0);
                toHourL =  calendar.getTimeInMillis();

            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (level.equals("INFO ")) {
                // String ragex = "方法名:(.*?),消耗时间:(.*?),当前用户:(.*?),传入参数:(.*?),返回数据:(.*)";
                String ragex = "方法名:(.*?),消耗时间:(.*?),传入参数:(.*?),返回数据:(.*)";
                Pattern p = Pattern.compile(ragex);
                Matcher m = p.matcher(message);
                if (m.matches()) {
                    System.out.println("789");
                    String functionName = className + "." + m.group(1);
                    String useTime = m.group(2);
//                    String userId = m.group(3);
                    Long durationTime = DateUtils.durationTime(useTime);
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
                    jedis.zincrby("user:visitTimeSet." + curDate,1, toHourL.toString());//用户每天访问时间列表
                    jedis.zincrby("user:dateVisitTime." + curDate, 1, "123");//当天用户访问次数

                    //function信息记录
                    jedis.zincrby("function:" + curDate, 1, functionName);//功能点访问次数
                    if (durationTime > 100) {
                        jedis.zadd("functionList:" + curDate, durationTime, functionName);//功能点访问列表
                    }
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

    private void errorDo(String date, String level, String className, String threadId, String message) {
        try {

            String curDate = date.substring(0, 10).replaceAll("-", "");
            Long curDateL = null;
            try {
                curDateL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").parse(date).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (level.equals("ERROR")) {
//                String ragex = "方法名:(.*?),消耗时间:(.*?),当前用户:(.*?),传入参数:(.*?),返回数据:(.*)";
                String ragex = "方法名:(.*?),消耗时间:(.*?),传入参数:(.*?),返回数据:(.*)";
                Pattern p = Pattern.compile(ragex);
                Matcher m = p.matcher(message);
                if (m.matches()) {
                    System.out.println("111");
                    String functionName = className + m.group(1);
                    String useTime = m.group(2);
//                    String userId = m.group(3);
                    Long durationTime = DateUtils.durationTime(useTime);
                    Long errorInfoNo = jedis.incr("ERROR:num." + curDate);
                    Long countErrFunc = jedis.sadd("ERROR:function." + curDate + "." + functionName, errorInfoNo.toString());
//                    Long userVisitTime = jedis.incr("user:visitTime." + curDate + "." + userId);
                    Long userVisitTime = jedis.incr("user:visitTime." + curDate + "." + 345);
                    jedis.set("ERROR:content." + curDate + "." + errorInfoNo, date + " " + className + " " + message);//ERROR日志内容
                    jedis.incr("pv:num." + curDate);
                    jedis.zadd("user:visitError." + curDate, curDateL, errorInfoNo.toString());
                    if (userVisitTime == 1) {
                        jedis.incr("uv:" + curDate);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Date temp = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(temp);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        long toHourL =  calendar.getTimeInMillis();
        Date newdate = calendar.getTime();
        System.out.println(toHourL);
    }
}

