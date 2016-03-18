package com.ks.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Created by ivan on 16/3/17.
 */
public class InfoTestBolt extends BaseRichBolt {

    private static final long serialVersionUID = -257058152037212394L;

    public static Logger LOG = Logger.getLogger(InfoTestBolt.class);

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
