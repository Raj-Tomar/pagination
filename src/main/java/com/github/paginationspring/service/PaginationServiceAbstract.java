package com.github.paginationspring.service;

import java.util.List;

import com.github.paginationspring.bo.BoPaginationPage;
import com.github.paginationspring.bo.BoPaginationParam;
import com.github.paginationspring.bo.BoPaginationResult;
import com.github.paginationspring.bo.BoPaginationResultRow;
import com.github.paginationspring.dao.PaginationDao;

public abstract class PaginationServiceAbstract<P extends BoPaginationParam, BO extends BoPaginationResultRow<?>, ENTITY> implements PaginationService<P> {
    private int noOfPageNumberBeforeCurrentPage = 3;
    private int noOfPageNumberAfterCurrentPage = 3;
    private PaginationDao<ENTITY, P> paginationDao;

    public BoPaginationResult loadBoPaginationResult(P pparam) throws Exception {
        if ( paginationDao == null ) throw new IllegalArgumentException("Please inject paginationDao into your bean.");
        if ( pparam == null ) throw new IllegalArgumentException("pparam cannot be null.");
        if ( Integer.parseInt(pparam.getResultIndex()) < 0 ) return new BoPaginationResult(); // no result to be query
        
        BoPaginationResult paginationResult = new BoPaginationResult();

        int totalSizeOfResult = retrieveCountResult(pparam);
        int totalNoOfPage = new Double(Math.ceil((double) totalSizeOfResult/Integer.parseInt(pparam.getRecordPerPage()))).intValue();

        List<ENTITY> list = retrievePageResult(pparam);

        paginationResult.setTotalNoOfPage(totalNoOfPage);
        paginationResult.setTotalSizeOfResult(totalSizeOfResult);
        paginationResult.setRecordPerPage(Integer.parseInt(pparam.getRecordPerPage()));
        if ( list != null ) {
	        paginationResult.setNoOfRecordOnCurrentPage(list.size());
	        int iit = 0;
	        for ( ENTITY en : list ) {
	            BO bo = assignDataToBo(en);
	            bo.setRowIndex(iit+1+Integer.parseInt(pparam.getResultIndex()));
	            paginationResult.getResultBoList().add(bo);
	            iit++;
	        }
        }
        _assignPageBo(pparam, totalNoOfPage, paginationResult);
        
        return paginationResult;
    }

    protected int retrieveCountResult(P pparam) throws Exception {
        return paginationDao.retrieveCountResult(pparam);
    }
    protected List<ENTITY> retrievePageResult(P pparam) throws Exception {
        return paginationDao.retrievePageResult(pparam);
    }
    protected abstract BO assignDataToBo(final ENTITY en) throws Exception; 

    private void _assignPageBo(P pparam, int totalNoOfPage, BoPaginationResult paginationResult ) {
        
        Integer fr = Integer.parseInt(pparam.getResultIndex());
        Integer mr = Integer.parseInt(pparam.getRecordPerPage());
        int idxOfPre = mr >= ( fr==null ? 0 : fr ) ? 0 : fr - mr;
        paginationResult.setResultIndexOfPreviousPage(idxOfPre);
        paginationResult.setPreviousPageExists(fr!=null && fr!=0);
        
        fr = Integer.parseInt(pparam.getResultIndex());
        int idxOfNext = ( fr==null ? 0 : fr ) + Integer.parseInt(pparam.getRecordPerPage());
        paginationResult.setResultIndexOfNextPage(idxOfNext);

        Integer pc = totalNoOfPage;
        int idxOfLast = ( pc-1 ) * Integer.parseInt(pparam.getRecordPerPage());

        paginationResult.setResultIndexOfLastPage(idxOfLast);
        paginationResult.setNextPageExists(Integer.parseInt(pparam.getResultIndex())<idxOfLast);

        // calculate current page
        Integer firstResult  =  Integer.parseInt(pparam.getResultIndex());
        if ( firstResult == null)
            firstResult = new Integer(0);
        int currentPage = ( firstResult / Integer.parseInt(pparam.getRecordPerPage()) )  + 1 ;
       
        // calculate pageBegin
        int TOTAL_NO_OF_PAGE_NUMBER = noOfPageNumberBeforeCurrentPage + noOfPageNumberAfterCurrentPage + 1;
        int pageBegin = 0;
        if ( totalNoOfPage <= TOTAL_NO_OF_PAGE_NUMBER) {
            pageBegin = 1;
        } else if ( currentPage >= (totalNoOfPage - noOfPageNumberAfterCurrentPage) ) {
            pageBegin = (totalNoOfPage-TOTAL_NO_OF_PAGE_NUMBER)+1;
            if ( pageBegin < 0 ) pageBegin=1;
        } else {
            Integer begin = currentPage - noOfPageNumberBeforeCurrentPage ;
            if ( begin <=0 )
                begin = 1;
            pageBegin=begin;
        }
        
        int pageEnd = 0;
        if ( currentPage <= noOfPageNumberBeforeCurrentPage ) {
            pageEnd = Math.min( totalNoOfPage, TOTAL_NO_OF_PAGE_NUMBER);
        } else {
            pageEnd = Math.min(currentPage + noOfPageNumberAfterCurrentPage , totalNoOfPage );
        }

        paginationResult.setShowDotDotBegin(pageBegin>1);

        // calculate showdotdotend
        boolean showDotDotEnd = false;
        if ( currentPage ==1 ){
            if ( totalNoOfPage > TOTAL_NO_OF_PAGE_NUMBER  ) {
                showDotDotEnd = true;
            } else { 
                showDotDotEnd = false;
            }
        } else {
            if ( pageEnd < totalNoOfPage ){
                showDotDotEnd = true;
            } else {
                showDotDotEnd = false;
            }
        }
        paginationResult.setShowDotDotEnd(showDotDotEnd);
        
        for ( int i=pageBegin; i<=pageEnd; i++ ) {
            BoPaginationPage bean = new BoPaginationPage();
            if ( i == currentPage ) {
                bean.setCurrentPage(true);
                bean.setPageNo(i);
            } else {
                bean.setCurrentPage(false);
                bean.setPageNo(i);
            }
            paginationResult.getListOfPageNos().add(bean);
        }
    }

    public void setPaginationDao(PaginationDao<ENTITY, P> paginationDao) {
        this.paginationDao = paginationDao;
    }

    public void setNoOfPageNumberBeforeCurrentPage(
            int noOfPageNumberBeforeCurrentPage) {
        this.noOfPageNumberBeforeCurrentPage = noOfPageNumberBeforeCurrentPage;
    }

    public void setNoOfPageNumberAfterCurrentPage(int noOfPageNumberAfterCurrentPage) {
        this.noOfPageNumberAfterCurrentPage = noOfPageNumberAfterCurrentPage;
    }
}
