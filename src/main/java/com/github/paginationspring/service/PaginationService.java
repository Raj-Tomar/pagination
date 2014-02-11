package com.github.paginationspring.service;

import java.util.List;

import com.github.paginationspring.bo.BoPaginationColumn;
import com.github.paginationspring.bo.BoPaginationParam;
import com.github.paginationspring.bo.BoPaginationResult;

public interface PaginationService<P extends BoPaginationParam> {

    public BoPaginationResult loadBoPaginationResult(P pparam) throws Exception;
    public void assignColumnsDefinition(final List<BoPaginationColumn> columns) throws Exception;
}
