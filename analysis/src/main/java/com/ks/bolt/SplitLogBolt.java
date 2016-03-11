package com.ks.bolt;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ivan on 16/3/3.
 */
public class SplitLogBolt extends BaseBasicBolt {

    public static Logger LOG = Logger.getLogger(SplitLogBolt.class);

    private static final long serialVersionUID = 689316581965807841L;

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        try {
            String data = tuple.getString(0);
            if (data != null && data.length() > 0) {
                String ragex = "(.*?)\\[(.*?)\\]\\[(.*?)\\]\\[(.*?)\\] - (.*)";
                Pattern p = Pattern.compile(ragex);
                Matcher m = p.matcher(data);
                if (m.find()) {
                    String date = m.group(1);
                    String level = m.group(2);
                    String className = m.group(3);
                    String threadId = m.group(4);
                    String message = m.group(5);
                    if (level.equals("ERROR")) {
                        collector.emit("ERROR", new Values(date, level, className, threadId, message));
                    }
                    if (level.equals("INFO ")) {
                        collector.emit("INFO ", new Values(date, level, className, threadId, message));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        Fields fields = new Fields("date", "level", "className", "threadId", "message");
        declarer.declareStream("ERROR", fields);
        declarer.declareStream("INFO ", fields);
    }

    public static void main(String[] args) {
        String str = "2016-02-03 09:31:09:862[INFO ][com.ync365.njt.admin.rest.ProductionGuideRestController][51  ] - 方法名:getProdGuideList,消耗时间:0:00:01.798,当前用户:null,传入参数:{\"reqSn\":null,\"token\":null,\"data\":{\"categoryId\":0,\"skip\":1,\"isMy\":0,\"take\":10}},返回数据:{\"timestamp\":\"1454463069849\",\"results\":[{\"praiseNum\":0,\"id\":52,\"categoryName\":\"虫草防治\",\"createTime\":1452517967000,\"title\":\"萝莉控\",\"userId\":2046,\"userName\":\"刘伟光\",\"collectionNum\":0,\"coverImage\":\"\"},{\"praiseNum\":1,\"id\":51,\"createTime\":1451956902000,\"title\":\"afsdfsdfs\",\"userId\":1,\"userImage\":\"\",\"userName\":\"超级管理员\",\"collectionNum\":1,\"coverImage\":\"http://ync-njt-dev.b0.upaiyun.com/njt/images/20160105/1451956833068.jpg\"},{\"praiseNum\":12,\"id\":4,\"categoryName\":\"物种育种\",\"createTime\":1450251671000,\"title\":\"物种育种指南农技师发布\",\"userId\":1002,\"collectionNum\":15,\"coverImage\":\"\"},{\"praiseNum\":13,\"id\":2,\"categoryName\":\"物种育种\",\"createTime\":1450251671000,\"title\":\"null\",\"userId\":1001,\"collectionNum\":15,\"coverImage\":\"\"},{\"praiseNum\":12,\"id\":7,\"categoryName\":\"物种育种\",\"createTime\":1450251671000,\"title\":\"物种育种指南农技师发布\",\"userId\":1,\"userImage\":\"\",\"userName\":\"超级管理员\",\"collectionNum\":15,\"coverImage\":\"http://ync-njt-dev.b0.upaiyun.com/njt/images/20160104/1451904617633.jpg\"},{\"praiseNum\":12,\"id\":5,\"categoryName\":\"物种育种\",\"createTime\":1450251671000,\"title\":\"物种育种指南农技师发布\",\"userId\":1001,\"collectionNum\":15,\"coverImage\":\"\"},{\"praiseNum\":0,\"id\":10,\"categoryName\":\"农技文章\",\"createTime\":1450183335000,\"title\":\"阿萨斯\",\"userId\":1,\"userImage\":\"\",\"userName\":\"超级管理员\",\"collectionNum\":0,\"coverImage\":\"\"},{\"praiseNum\":0,\"id\":9,\"categoryName\":\"虫草防治\",\"createTime\":1450183245000,\"title\":\"阿萨斯\",\"userId\":1,\"userImage\":\"\",\"userName\":\"超级管理员\",\"collectionNum\":0,\"coverImage\":\"http://ynctest.b0.upaiyun.com/njt/images/20151215/1450183076235.png\"}],\"status\":200,\"msg\":\"success\"}2016-02-03 09:31:09:867[DEBUG][org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdviceChain][61  ] - Invoking ResponseBodyAdvice chain for body=com.ync365.njt.admin.rest.dto.RestResponseDTO@779c6215[timestamp=1454463069849,results=[{praiseNum=0, id=52, categoryName=虫草防治, createTime=2016-01-11 21:12:47.0, title=萝莉控, userId=2046, userName=刘伟光, collectionNum=0, coverImage=}, {praiseNum=1, id=51, createTime=2016-01-05 09:21:42.0, title=afsdfsdfs, userId=1, userImage=, userName=超级管理员, collectionNum=1, coverImage=http://ync-njt-dev.b0.upaiyun.com/njt/images/20160105/1451956833068.jpg}, {praiseNum=12, id=4, categoryName=物种育种, createTime=2015-12-16 15:41:11.0, title=物种育种指南农技师发布, userId=1002, collectionNum=15, coverImage=}, {praiseNum=13, id=2, categoryName=物种育种, createTime=2015-12-16 15:41:11.0, title=null, userId=1001, collectionNum=15, coverImage=}, {praiseNum=12, id=7, categoryName=物种育种, createTime=2015-12-16 15:41:11.0, title=物种育种指南农技师发布, userId=1, userImage=, userName=超级管理员, collectionNum=15, coverImage=http://ync-njt-dev.b0.upaiyun.com/njt/images/20160104/1451904617633.jpg}, {praiseNum=12, id=5, categoryName=物种育种, createTime=2015-12-16 15:41:11.0, title=物种育种指南农技师发布, userId=1001, collectionNum=15, coverImage=}, {praiseNum=0, id=10, categoryName=农技文章, createTime=2015-12-15 20:42:15.0, title=阿萨斯, userId=1, userImage=, userName=超级管理员, collectionNum=0, coverImage=}, {praiseNum=0, id=9, categoryName=虫草防治, createTime=2015-12-15 20:40:45.0, title=阿萨斯, userId=1, userImage=, userName=超级管理员, collectionNum=0, coverImage=http://ynctest.b0.upaiyun.com/njt/images/20151215/1450183076235.png}],status=200,msg=success]";
        String ragex = "(.*?)\\[(.*?)\\]\\[(.*?)\\]\\[(.*?)\\] - (.*)";
        Pattern p = Pattern.compile(ragex);
        Matcher m = p.matcher(str);
        if (m.find()) {
            String date = m.group(1);
            String level = m.group(2);
            String className = m.group(3);
            String threadId = m.group(4);
            String message = m.group(5);
            System.out.println(date);
            System.out.println(level);
            System.out.println(className);
            System.out.println(threadId);
            System.out.println(message);
        }

    }
}
