package com.sankuai.inf.leaf.snowflake;

import com.google.common.base.Preconditions;
import com.sankuai.inf.leaf.IDGen;
import com.sankuai.inf.leaf.common.Result;
import com.sankuai.inf.leaf.common.Status;
import com.sankuai.inf.leaf.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class SnowflakeIDGenImpl implements IDGen {

    @Override
    public boolean init() {
        return true;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeIDGenImpl.class);

    /**
     * 起始时间戳，用于用当前时间戳减去这个时间戳，算出偏移量
     * SnowflakeID 可以使用 79 年是相对于 twepoch 时间戳
     */
    private final long twepoch;
    /**
     * workID 长度
     */
    private final long workerIdBits = 10L;
    /**
     * 最大能够分配的workerid =1023
     */
    private final long maxWorkerId = ~(-1L << workerIdBits);//最大能够分配的workerid =1023
    /**
     * 自增序列号长度
     */
    private final long sequenceBits = 12L;
    /**
     * workID左移位数为自增序列号的位数
     */
    private final long workerIdShift = sequenceBits;
    /**
     * 时间戳的左移位数为 自增序列号的位数+workID的位数
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits;
    /**
     * 掩码 后12位都为1
     */
    private final long sequenceMask = ~(-1L << sequenceBits);
    /**
     * 工作机器 id
     */
    private long workerId;
    /**
     * 自增序列 id
     */
    private long sequence = 0L;
    /**
     * 最后一次生成 SnowflakeId 的时间戳
     */
    private long lastTimestamp = -1L;
    private static final Random RANDOM = new Random();

    public SnowflakeIDGenImpl(String zkAddress, int port) {
        //Thu Nov 04 2010 09:42:54 GMT+0800 (中国标准时间) 
        this(zkAddress, port, 1288834974657L);
    }

    /**
     * @param zkAddress zk地址
     * @param port      snowflake 监听端口
     * @param twepoch   起始的时间戳
     */
    public SnowflakeIDGenImpl(String zkAddress, int port, long twepoch) {
        this.twepoch = twepoch;
        Preconditions.checkArgument(timeGen() > twepoch, "Snowflake not support twepoch gt currentTime");
        final String ip = Utils.getIp();
        SnowflakeZookeeperHolder holder = new SnowflakeZookeeperHolder(ip, String.valueOf(port), zkAddress);
        LOGGER.info("twepoch:{} ,ip:{} ,zkAddress:{} port:{}", twepoch, ip, zkAddress, port);
        boolean initFlag = holder.init();
        if (initFlag) {
            workerId = holder.getWorkerID();
            LOGGER.info("START SUCCESS USE ZK WORKERID-{}", workerId);
        } else {
            Preconditions.checkArgument(initFlag, "Snowflake Id Gen is not init ok");
        }
        Preconditions.checkArgument(workerId >= 0 && workerId <= maxWorkerId, "workerID must gte 0 and lte 1023");
    }

    @Override
    public synchronized Result get(String key) {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        return new Result(-1, Status.EXCEPTION);
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("wait interrupted");
                    return new Result(-2, Status.EXCEPTION);
                }
            } else {
                return new Result(-3, Status.EXCEPTION);
            }
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                //seq 为0的时候表示是下一毫秒时间开始对seq做随机
                sequence = RANDOM.nextInt(100);
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //如果是新的ms开始
            sequence = RANDOM.nextInt(100);
        }

        lastTimestamp = timestamp;

        // timestampLeftShift = sequenceBits + workerIdBits = 12 + 10 = 22
        // workerIdShift = 12
        long id = ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
        return new Result(id, Status.SUCCESS);

    }

    /**
     * 比最后一次上报晚的时间戳
     * 当前时间戳内序列号使用玩需要自旋等待下一个时间戳
     *
     * @param lastTimestamp
     * @return
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 当前时间戳
     *
     * @return
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 工作 id
     *
     * @return
     */
    public long getWorkerId() {
        return workerId;
    }

}
