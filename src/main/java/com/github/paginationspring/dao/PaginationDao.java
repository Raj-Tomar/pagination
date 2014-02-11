package com.github.paginationspring.dao;

import java.util.List;

import com.github.paginationspring.bo.BoPaginationParam;

public interface PaginationDao<E, P extends BoPaginationParam> {

    public int retrieveCountResult(P pparam) throws Exception;
    public List<E> retrievePageResult(P pparam) throws Exception;

}
