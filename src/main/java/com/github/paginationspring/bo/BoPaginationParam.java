package com.github.paginationspring.bo;


public class BoPaginationParam {
    private String resultIndex;
    private String recordPerPage;
    private String sortName;
    private String sortAscDesc;
    private String orderColumns;
    private String orderDirections;
    private String selectall;
    private String[] selectedIds;

    public String[] getSelectedIds() {
		return selectedIds;
	}
	public void setSelectedIds(String[] selectedIds) {
		this.selectedIds = selectedIds;
	}
	public String getResultIndex() {
        return resultIndex;
    }
    public void setResultIndex(String resultIndex) {
        this.resultIndex = resultIndex;
    }
    public String getRecordPerPage() {
        return recordPerPage;
    }
    public void setRecordPerPage(String recordPerPage) {
        this.recordPerPage = recordPerPage;
    }
    public String getSortName() {
        return sortName;
    }
    public void setSortName(String sortName) {
        this.sortName = sortName;
    }
    public String getSortAscDesc() {
        return sortAscDesc;
    }
    public void setSortAscDesc(String sortAscDesc) {
        this.sortAscDesc = sortAscDesc;
    }
    public String getOrderColumns() {
        return orderColumns;
    }
    public void setOrderColumns(String orderColumns) {
        this.orderColumns = orderColumns;
    }
    public String getOrderDirections() {
        return orderDirections;
    }
    public void setOrderDirections(String orderDirections) {
        this.orderDirections = orderDirections;
    }
	public String getSelectall() {
		return selectall;
	}
	public void setSelectall(String selectall) {
		this.selectall = selectall;
	}
}
