package com.lyle.common.sequence.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lyle.common.sequence.SequenceConstants;
import com.lyle.common.sequence.SequenceRange;
import com.lyle.common.sequence.exception.SequenceException;

/**
 * 序列DAO默认实现，JDBC方式
 */
public class DefaultSequenceDAO {

	private static final Logger logger = LoggerFactory.getLogger(SequenceConstants.SEQUENCE_LOG_NAME);

	/** 序列数据源持有类 */
	private SequenceDataSourceHolder sequenceDataSourceHolder;

	/** sequence表名默认值 */
	private static final String DEFAULT_TABLE_NAME = "sequence";

	/**
	 * 以下表结构字段默认值 分别是：sequence名称、sequence当前值、库的标号、最小值、最大值、内步长、创建时间以及修改时间
	 */
	private static final String DEFAULT_NAME_COLUMN_NAME = "name";
	private static final String DEFAULT_VALUE_COLUMN_NAME = "value";
	private static final String DEFAULT_MIN_VALUE_COLUMN_NAME = "min_value";
	private static final String DEFAULT_MAX_VALUE_COLUMN_NAME = "max_value";
	private static final String DEFAULT_INNER_STEP_COLUMN_NAME = "step";
	private static final String DEFAULT_GMT_CREATE_COLUMN_NAME = "gmt_create";
	private static final String DEFAULT_GMT_MODIFIED_COLUMN_NAME = "gmt_modified";

	/** 序列所在的表名 默认为 sequence */
	private String tableName = DEFAULT_TABLE_NAME;
	/** 存储序列名称的列名 默认为 name */
	private String nameColumnName = DEFAULT_NAME_COLUMN_NAME;
	/** 存储序列值的列名 默认为 value */
	private String valueColumnName = DEFAULT_VALUE_COLUMN_NAME;

	/** 最小值的列名 默认为 min_value */
	private String minValueColumnName = DEFAULT_MIN_VALUE_COLUMN_NAME;
	/** 最大值的列名 默认为max_value */
	private String maxValueColumnName = DEFAULT_MAX_VALUE_COLUMN_NAME;
	/** 内步长的列名 默认为step */
	private String innerStepColumnName = DEFAULT_INNER_STEP_COLUMN_NAME;

	/** 创建时间 默认为gmt_create */
	private String gmtCreateColumnName = DEFAULT_GMT_CREATE_COLUMN_NAME;
	/** 存储序列最后更新时间的列名 默认为 gmt_modified */
	private String gmtModifiedColumnName = DEFAULT_GMT_MODIFIED_COLUMN_NAME;

	/** 重试次数 */
	private static final int DEFAULT_RETRY_TIMES = 150;

	/** sequence的最大值=Long.MAX_VALUE-DELTA，超过这个值就说明sequence溢出了. */
	private static final long DELTA = 100000000L;

	/**
	 * 重试次数
	 */
	private int retryTimes = DEFAULT_RETRY_TIMES;

	/**
	 * 查询sequence记录的sql<br>
	 * 格式：select value from sequence where name=?
	 */
	private String selectSql;

	/**
	 * 更新sequence记录的sql<br>
	 * 格式 update table_name(default：sequence) set value=? ,gmt_modified=? where
	 * name= and value=?
	 */
	private String updateSql;

	/**
	 * 插入sequence记录的sql<br>
	 * 格式: insert into
	 * table_name(default:sequence)(name,value,min_value,max_value,step,gmt_create,gmt_modified)
	 * values(?,?,?,?,?,?,?)
	 */
	private String insertSql;

	/** 格式：select value from table_name(default:sequence) where name=? */
	/**
	 * 获取db里所有sequence记录的sql<br>
	 * 格式：select name,value,min_value,max_value,step from sequence
	 */
	private String selectAllRecordSql;

	/**
	 * 根据sequence name获取一条sequence记录<br>
	 * 格式：select value,min_value,max_value,step from sequence where name=?
	 */
	private String selectSeqRecordSql;

