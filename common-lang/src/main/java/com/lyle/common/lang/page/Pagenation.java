package com.lyle.common.lang.page;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:
 * @author Lyle
 * @date 2019-06-13
 */
public class Pagenation implements Serializable {

	private static final long serialVersionUID = -7736165980752917500L;

	private static final int DEFAULT_PAGESIZE = 10;

	private int pageNo;
	private int pageSize;
	private int totalRecord;

	private int offset;
	private int totalPage;
	private int prePageNo;
	private int nextPageNo;

	public Pagenation() {
		this.pageNo = 1;
		this.pageSize = DEFAULT_PAGESIZE;
		this.offset = 0;
	}

	public Pagenation(int pageNo, int pageSize) {
		this.pageNo = pageNo <= 0 ? 1 : pageNo;
		this.pageSize = pageSize <= 0 ? DEFAULT_PAGESIZE : pageSize;
		this.offset = (this.pageNo - 1) * this.pageSize;
	}

	public Pagenation(int pageNo, int pageSize, int totalRecord) {
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.offset = (this.pageNo - 1) * this.pageSize;
		this.totalRecord = totalRecord;
		this.totalPage = (int) Math.ceil((double) totalRecord / (double) pageSize);
		this.prePageNo = pageNo <= 1 ? 1 : pageNo - 1;
		this.nextPageNo = pageNo >= this.totalPage ? this.totalPage : pageNo + 1;
	}

	public <T> PageResult<T> toPageResult(int totalRecord, List<T> resultData) {
		this.totalRecord = totalRecord;
		this.totalPage = (int) Math.ceil((double) totalRecord / (double) pageSize);
		this.prePageNo = pageNo <= 1 ? 1 : pageNo - 1;
		this.nextPageNo = pageNo >= this.totalPage ? this.totalPage : pageNo + 1;
		return new PageResult<T>(this, resultData);
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
		this.offset = (this.pageNo - 1) * this.pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		this.offset = (this.pageNo - 1) * this.pageSize;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public int getPageNo() {
		return pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public int getOffset() {
		return offset;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public int getPrePageNo() {
		return prePageNo;
	}

	public int getNextPageNo() {
		return nextPageNo;
	}

}