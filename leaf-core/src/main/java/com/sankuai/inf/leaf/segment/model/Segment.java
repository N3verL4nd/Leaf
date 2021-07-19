package com.sankuai.inf.leaf.segment.model;

import java.util.concurrent.atomic.AtomicLong;

public class Segment {
    /**
     * 获取 SegmentId 的对象
     */
    private AtomicLong value = new AtomicLong(0);
    /**
     * SegmentId 最大值
     */
    private volatile long max;
    /**
     * 步长，动态变更
     */
    private volatile int step;
    private SegmentBuffer buffer;

    public Segment(SegmentBuffer buffer) {
        this.buffer = buffer;
    }

    public AtomicLong getValue() {
        return value;
    }

    public void setValue(AtomicLong value) {
        this.value = value;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public SegmentBuffer getBuffer() {
        return buffer;
    }

    public long getIdle() {
        return this.getMax() - getValue().get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Segment(");
        sb.append("value:");
        sb.append(value);
        sb.append(",max:");
        sb.append(max);
        sb.append(",step:");
        sb.append(step);
        sb.append(")");
        return sb.toString();
    }
}
