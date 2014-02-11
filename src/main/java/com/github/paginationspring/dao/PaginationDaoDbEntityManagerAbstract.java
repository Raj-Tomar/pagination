package com.github.paginationspring.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.github.paginationspring.bo.BoPaginationParam;

public abstract class PaginationDaoDbEntityManagerAbstract<E, P extends BoPaginationParam> extends PaginationDaoDbAbstract<E, P> {

    private EntityManager entityManager;

    protected int runCountQuery(String countQuery, Map<String, Object> queryParameters) throws Exception {
        if ( entityManager == null ) throw new IllegalArgumentException("Please inject entityManager into your bean.");
        Query query = entityManager.createQuery( countQuery );
        for ( String key : queryParameters.keySet() ) {
            Object value = queryParameters.get(key);
            query.setParameter(key, value);
        }
        int resultcount=0;
        if ( query == null ) resultcount = 0;
        else {
            Long cnt = (Long) query.getSingleResult();
            if ( cnt == null ) resultcount = 0;
            else {
                resultcount = cnt.intValue();
            }
        }
        return resultcount;
    }

    protected List<E> runPageQuery(String pageQuery, Map<String, Object> queryParameters, int resultIndex, int recordPerPage) throws Exception {
        if ( entityManager == null ) throw new IllegalArgumentException("Please inject entityManager into your bean.");
        Query query = entityManager.createQuery(pageQuery);
        for ( String key : queryParameters.keySet() ) {
            Object value = queryParameters.get(key);
            query.setParameter(key, value);
        }
        query.setFirstResult(resultIndex);
        query.setMaxResults(recordPerPage);
        if ( getHints()!=null )
        {
           for ( Map.Entry<String, String> me: getHints().entrySet() )
           {
              query.setHint(me.getKey(), me.getValue());
           }
        }
        if ( query != null ) {
            List<E> list = new ArrayList<E>();
            List<?> result = query.getResultList();
            for ( Object obj : result ) {
                if ( obj instanceof Object[] ) {
                    // for "select a,b.name from Tab1 a join Tab2 b where...order by b.name
                    Object[] objarr = (Object[]) obj;
                    list.add((E) objarr[0]);
                } else {
                    // for "select a from Tab a where...
                    list.add((E) obj);
                }
            }
            if ( list != null && list.size()>0 ) return list;
        }
        return new ArrayList<E>();
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
