package com.ks.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import com.google.common.collect.ImmutableList;
import com.ks.bolt.SplitLogTestBolt;
import storm.kafka.*;

public class CounterTopology {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			String kafkaZookeeper = "server1:2181,server2:2181,server3:2181";
			BrokerHosts brokerHosts = new ZkHosts(kafkaZookeeper);
			SpoutConfig kafkaConfig = new SpoutConfig(brokerHosts, "log", "/logstormtest", "id");
	        kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
			kafkaConfig.zkServers =  ImmutableList.of("server1","server2","server3");
	        kafkaConfig.zkPort = 2181;


	        //kafkaConfig.forceFromStart = true;
			
	        TopologyBuilder builder = new TopologyBuilder();
	        builder.setSpout("spout", new KafkaSpout(kafkaConfig), 1);
	        builder.setBolt("split", new SplitLogTestBolt(),1).allGrouping("spout");



	        Config config = new Config();
	        config.setDebug(true);
	        
	        if(args!=null && args.length > 0) {
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				config.setNumWorkers(2);
	            
	            StormSubmitter.submitTopology(args[0], config, builder.createTopology());
	        } else {
				System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
				config.setMaxTaskParallelism(1);
	
	            LocalCluster cluster = new LocalCluster();
	            cluster.submitTopology("special-topology", config, builder.createTopology());
	        

	        }
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
