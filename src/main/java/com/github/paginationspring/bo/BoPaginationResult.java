package com.github.paginationspring.bo;

import java.util.ArrayList;
import java.util.List;

public class BoPaginationResult {
    private int totalNoOfPage;
    private int totalSizeOfResult; // size from select count(*) statement
    private int recordPerPage; // default 25.
    private int noOfRecordOnCurrentPage;
    private List<BoPaginationResultRow<?>> resultBoList = new ArrayList<BoPaginationResultRow<?>>();
    private List<BoPaginationColumn> columns = new ArrayList<BoPaginationColumn>();

    private boolean optionDisplaySerialNo=true;
    private boolean optionDisplayCheckbox=true;
    private int     optionWidth=950;
    private int     defaultRecordPerPage=25;
    private String  defaultSortName;
    private String  defaultSortAscDesc;
    private String pageLink;
    private boolean ajax=false;
    private boolean rewriteUrl=false;

    private boolean previousPageExists;
    private int resultIndexOfPreviousPage;
    private boolean showDotDotBegin;
    private List<BoPaginationPage> listOfPageNos = new ArrayList<BoPaginationPage>();
    private boolean showDotDotEnd;
    private boolean nextPageExists;
    private int resultIndexOfNextPage;
    private int resultIndexOfLastPage;
    
    public int getTotalNoOfPage() {
        return totalNoOfPage;
    }
    public void setTotalNoOfPage(int totalNoOfPage) {
        this.totalNoOfPage = totalNoOfPage;
    }
    public int getTotalSizeOfResult() {
        return totalSizeOfResult;
    }
    public void setTotalSizeOfResult(int totalSizeOfResult) {
        this.totalSizeOfResult = totalSizeOfResult;
    }
    public int getRecordPerPage() {
        return recordPerPage;
    }
    public void setRecordPerPage(int recordPerPage) {
        this.recordPerPage = recordPerPage;
    }
    public List<BoPaginationResultRow<?>> getResultBoList() {
        return resultBoList;
    }
    public void setResultBoList(List<BoPaginationResultRow<?>> resultBoList) {
        this.resultBoList = resultBoList;
    }
    public boolean isPreviousPageExists() {
        return previousPageExists;
    }
    public void setPreviousPageExists(boolean previousPageExists) {
        this.previousPageExists = previousPageExists;
    }
    public int getResultIndexOfPreviousPage() {
        return resultIndexOfPreviousPage;
    }
    public void setResultIndexOfPreviousPage(int resultIndexOfPreviousPage) {
        this.resultIndexOfPreviousPage = resultIndexOfPreviousPage;
    }
    public boolean isShowDotDotBegin() {
        return showDotDotBegin;
    }
    public void setShowDotDotBegin(boolean showDotDotBegin) {
        this.showDotDotBegin = showDotDotBegin;
    }
    public List<BoPaginationPage> getListOfPageNos() {
        return listOfPageNos;
    }
    public void setListOfPageNos(List<BoPaginationPage> listOfPageNos) {
        this.listOfPageNos = listOfPageNos;
    }
    public boolean isShowDotDotEnd() {
        return showDotDotEnd;
    }
    public void setShowDotDotEnd(boolean showDotDotEnd) {
        this.showDotDotEnd = showDotDotEnd;
    }
    public boolean isNextPageExists() {
        return nextPageExists;
    }
    public void setNextPageExists(boolean nextPageExists) {
        this.nextPageExists = nextPageExists;
    }
    public int getResultIndexOfNextPage() {
        return resultIndexOfNextPage;
    }
    public void setResultIndexOfNextPage(int resultIndexOfNextPage) {
        this.resultIndexOfNextPage = resultIndexOfNextPage;
    }
    public int getResultIndexOfLastPage() {
        return resultIndexOfLastPage;
    }
    public void setResultIndexOfLastPage(int resultIndexOfLastPage) {
        this.resultIndexOfLastPage = resultIndexOfLastPage;
    }
    public int getNoOfRecordOnCurrentPage() {
        return noOfRecordOnCurrentPage;
    }
    public void setNoOfRecordOnCurrentPage(int noOfRecordOnCurrentPage) {
        this.noOfRecordOnCurrentPage = noOfRecordOnCurrentPage;
    }
    public List<BoPaginationColumn> getColumns() {
        return columns;
    }
    public void setColumns(List<BoPaginationColumn> columns) {
        this.columns = columns;
    }
	public boolean isOptionDisplaySerialNo() {
		return optionDisplaySerialNo;
	}
	public void setOptionDisplaySerialNo(boolean optionDisplaySerialNo) {
		this.optionDisplaySerialNo = optionDisplaySerialNo;
	}
	public boolean isOptionDisplayCheckbox() {
		return optionDisplayCheckbox;
	}
	public void setOptionDisplayCheckbox(boolean optionDisplayCheckbox) {
		this.optionDisplayCheckbox = optionDisplayCheckbox;
	}
	public int getOptionWidth() {
		return optionWidth;
	}
	public void setOptionWidth(int optionWidth) {
		this.optionWidth = optionWidth;
	}
	public int getDefaultRecordPerPage() {
		return defaultRecordPerPage;
	}
	public void setDefaultRecordPerPage(int defaultRecordPerPage) {
		this.defaultRecordPerPage = defaultRecordPerPage;
	}
	public String getDefaultSortName() {
		return defaultSortName;
	}
	public void setDefaultSortName(String defaultSortName) {
		this.defaultSortName = defaultSortName;
	}
	public String getDefaultSortAscDesc() {
		return defaultSortAscDesc;
	}
	public void setDefaultSortAscDesc(String defaultSortAscDesc) {
		this.defaultSortAscDesc = defaultSortAscDesc;
	}
	public String getPageLink() {
		return pageLink;
	}
	public void setPageLink(String pageLink) {
		this.pageLink = pageLink;
	}
	public boolean isAjax() {
		return ajax;
	}
	public void setAjax(boolean ajax) {
		this.ajax = ajax;
	}
	public boolean isRewriteUrl() {
		return rewriteUrl;
	}
	public void setRewriteUrl(boolean rewriteUrl) {
		this.rewriteUrl = rewriteUrl;
	}
}
