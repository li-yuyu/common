package com.lyle.common.sequence.impl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lyle.common.sequence.Sequence;
import com.lyle.common.sequence.SequenceConstants;
import com.lyle.common.sequence.SequenceRange;
import com.lyle.common.sequence.exception.SequenceException;

/**
 * 序列默认实现
 */
public class DefaultSequence implements Sequence {

    private static final Logger    logger            = LoggerFactory
        .getLogger(SequenceConstants.SEQUENCE_LOG_NAME);

    private final Lock             lock              = new ReentrantLock();

    /** 序列DAO */
    private DefaultSequenceDAO     sequenceDAO;

    /** 默认步长 */
    private static final int       DEFAULT_STEP      = 1000;
    /**默认sequence的最小值*/
    private static final long      DEFAULT_MIN_VALUE = 0;
    /**默认sequence的最大值*/
    private static final long      DEFAULT_MAX_VALUE = Long.MAX_VALUE;
    /**内步长*/
    private int                    innerStep         = DEFAULT_STEP;
    /** 最小值*/
    private long                   minValue          = DEFAULT_MIN_VALUE;
    /** 最大值*/
    private long                   maxValue          = DEFAULT_MAX_VALUE;
    /**序列名称*/
    private String                 sequenceName;
    /** sequence 段*/
    private volatile SequenceRange currentRange;

    /** 是否初始化完成 */
    private volatile boolean       isInitialize      = false;

    /**
     * 在db里如果存在记录的情况下，调用的构造函数,此时不需要再初始化
     *
     * @param sequenceDAO
     * @param sequenceName    序列名称
     * @param innerStep 内步长
     * @param minValue  最小值
     * @param maxValue  最大值
     */

    public DefaultSequence(DefaultSequenceDAO sequenceDAO, String sequenceName, long minValue,
                           long maxValue, int innerStep) {
        this.sequenceDAO = sequenceDAO;
        this.sequenceName = sequenceName;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.innerStep = innerStep;
    }

    /**
     * 初始化各个数据源的sequence，如果不存在记录就插入一条到db
     *
     * @throws SequenceException
     */
    public void init() throws SequenceException {
        if (isInitialize == true) {
            throw new SequenceException("ERROR ## the MultipleSequence has inited");
        }
        if (sequenceDAO == null) {
            throw new IllegalArgumentException("ERROR ## the sequenceDao is null");
        }
        if (sequenceName == null || sequenceName.trim().length() == 0) {
            throw new IllegalArgumentException("ERROR ## the sequenceName is null");
        }
        if (minValue < 0) {
            throw new IllegalArgumentException("ERROR ## the minValue is less than zero");
        }
        if (maxValue < 0) {
            throw new IllegalArgumentException("ERROR ## the maxValue is less than zero");
        }

        try {
            sequenceDAO.initSequenceRecord(sequenceName, minValue, maxValue, innerStep);
        } catch (SequenceException e) {
            throw e;
        }

        logger.warn("WARN ## init the multipleSequence success,the sequenceName = " + sequenceName);
        this.isInitialize = true;
    }

    /**
     * 取得序列下一个值
     *
     * @return
     * @throws SequenceException
     */
    public long nextValue() throws SequenceException {
        if (currentRange == null) {
            lock.lock();
            try {
                if (currentRange == null) {
                    currentRange = sequenceDAO.nextRange(sequenceName, minValue, maxValue,
                        innerStep);
                }
            } finally {
                lock.unlock();
            }
        }

        long value = currentRange.getAndIncrement();
        if (value == -1) {
            lock.lock();
            try {
                for (;;) {
                    if (currentRange.isOver()) {
                        currentRange = sequenceDAO.nextRange(sequenceName, minValue, maxValue,
                            innerStep);
                    }

                    value = currentRange.getAndIncrement();
                    if (value == -1) {
                        continue;
                    }

                    break;
                }
            } finally {
                lock.unlock();
            }
        }

        if (value < 0) {
            throw new SequenceException("Sequence value overflow, value = " + value);
        }

        return value;
    }

    /**
     * 获取配置的序列最小值
     *
     * @return
     */
    public long getMinValue() {
        return this.minValue;
    }

    /**
     * 获取配置的序列最大值
     *
     * @return
     */
    public long getMaxValue() {
        return this.maxValue;
    }

    /**
     * Getter method for property <tt>sequenceDAO</tt>.
     *
     * @return property value of sequenceDAO
     */
    public DefaultSequenceDAO getSequenceDAO() {
        return sequenceDAO;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param sequenceDAO value to be assigned to property sequenceDAO
     */
    public void setSequenceDAO(DefaultSequenceDAO sequenceDAO) {
        this.sequenceDAO = sequenceDAO;
    }

    /**
     * Getter method for property <tt>sequenceName</tt>.
     *
     * @return property value of sequenceName
     */
    public String getSequenceName() {
        return sequenceName;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param sequenceName value to be assigned to property sequenceName
     */
    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }
}
