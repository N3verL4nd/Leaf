package com.sankuai.inf.leaf.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

import java.util.List;

/**
 * @author liguanghui02
 * @date 2021/7/7
 */
public class ZookeeperTest {
    @Test
    public void test1() throws Exception {
        CuratorFramework curator = CuratorFrameworkFactory.builder().connectString("127.0.0.1")
                .retryPolicy(new RetryUntilElapsed(1000, 4))
                .connectionTimeoutMs(10000)
                .sessionTimeoutMs(6000)
                .build();

        curator.start();

        List<String> paths = curator.getChildren().forPath("/");
        System.out.println(paths);


        curator.close();
    }


    @Test
    public void test2() throws Exception {
        CuratorFramework curator = CuratorFrameworkFactory.builder().connectString("127.0.0.1")
                .retryPolicy(new RetryUntilElapsed(1000, 4))
                .connectionTimeoutMs(10000)
                .sessionTimeoutMs(6000)
                .build();

        curator.start();


        String s = curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath("/snowflake/com.sankuai.leaf.opensource.test/forever/127.0.0.1:2181-", "{\"ip\":\"172.18.197.165\",\"port\":\"2181\",\"timestamp\":1625721916737}".getBytes());
        System.out.println(s);


        curator.close();

    }
}
