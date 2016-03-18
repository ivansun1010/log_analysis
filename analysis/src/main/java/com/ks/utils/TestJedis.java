package com.ks.utils;

import redis.clients.jedis.Jedis;

/**
 * Created by ivan on 16/3/17.
 */
public class TestJedis implements  Runnable{
    private Jedis jedis;


    public void per(){
        jedis = new Jedis("127.0.0.1", 6379);
        jedis.connect();
    }

    @Override
    public void run() {
        jedis = new Jedis("127.0.0.1", 6379);
        jedis.connect();
        for (int i = 0;i<100000;i++)
        jedis.incr("test");
    }

}
