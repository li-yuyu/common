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
 * 高可用 sequence 序列实现,sequence包含名称、当前值、步长、最小值、最大值、创建时间和修改时间等属性。
 * 采用乐观锁的方式实现从db获取当前sequence段，取完缓存起来，供业务使用
 */
public class MultipleSequence implements Sequence {

	private static final Logger logger = LoggerFactory.getLogger(SequenceConstants.SEQUENCE_LOG_NAME);

	private final Lock lock = new ReentrantLock();

	/**
	 * 默认步长
	 */
	private static final int DEFAULT_STEP = 1000;
	/**
	 * 默认sequence的最小值
	 */
	private static final long DEFAULT_MIN_VALUE = 0;
	/**
	 * 默认sequence的最大值
	 */
	private static final long DEFAULT_MAX_VALUE = Long.MAX_VALUE;
	/**
	 * 内步长
	 */
	private int innerStep = DEFAULT_STEP;
	/**
	 * 最小值
	 */
	private long minValue = DEFAULT_MIN_VALUE;
	/**
	 * 最大值
	 */
	private long maxValue = DEFAULT_MAX_VALUE;
	/**
	 * 序列名称
	 */
	private String sequenceName;
	/**
	 * sequence 段
	 */
	private volatile SequenceRange currentRange;
	/**
	 * 数据源的包装器
	 */
	private MultipleSequenceDAO sequenceDAO;

	private volatile boolean isInitialize = false;

	public MultipleSequence() {

	}

	/**
	 * 在db里如果存在记录的情况下，调用的构造函数,此时不需要再初始化
	 *
	 * @param sequenceDAO
	 * @param sequenceName 序列名称
	 * @param innerStep    内步长
	 * @param minValue     最小值
	 * @param maxValue     最大值
	 */

	public MultipleSequence(MultipleSequenceDAO sequenceDAO, String sequenceName, long minValue, long maxValue,
			int innerStep) {
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
		if (isInitialize) {
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
		// 初始化该sequence
		try {
			sequenceDAO.initSequenceRecord(sequenceName, minValue, maxValue, innerStep);
		} catch (SequenceException e) {
			throw e;
		}

		logger.info("WARN ## init the multipleSequence success,the sequenceName = " + sequenceName);
		this.isInitialize = true;
	}

	/**
	 * 获取sequence值
	 */
	public long nextValue() throws SequenceException {
		if (this.isInitialize == false) {
			throw new SequenceException("ERROR ## the MultipleSequence is not init");
		}
		if (currentRange == null) {
			lock.lock();
			try {
				if (currentRange == null) {
					currentRange = sequenceDAO.nextRange(sequenceName, minValue, maxValue, innerStep);
					logger.warn("WARN ## get the sequence range, from " + currentRange.getMin() + " to "
							+ currentRange.getMax() + ",the sequenceName = " + sequenceName);
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
						currentRange = sequenceDAO.nextRange(sequenceName, minValue, maxValue, innerStep);
						logger.warn("WARN ## after over,get the sequence range, from " + currentRange.getMin() + " to "
								+ currentRange.getMax() + ",the sequenceName = " + sequenceName);
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

	public MultipleSequenceDAO getSequenceDAO() {
		return sequenceDAO;
	}

	public void setSequenceDao(MultipleSequenceDAO sequenceDAO) {
		this.sequenceDAO = sequenceDAO;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String name) {
		this.sequenceName = name;
	}

	public int getInnerStep() {
		return innerStep;
	}

	public void setInnerStep(int innerStep) {
		this.innerStep = innerStep;
	}

	public long getMinValue() {
		return minValue;
	}

	public void setMinValue(long minValue) {
		this.minValue = minValue;
	}

	public long getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(long maxValue) {
		this.maxValue = maxValue;
	}
}