	/** 调整开关 adjust 默认true */
	private Boolean adjust = true;

	/** DefaultSequenceDao是否已经初始化 */
	private volatile boolean isInitialize = false;

	/**
	 * 初始化multiSequenceDao<br>
	 * 1）获取数据源的个数；2）生成随机对象； 3）初始化各个数据源包装器的sql等参数 4）如果配置了log库，则初始化异步log库
	 * 
	 * @throws SequenceException
	 */
	public void init() {
		if (isInitialize == true) {
			throw new SequenceException("ERROR ## the DefaultSequenceDao has inited");
		}

		sequenceDataSourceHolder.setParameters(getTableName(), getSelectSql(), getUpdateSql(), getInsertSql(), adjust);

		isInitialize = true;
	}

	/**
	 * 初始化sequence的初始值,每个数据源都要去检查一遍，如果不存在就插入一条记录
	 *
	 * @param sequenceName sequence名称
	 * @throws SequenceException
	 */
	public void initSequenceRecord(String sequenceName, long minValue, long maxValue, int innerStep)
			throws SequenceException {
		if (!isInitialize) {
			throw new SequenceException("ERROR ## please init the DefaultSequenceDAO first");
		}

		sequenceDataSourceHolder.initSequenceRecord(0, sequenceName, innerStep, innerStep, minValue, maxValue,
				getValueColumnName());

		isInitialize = true;

	}

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
	public SequenceRange nextRange(String name, long minValue, long maxValue, int innerStep) throws SequenceException {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("序列名称不能为空");
		}

		long oldValue;
		long newValue;

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		for (int i = 0; i < retryTimes + 1; ++i) {
			SequenceRange sequenceRange = sequenceDataSourceHolder.tryOnSelectedDataSource(0, name, minValue, maxValue,
					innerStep, innerStep, new ArrayList<Integer>(0));

			if (sequenceRange == null) {
				logger.warn("WARN ## 重试去取 sequenceRange，第" + (i + 1) + "次尝试!");
				continue;
			}
			return sequenceRange;
		}

