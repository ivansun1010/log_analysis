package com.ks.utils;

/**
 * Created by ivan on 16/3/17.
 */
public class Test {
    public static void main(String[] args) {
        TestJedis test = new TestJedis();
        TestJedis test2= new TestJedis();
        TestJedis test1 = new TestJedis();

        new Thread(test).start();
        new Thread(test1).start();
        new Thread(test2).start();

    }
}
