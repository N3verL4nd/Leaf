package com.sankuai.inf.leaf;

import org.junit.Test;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;

import java.util.concurrent.TimeUnit;

/**
 * @author liguanghui02
 * @date 2021/7/13
 */
public class T {
    @Test
    public void test1() {
        StopWatch sw = new Slf4JStopWatch();


        try {
            TimeUnit.MILLISECONDS.sleep(1024);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(sw.stop());
    }
}
