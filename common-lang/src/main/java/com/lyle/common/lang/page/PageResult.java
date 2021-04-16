package com.lyle.common.lang.page;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:
 * @author Lyle
 * @date 2019-06-13
 * @param <T>
 */
public class PageResult<T> implements Serializable {

	private static final long serialVersionUID = -6064104564147446671L;

    private Page page;

    private List<T> resultData;
    
	public PageResult() {
		super();
	}

	public PageResult(Page page, List<T> resultData) {
		super();
		this.page = page;
		this.resultData = resultData;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public List<T> getResultData() {
		return resultData;
	}

	public void setResultData(List<T> resultData) {
		this.resultData = resultData;
	}
    
}
