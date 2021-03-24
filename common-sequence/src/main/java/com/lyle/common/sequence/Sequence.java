package com.lyle.common.sequence;

import com.lyle.common.sequence.exception.SequenceException;

/**
 * 序列接口
 */
public interface Sequence {

	/**
	 * 取得序列下一个值
	 *
	 * @return 返回序列下一个值
	 * @throws SequenceException
	 */
	long nextValue() throws SequenceException;

	/**
	 * 获取配置的序列最小值
	 * 
	 * @return
	 */
	long getMinValue();

	/**
	 * 获取配置的序列最大值
	 *
	 * @return
	 */
	long getMaxValue();

}
