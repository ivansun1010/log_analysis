package com.ks.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.ks.utils.DateUtils;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ivan on 16/3/17.
 */
public class ErrorTestBolt extends BaseRichBolt {

    private static final long serialVersionUID = -9151127204457065113L;

    public static Logger LOG = Logger.getLogger(ErrorTestBolt.class);

    private static Jedis jedis;

    private OutputCollector collector;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        jedis  = new Jedis("192.168.88.24", 6379);
        jedis.connect();
        this.collector = collector;

    }


    @Override
    public void execute(Tuple input) {


    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}
