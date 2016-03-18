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
            try {
                curDateL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").parse(date).getTime();
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
        String data = "[16:11:14.145 [localhost-startStop-1] DEBUG o.s.c.i.s.PathMatchingResourcePatternResolver - Resolved location pattern [classpath*:com/ync365/njt/bussiness/**/*.class] to resources [URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/enums/RolesEnum.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/enums/utils/ValueEnumUtils.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/enums/UserTypeEnum.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/enums/ValueEnum.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/observer/OrderPaymentObservable.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/observer/SendMessageObserver.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/observer/OrderPaymentObserver.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/observer/SendMessageObservable.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/AdvertisementBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/SysUserBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/AnswersBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/SysRegionBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/NjsInfoBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/ArticleCategoryBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/SensitiveWordBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/UserBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/SysNjsQaBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/CropCategoriesBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/SysMessageBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/PostBackNoticeBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/SysMessageBiz$1.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/DownloadBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/GoldRecordBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/ProductionGuideBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/SignRecordBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/UserMessageBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/NczInfoBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/FeedBackBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/MobileValiCodeBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/SysRoleBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/MyCollectionBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/PostBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/QuestionsBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/hb/HomeHbBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/hb/UserHbBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/hb/QuestionsHbBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/hb/SignHbBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/hb/UserUploadPhotoHbBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/hb/UserCenterHbBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/PostBackBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/bo/QuestionsBo.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/bo/MessageBo.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/bo/PostBackBo.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/bo/AnswersBo.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/bo/UserGoldInfoBo.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/bo/MessageNotRead.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/bo/UserBo.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/bo/PostBo.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/bo/FeedBackBo.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/AdminShiroBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/UserGoldBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/biz/SysResourceBiz.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/mybatis/ResultMap.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/CropCategoriesDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/QuestionsDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/SysResourceDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/SensitiveWordDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/PostBackDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/AdvertisementPicDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/NjsInfoDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/UserDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/SysRegionDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/SysNjsQaDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/SignRecordDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/GoldRecordDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/MyCollectionDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/FeedBackDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/UserGoldDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/SysSeuserAreas.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/NjsInfo.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/GoldRecord.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/MyCollection.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/QuestionsNjs.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/SignRecord.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/Areas.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/AdvertisementPic.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/NczInfo.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/FeedBack.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/SysNjsQa.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/SysUser.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/Questions.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/SysMessage.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/User.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/CropCategories.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/UserMessage.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/SysResource.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/ProductionGuideOperationLog.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/NjsInfoQuery.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/SensitiveWord.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/PostBackNotice.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/NczInfoQuery.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/UserGold.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/SysRegion.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/Post.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/SysRole.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/NjtResultMap.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/Answers.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/ArticleCategory.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/ProductionGuide.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/entity/PostBack.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/ArticleCategoryDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/QuestionsNjsDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/ProductionGuideOperationLogDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/AnswersDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/PostDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/SysMessageDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/SysUserDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/NczInfoDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/SysRoleDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/AreasDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/PostBackNoticeDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/ProductionGuideDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/dao/UserMessageDao.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/pagination/PageRequest.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/pagination/PageRequest$Sort.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/pagination/PageBuilder.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/pagination/Page.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/StateConstants$Status.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/StringUtilsforRegex.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/StateConstants$NjsAuthenticationStateType.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/TreeNode.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/StateConstants$GoldRecordType.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/StateConstants$IsDel.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/ReturnCode.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/StateConstants$MessageType.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/StateConstants.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/CloneUtils.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/StateConstants$AnswerState.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/DFAUtil.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/StateConstants$UserType.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/ConfigUtil.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/StateConstants$GoldType.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/StateConstants$QuestionStateForNjs.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/ReadExcelUtil.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/Constant.class], URL [jar:file:/opt/tomcat/webapps/admin/WEB-INF/lib/bussiness-0.0.1-SNAPSHOT.jar!/com/ync365/njt/bussiness/utils/StateConstants$QuestionState.class]]]";
            if (data != null && data.length() > 0) {
                System.out.println("555>>>>>>>>>>>>>>");
                String ragex = "(.*?)\\[(.*?)\\]\\[(.*?)\\]\\[(.*?)\\] - (.*)";
                Pattern p = Pattern.compile(ragex);
                Matcher m = p.matcher(data);
                System.out.println(m.find());
                System.out.println("333>>>>>>>>>>");
            }
            System.out.println("444>>>>>>>>>");
    }
}

