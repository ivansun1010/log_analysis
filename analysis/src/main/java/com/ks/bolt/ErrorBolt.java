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
public class ErrorBolt extends BaseBasicBolt {

    public static Logger LOG = Logger.getLogger(ErrorBolt.class);

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

            if (level.equals("ERROR")) {
//                String ragex = "方法名:(.*?),消耗时间:(.*?),当前用户:(.*?),传入参数:(.*?),返回数据:(.*)";
                String ragex = "方法名:(.*?),消耗时间:(.*?),传入参数:(.*?),返回数据:(.*)";
                Pattern p = Pattern.compile(ragex);
                Matcher m = p.matcher(message);
                if (m.find()) {
                    String functionName = className + m.group(1);
                    String useTime = m.group(2);
//                    String userId = m.group(3);
                    Long durationTime = dateUtils.durationTime(useTime);
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

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}
