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
        try {

            List<Object> list = tuple.getValues();

            String date = (String) list.get(0);
            String curDate = date.substring(0, 10).replaceAll("-","");
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
            LOG.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + message);
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
                    jedis.sadd("user:function." + curDate + "." + userId + "." + functionName, curDateL.toString());//用户访问方法次数
                    //function信息记录
                    Long countFunction = jedis.incr("function:" + curDate + "." + functionName);//功能点访问次数
                    jedis.zadd("functionList:" + curDate + "." + functionName, durationTime, userId);//功能点访问列表
                    jedis.zadd("functionTime:" + functionName + "." + curDate, durationTime, message);//方法耗时
                    //pv
                    jedis.incr("pv:" + curDate);
                    //uv
                    if (userVisitTime.equals(1)) {
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
        String message = "方法名:getProdGuideList,消耗时间:0:00:01.798,当前用户:null,传入参数:{\"reqSn\":null,\"token\":null,\"data\":{\"categoryId\":0,\"skip\":1,\"isMy\":0,\"take\":10}},返回数据:{\"timestamp\":\"1454463069849\",\"results\":[{\"praiseNum\":0,\"id\":52,\"categoryName\":\"虫草防治\",\"createTime\":1452517967000,\"title\":\"萝莉控\",\"userId\":2046,\"userName\":\"刘伟光\",\"collectionNum\":0,\"coverImage\":\"\"},{\"praiseNum\":1,\"id\":51,\"createTime\":1451956902000,\"title\":\"afsdfsdfs\",\"userId\":1,\"userImage\":\"\",\"userName\":\"超级管理员\",\"collectionNum\":1,\"coverImage\":\"http://ync-njt-dev.b0.upaiyun.com/njt/images/20160105/1451956833068.jpg\"},{\"praiseNum\":12,\"id\":4,\"categoryName\":\"物种育种\",\"createTime\":1450251671000,\"title\":\"物种育种指南农技师发布\",\"userId\":1002,\"collectionNum\":15,\"coverImage\":\"\"},{\"praiseNum\":13,\"id\":2,\"categoryName\":\"物种育种\",\"createTime\":1450251671000,\"title\":\"null\",\"userId\":1001,\"collectionNum\":15,\"coverImage\":\"\"},{\"praiseNum\":12,\"id\":7,\"categoryName\":\"物种育种\",\"createTime\":1450251671000,\"title\":\"物种育种指南农技师发布\",\"userId\":1,\"userImage\":\"\",\"userName\":\"超级管理员\",\"collectionNum\":15,\"coverImage\":\"http://ync-njt-dev.b0.upaiyun.com/njt/images/20160104/1451904617633.jpg\"},{\"praiseNum\":12,\"id\":5,\"categoryName\":\"物种育种\",\"createTime\":1450251671000,\"title\":\"物种育种指南农技师发布\",\"userId\":1001,\"collectionNum\":15,\"coverImage\":\"\"},{\"praiseNum\":0,\"id\":10,\"categoryName\":\"农技文章\",\"createTime\":1450183335000,\"title\":\"阿萨斯\",\"userId\":1,\"userImage\":\"\",\"userName\":\"超级管理员\",\"collectionNum\":0,\"coverImage\":\"\"},{\"praiseNum\":0,\"id\":9,\"categoryName\":\"虫草防治\",\"createTime\":1450183245000,\"title\":\"阿萨斯\",\"userId\":1,\"userImage\":\"\",\"userName\":\"超级管理员\",\"collectionNum\":0,\"coverImage\":\"http://ynctest.b0.upaiyun.com/njt/images/20151215/1450183076235.png\"}],\"status\":200,\"msg\":\"success\"}2016-02-03 09:31:09:867[DEBUG][org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdviceChain][61  ] - Invoking ResponseBodyAdvice chain for body=com.ync365.njt.admin.rest.dto.RestResponseDTO@779c6215[timestamp=1454463069849,results=[{praiseNum=0, id=52, categoryName=虫草防治, createTime=2016-01-11 21:12:47.0, title=萝莉控, userId=2046, userName=刘伟光, collectionNum=0, coverImage=}, {praiseNum=1, id=51, createTime=2016-01-05 09:21:42.0, title=afsdfsdfs, userId=1, userImage=, userName=超级管理员, collectionNum=1, coverImage=http://ync-njt-dev.b0.upaiyun.com/njt/images/20160105/1451956833068.jpg}, {praiseNum=12, id=4, categoryName=物种育种, createTime=2015-12-16 15:41:11.0, title=物种育种指南农技师发布, userId=1002, collectionNum=15, coverImage=}, {praiseNum=13, id=2, categoryName=物种育种, createTime=2015-12-16 15:41:11.0, title=null, userId=1001, collectionNum=15, coverImage=}, {praiseNum=12, id=7, categoryName=物种育种, createTime=2015-12-16 15:41:11.0, title=物种育种指南农技师发布, userId=1, userImage=, userName=超级管理员, collectionNum=15, coverImage=http://ync-njt-dev.b0.upaiyun.com/njt/images/20160104/1451904617633.jpg}, {praiseNum=12, id=5, categoryName=物种育种, createTime=2015-12-16 15:41:11.0, title=物种育种指南农技师发布, userId=1001, collectionNum=15, coverImage=}, {praiseNum=0, id=10, categoryName=农技文章, createTime=2015-12-15 20:42:15.0, title=阿萨斯, userId=1, userImage=, userName=超级管理员, collectionNum=0, coverImage=}, {praiseNum=0, id=9, categoryName=虫草防治, createTime=2015-12-15 20:40:45.0, title=阿萨斯, userId=1, userImage=, userName=超级管理员, collectionNum=0, coverImage=http://ynctest.b0.upaiyun.com/njt/images/20151215/1450183076235.png}],status=200,msg=success]";
        String date = "2016-02-03 09:31:09:862";
        String curDate = date.substring(0, 10).replaceAll("-", "");
        Long curDateL = null;
        try {
            curDateL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String level = "INFO ";
        String className = "com.ync365.njt.admin.rest.ProductionGuideRestController";
        String threadId = "51";
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
            jedis.sadd("user:function." + curDate + "." + userId + "." + functionName, curDateL.toString());//用户访问方法次数
            //function信息记录
            Long countFunction = jedis.incr("function:" + curDate + "." + functionName);//功能点访问次数
            jedis.zadd("functionList:" + curDate + "." + functionName, durationTime, userId);//功能点访问列表
            jedis.zadd("functionTime:" + curDate + functionName, durationTime, logInfoNo.toString());//方法耗时
            //pv
            jedis.incr("pv:" + curDate);
            //uv
            if (userVisitTime.equals(1)) {
                jedis.incr("uv:" + curDate);
            }
        }
    }
}