		throw new SequenceException("Retried too many times, retryTimes = " + retryTimes);
	}

	/**
	 * 获取当前db里所有的sequence记录的指定字段值
	 *
	 * @param selectSql           ：select name,min_value,max_value,step from
	 *                            table_name(default:sequence)
	 * @param nameColumn          sequence列名
	 * @param minValueColumnName  最小值
	 * @param maxValueColumnName  最大值
	 * @param innerStepColumnName 内步长
	 * @return Map<String, Map<String, Object>>
	 *         外层key标识sequence名字，内层key表示最小、最大值以及步长等；
	 * @throws SQLException
	 */
	public Map<String, Map<String, Object>> getAllSequenceRecordName(String selectSql, String nameColumn,
			String minValueColumnName, String maxValueColumnName, String innerStepColumnName) throws SQLException {

		Map<String, Map<String, Object>> records = new HashMap<String, Map<String, Object>>(0);
		return sequenceDataSourceHolder.getAllSequenceRecordName(selectSql, nameColumn, minValueColumnName,
				maxValueColumnName, innerStepColumnName);
	}

	/**
	 * 获取所有的sequence记录
	 *
	 * @return
	 * @throws SQLException
	 */
	public Map<String, Map<String, Object>> getAllSequenceNameRecord() throws SQLException {
		Map<String, Map<String, Object>> sequenceRecordMap = sequenceDataSourceHolder.getAllSequenceRecordName(
				getSelectAllRecord(), getNameColumnName(), getMinValueColumnName(), getMaxValueColumnName(),
				getInnerStepColumnName());

		return sequenceRecordMap;
	}

	/** 格式：select value from table_name(default:sequence) where name=? */
	private String getSelectSql() {
		if (selectSql == null) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("select ").append(getValueColumnName());
			buffer.append(" from ").append(getTableName());
			buffer.append(" where ").append(getNameColumnName()).append(" = ?");
			selectSql = buffer.toString();
		}
		return selectSql;
	}

	/**
	 * 格式：select name,value,min_value,max_value,step from
	 * table_name(default:sequence)
	 */
	public String getSelectAllRecord() {
		if (selectAllRecordSql == null) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("select ").append(getNameColumnName()).append(",");
			buffer.append(this.getValueColumnName()).append(",");
			buffer.append(this.getMinValueColumnName()).append(",");
			buffer.append(this.getMaxValueColumnName()).append(",");
			buffer.append(this.getInnerStepColumnName());
			buffer.append(" from ").append(getTableName());
			selectAllRecordSql = buffer.toString();
		}
		return selectAllRecordSql;
	}

	/** 格式：select value,min_value,max_value,step from sequence where name=? */
	public String getSequenceRecordSql() {
		if (selectSeqRecordSql == null) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("select ").append(getNameColumnName()).append(",");
			buffer.append(this.getValueColumnName()).append(",");
			buffer.append(this.getMinValueColumnName()).append(",");
			buffer.append(this.getMaxValueColumnName()).append(",");
			buffer.append(this.getInnerStepColumnName());
			buffer.append(" from ").append(getTableName());
			buffer.append(" where ").append(getNameColumnName()).append("= ?");
			selectSeqRecordSql = buffer.toString();
		}
		return selectSeqRecordSql;
	}

	/**
	 * 格式 update table_name(default：sequence) set value=? ,gmt_modified=? where
	 * name=? and value=?
	 */
	private String getUpdateSql() {
		if (updateSql == null) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("update ").append(getTableName());
			buffer.append(" set ").append(getValueColumnName()).append(" = ?, ");
			buffer.append(getGmtModifiedColumnName()).append(" = ? where ");
			buffer.append(getNameColumnName()).append(" = ? and ");
			buffer.append(getValueColumnName()).append("=?");
			updateSql = buffer.toString();
		}
		return updateSql;
	}

	/**
	 * 格式: insert into
	 * table_name(default:sequence)(name,value,min_value,max_value,step,gmt_create,gmt_modified)
	 * values(?,?,?,?,?,?,?)
	 */
	private String getInsertSql() {
		if (insertSql == null) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("insert into ").append(getTableName()).append("(");
			buffer.append(getNameColumnName()).append(",");
			buffer.append(getValueColumnName()).append(",");
			buffer.append(getMinValueColumnName()).append(",");
			buffer.append(getMaxValueColumnName()).append(",");
			buffer.append(getInnerStepColumnName()).append(",");
			buffer.append(getGmtCreateColumnName()).append(",");
			buffer.append(getGmtModifiedColumnName()).append(") values(?,?,?,?,?,?,?);");
			insertSql = buffer.toString();
		}
		return insertSql;
	}

	/**
	 * Getter method for property <tt>retryTimes</tt>.
	 *
	 * @return property value of retryTimes
	 */
	public int getRetryTimes() {
		return retryTimes;
	}

	/**
	 * Setter method for property <tt>counterType</tt>.
	 *
	 * @param retryTimes value to be assigned to property retryTimes
	 */
	public void setRetryTimes(int retryTimes) {
		if (retryTimes < 0) {
			throw new IllegalArgumentException(
					"Property retryTimes cannot be less than zero, retryTimes = " + retryTimes);
		}
		this.retryTimes = retryTimes;
	}

	/**
	 * Getter method for property <tt>tableName</tt>.
	 *
	 * @return property value of tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Setter method for property <tt>counterType</tt>.
	 *
	 * @param tableName value to be assigned to property tableName
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Getter method for property <tt>nameColumnName</tt>.
	 *
	 * @return property value of nameColumnName
	 */
	public String getNameColumnName() {
		return nameColumnName;
	}

	/**
	 * Setter method for property <tt>counterType</tt>.
	 *
	 * @param nameColumnName value to be assigned to property nameColumnName
	 */
	public void setNameColumnName(String nameColumnName) {
		this.nameColumnName = nameColumnName;
	}

	/**
	 * Getter method for property <tt>valueColumnName</tt>.
	 *
	 * @return property value of valueColumnName
	 */
	public String getValueColumnName() {
		return valueColumnName;
	}

	/**
	 * Setter method for property <tt>counterType</tt>.
	 *
	 * @param valueColumnName value to be assigned to property valueColumnName
	 */
	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}

	/**
	 * Getter method for property <tt>minValueColumnName</tt>.
	 *
	 * @return property value of minValueColumnName
	 */
	public String getMinValueColumnName() {
		return minValueColumnName;
	}

	/**
	 * Setter method for property <tt>counterType</tt>.
	 *
	 * @param minValueColumnName value to be assigned to property minValueColumnName
	 */
	public void setMinValueColumnName(String minValueColumnName) {
		this.minValueColumnName = minValueColumnName;
	}

	/**
	 * Getter method for property <tt>maxValueColumnName</tt>.
	 *
	 * @return property value of maxValueColumnName
	 */
	public String getMaxValueColumnName() {
		return maxValueColumnName;
	}

	/**
	 * Setter method for property <tt>counterType</tt>.
	 *
	 * @param maxValueColumnName value to be assigned to property maxValueColumnName
	 */
	public void setMaxValueColumnName(String maxValueColumnName) {
		this.maxValueColumnName = maxValueColumnName;
	}

	/**
	 * Getter method for property <tt>innerStepColumnName</tt>.
	 *
	 * @return property value of innerStepColumnName
	 */
	public String getInnerStepColumnName() {
		return innerStepColumnName;
	}

	/**
	 * Setter method for property <tt>counterType</tt>.
	 *
	 * @param innerStepColumnName value to be assigned to property
	 *                            innerStepColumnName
	 */
	public void setInnerStepColumnName(String innerStepColumnName) {
		this.innerStepColumnName = innerStepColumnName;
	}

	/**
	 * Getter method for property <tt>gmtCreateColumnName</tt>.
	 *
	 * @return property value of gmtCreateColumnName
	 */
	public String getGmtCreateColumnName() {
		return gmtCreateColumnName;
	}

	/**
	 * Setter method for property <tt>counterType</tt>.
	 *
	 * @param gmtCreateColumnName value to be assigned to property
	 *                            gmtCreateColumnName
	 */
	public void setGmtCreateColumnName(String gmtCreateColumnName) {
		this.gmtCreateColumnName = gmtCreateColumnName;
	}

	/**
	 * Getter method for property <tt>gmtModifiedColumnName</tt>.
	 *
	 * @return property value of gmtModifiedColumnName
	 */
	public String getGmtModifiedColumnName() {
		return gmtModifiedColumnName;
	}

	/**
	 * Setter method for property <tt>counterType</tt>.
	 *
	 * @param gmtModifiedColumnName value to be assigned to property
	 *                              gmtModifiedColumnName
	 */
	public void setGmtModifiedColumnName(String gmtModifiedColumnName) {
		this.gmtModifiedColumnName = gmtModifiedColumnName;
	}

	/**
	 * Getter method for property <tt>sequenceDataSourceHolder</tt>.
	 *
	 * @return property value of sequenceDataSourceHolder
	 */
	public SequenceDataSourceHolder getSequenceDataSourceHolder() {
		return sequenceDataSourceHolder;
	}

	/**
	 * Setter method for property <tt>counterType</tt>.
	 *
	 * @param sequenceDataSourceHolder value to be assigned to property
	 *                                 sequenceDataSourceHolder
	 */
	public void setSequenceDataSourceHolder(SequenceDataSourceHolder sequenceDataSourceHolder) {
		this.sequenceDataSourceHolder = sequenceDataSourceHolder;
	}
}
