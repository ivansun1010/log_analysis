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
        String str = "2016-01-26 17:15:23:123[INFO ][com.ync365.njt.admin.rest.hb.HomeHbController][50  ] - 方法名:show,消耗时间:0:00:00.271,传入参数:{\"reqSn\":null,\"token\":null,\"data\":{\"cityName\":\"张掖市\"}},返回数据:{\"timestamp\":\"1453799723122\",\"results\":{\"qlist\":[{\"id\":174,\"userId\":67697,\"content\":\"黄麻子，田间如何管理呢？\",\"pic1\":\"http://ync-njt.b0.upaiyun.com/njt/images/20160125/1453716011881.jpg\",\"pic2\":null,\"pic3\":null,\"pic4\":null,\"goldNum\":0,\"cropsType\":\"黄麻,土豆\",\"province\":1,\"city\":2,\"county\":4,\"town\":3534,\"village\":47571,\"addressDetail\":\"加州小\",\"state\":1,\"answersNum\":1,\"createTime\":1453716065000,\"isDel\":0,\"realName\":\"180****7734\",\"photo\":null,\"stateForNjs\":null,\"pcAddress\":null,\"mobile\":null,\"provinceName\":null,\"cityName\":null},{\"id\":173,\"userId\":2047,\"content\":\"黄麻的种植条件\",\"pic1\":null,\"pic2\":null,\"pic3\":null,\"pic4\":null,\"goldNum\":1,\"cropsType\":\"黄麻,土豆\",\"province\":1,\"city\":2,\"county\":3,\"town\":3516,\"village\":47370,\"addressDetail\":\"\",\"state\":1,\"answersNum\":1,\"createTime\":1453715054000,\"isDel\":0,\"realName\":\"186****6963\",\"photo\":null,\"stateForNjs\":null,\"pcAddress\":null,\"mobile\":null,\"provinceName\":null,\"cityName\":null},{\"id\":172,\"userId\":4117,\"content\":\"咋防治\",\"pic1\":\"http://ync-njt.b0.upaiyun.com/njt/images/20160122/1453434460260.jpg\",\"pic2\":\"http://ync-njt.b0.upaiyun.com/njt/images/20160122/1453434449876.jpg\",\"pic3\":\"http://ync-njt.b0.upaiyun.com/njt/images/20160122/1453434439947.jpg\",\"pic4\":\"http://ync-njt.b0.upaiyun.com/njt/images/20160122/1453434429619.jpg\",\"goldNum\":4,\"cropsType\":\"西红柿\",\"province\":3069,\"city\":3105,\"county\":3119,\"town\":42921,\"village\":683505,\"addressDetail\":\"\",\"state\":1,\"answersNum\":1,\"createTime\":1453434567000,\"isDel\":0,\"realName\":\"杨平\",\"photo\":null,\"stateForNjs\":null,\"pcAddress\":null,\"mobile\":null,\"provinceName\":null,\"cityName\":null},{\"id\":169,\"userId\":4117,\"content\":\"这是什么病害，作防治？\",\"pic1\":\"http://ync-njt.b0.upaiyun.com/njt/images/20160121/1453363937205.jpg\",\"pic2\":\"http://ync-njt.b0.upaiyun.com/njt/images/20160121/1453363926307.jpg\",\"pic3\":\"http://ync-njt.b0.upaiyun.com/njt/images/20160121/1453363913697.jpg\",\"pic4\":null,\"goldNum\":0,\"cropsType\":\"西红柿\",\"province\":3069,\"city\":3105,\"county\":3119,\"town\":42921,\"village\":683505,\"addressDetail\":\"圪捞村大棚蔬菜\",\"state\":1,\"answersNum\":1,\"createTime\":1453364126000,\"isDel\":0,\"realName\":\"杨平\",\"photo\":null,\"stateForNjs\":null,\"pcAddress\":null,\"mobile\":null,\"provinceName\":null,\"cityName\":null},{\"id\":164,\"userId\":2047,\"content\":\"24节气有哪些？\",\"pic1\":null,\"pic2\":null,\"pic3\":null,\"pic4\":null,\"goldNum\":1,\"cropsType\":\"土豆\",\"province\":1,\"city\":2,\"county\":3,\"town\":3516,\"village\":47370,\"addressDetail\":\"\",\"state\":1,\"answersNum\":1,\"createTime\":1453119149000,\"isDel\":0,\"realName\":\"186****6963\",\"photo\":null,\"stateForNjs\":null,\"pcAddress\":null,\"mobile\":null,\"provinceName\":null,\"cityName\":null}],\"signflag\":\"\",\"photo\":\"\",\"userType\":null,\"msgflag\":\"0\",\"signcontent\":\"签到得金币，好礼赚不停\",\"adlist\":[{\"id\":29,\"name\":\"种地\",\"position\":1,\"url\":\"http://ync-njt.b0.upaiyun.com/images/advertPic/2016112/auth_1452536126658.png\",\"sort\":1,\"createTime\":1452536135000},{\"id\":30,\"name\":\"种地\",\"position\":1,\"url\":\"http://ync-njt.b0.upaiyun.com/images/advertPic/2016112/auth_1452536143387.png\",\"sort\":2,\"createTime\":1452536151000},{\"id\":31,\"name\":\"种地\",\"position\":1,\"url\":\"http://ync-njt.b0.upaiyun.com/images/advertPic/2016112/auth_1452536169836.png\",\"sort\":3,\"createTime\":1452536172000}]},\"status\":200,\"msg\":\"成功\"}";
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
