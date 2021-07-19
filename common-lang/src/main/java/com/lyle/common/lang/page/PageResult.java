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

    private Pagenation pagenation;

    private List<T> resultData;
    
	public PageResult() {
		super();
	}

	public PageResult(Pagenation pagenation, List<T> resultData) {
		super();
		this.pagenation = pagenation;
		this.resultData = resultData;
	}

	public Pagenation getPage() {
		return pagenation;
	}

	public void setPage(Pagenation page) {
		this.pagenation = page;
	}

	public List<T> getResultData() {
		return resultData;
	}

	public void setResultData(List<T> resultData) {
		this.resultData = resultData;
	}
    
}
