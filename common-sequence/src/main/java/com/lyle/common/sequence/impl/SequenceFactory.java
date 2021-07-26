package com.lyle.common.sequence.impl;

import com.lyle.common.lang.util.StringUtils;
import com.lyle.common.sequence.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sequence工厂实现类
 */
public class SequenceFactory {

	private static final Logger logger = LoggerFactory.getLogger("dal-sequence");

	private DefaultSequenceDAO sequenceDAO;

	/** Sequence MAP */
	private Map<String, Sequence> sequenceMap = new ConcurrentHashMap<String, Sequence>(0);

	/**
	 * 构造函数
	 * 
	 * @param sequenceDAO
	 */
	public SequenceFactory(DefaultSequenceDAO sequenceDAO) {
		this.sequenceDAO = sequenceDAO;
	}

	/**
	 * 初始化Sequence的工厂 从数据源里获取sequence的记录，对每一条记录进行处理，生成对应的multipleSequence对象，加载到内存中
	 * 
	 * @throws Exception
	 */
	public void init() throws SQLException {
		if (sequenceDAO == null) {
			throw new IllegalArgumentException("The sequenceDao is null!");
		}
		initAllSequence();
	}

	/**
	 * 根据sequenceName初始化Sequence
	 */
	private void initAllSequence() throws SQLException {
		Map<String, Map<String, Object>> sequenceRecords = null;
		// 获取全部的sequence记录
		try {
			sequenceRecords = sequenceDAO.getAllSequenceNameRecord();
			if (sequenceRecords == null) {
				throw new IllegalArgumentException("ERROR ## The sequenceRecord is null!");
			}
			for (Map.Entry<String, Map<String, Object>> sequenceRecord : sequenceRecords.entrySet()) {
				String seqName = sequenceRecord.getKey().trim();
				Map<String, Object> sequeceRecordvalue = sequenceRecord.getValue();
				long min = (Long) sequeceRecordvalue.get(sequenceDAO.getMinValueColumnName());
				long max = (Long) sequeceRecordvalue.get(sequenceDAO.getMaxValueColumnName());
				int step = (Integer) sequeceRecordvalue.get(sequenceDAO.getInnerStepColumnName());
				DefaultSequence sequence = new DefaultSequence(sequenceDAO, seqName, min, max, step);
				try {
					sequence.init();
					sequenceMap.put(seqName, sequence);
				} catch (Exception e) {
					logger.error("ERROR ## init the sequenceName = " + seqName + " has an error:", e);
				}
			}
		} catch (Exception e) {
			logger.error("ERROR ## init the multiple-Sequence-Map failed!", e);
		}

	}

	/**
	 * 根据指定sequenceName生成序号 eg：YYYYYMMDD(8位)+sequence
	 *
	 * @param sequenceName 序列名
	 * @param completion   是否按序列最大值长度补全序列
	 * @return
	 */
	public String genSequence(String sequenceName, boolean completion) {

		return getDate() + (completion ? completion(sequenceName) : getSequence(sequenceName).nextValue());

	}

	/**
	 * 根据指定sequenceName生成序号，不含日期 eg：12345678
	 *
	 * @param sequenceName 序列名
	 * @param completion   是否按序列最大值长度补全序列
	 * @return
	 */
	public String genSequenceNoDate(String sequenceName, boolean completion) {
		return completion ? String.valueOf(completion(sequenceName))
				: String.valueOf(getSequence(sequenceName).nextValue());
	}

	/**
	 * 根据指定sequenceName和业务类型生成序号 eg：YYYYMMDD(8位)+type(4位)+sequence
	 *
	 * @param sequenceName 序列名
	 * @param type         业务类型
	 * @param completion   是否按序列最大值长度补全序列
	 * @return
	 */
	public String genSequence(String sequenceName, SubBusinessType type, boolean completion) {
		return getDate() + type.getWholeCode()
				+ (completion ? completion(sequenceName) : getSequence(sequenceName).nextValue());
	}

	/**
	 * 根据序列名获得固定长度（最大值长度）的序列，长度不足时左边补0
	 *
	 * @return
	 */
	private String completion(String sequenceName) {
		Sequence sequence = getSequence(sequenceName);
		String seqString = String.valueOf(sequence.nextValue());
		return StringUtils.alignRight(seqString, String.valueOf(sequence.getMaxValue()).length(), "0");
	}

	/**
	 * 获取Sequence对象
	 * 
	 * @param sequenceName Sequence名称
	 * @return
	 */
	private Sequence getSequence(String sequenceName) {
		Sequence sequence = sequenceMap.get(sequenceName);
		if (sequence == null) {
			throw new SecurityException("找不到对应的sequence对象。sequenceName=" + sequenceName);
		}
		return sequence;
	}

	/**
	 * 获取日期 YYYYMMDD
	 *
	 * @return
	 */
	private String getDate() {
		// 使用默认时区和语言环境获得一个日历。
		Calendar rightNow = Calendar.getInstance();
		int year = rightNow.get(Calendar.YEAR);
		int month = rightNow.get(Calendar.MONTH) + 1; // 第一个月从0开始，所以得到月份＋1
		int day = rightNow.get(rightNow.DAY_OF_MONTH);
		StringBuilder sb = new StringBuilder();
		sb.append(year).append(month < 10 ? "0" + month : month).append(day < 10 ? "0" + day : day);
		return sb.toString();
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
}
