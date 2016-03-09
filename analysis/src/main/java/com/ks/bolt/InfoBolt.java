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
        String curDate = date.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
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
                Long logInfoNo = jedis.incr("INFO:num");
                Long userVisitTime = jedis.incr("user:visitTime." + userId + "." + curDate);
                Long countFunction = jedis.incr("function:" + functionName);//功能点访问次数
                jedis.zadd("functionList:" + functionName, durationTime, userId);//功能点访问列表
                Long countUserfunc = jedis.incr("user.func:" + userId + "." + functionName + "." + curDate);//用户访问次数
                jedis.zadd("functionTime:" + functionName + "." + curDate, durationTime, message);//方法耗时
                jedis.incr("pv:" + curDate);
                jedis.set("INFO:" + logInfoNo, date + " " + className + " " + message);//INFO日志内容
                jedis.zadd("user:visitInfo", curDateL, message);
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
