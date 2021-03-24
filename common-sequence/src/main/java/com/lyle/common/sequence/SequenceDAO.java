package com.lyle.common.sequence;

import java.sql.SQLException;
import java.util.Map;

import com.lyle.common.sequence.exception.SequenceException;

/**
 * 序列DAO接口
 */
public interface SequenceDAO {

	/**
	 * 取得下一个可用的序列区间
	 *
	 * @param name      序列名称
	 * @param minValue  序列最小值
	 * @param maxValue  序列最大值
	 * @param innerStep 序列步长
	 * @return 返回下一个可用的序列区间
	 * @throws SequenceException
	 */
	SequenceRange nextRange(String name, long minValue, long maxValue, int innerStep) throws SequenceException;

	/**
	 * 获取所有的sequence记录
	 *
	 * @return
	 * @throws SQLException
	 */
	Map<String, Map<String, Object>> getAllSequenceNameRecord() throws SQLException;

}
