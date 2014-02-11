package com.github.paginationspring.bo;

public class BoPaginationPage {
    private int pageNo;
    private boolean currentPage=false;
    public int getPageNo() {
        return pageNo;
    }
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }
    public boolean isCurrentPage() {
        return currentPage;
    }
    public void setCurrentPage(boolean currentPage) {
        this.currentPage = currentPage;
    }

}
