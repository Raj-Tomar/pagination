package com.github.paginationspring.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.github.paginationspring.bo.BoPaginationParam;

public abstract class PaginationDaoDbHibernateTemplateAbstract<E, P extends BoPaginationParam> extends PaginationDaoDbAbstract<E, P> {
	private static Log log = LogFactory.getLog(PaginationDaoDbHibernateTemplateAbstract.class);

	private HibernateTemplate hibernateTemplate;

    protected int runCountQuery(String countQuery, Map<String, Object> queryParameters) throws Exception {
        if ( hibernateTemplate == null ) throw new IllegalArgumentException("Please inject hibernateTemplate into your bean.");
        List<String> keys = new ArrayList<String>();
        List<Object> values = new ArrayList<Object>();
        for ( String key : queryParameters.keySet() ) {
            Object value = queryParameters.get(key);
            keys.add(key);
            values.add(value);
        }
        String[] paramNames = new String[keys.size()];
        int i = 0;
        for ( String key : keys ) {
        	paramNames[i++]=key;
        }
        Object[] vls = values.toArray();
    	
        List result = hibernateTemplate.findByNamedParam(countQuery, paramNames, vls);
        if ( result != null && result.size()>0 ) {
        	Object obj = result.get(0);
        	return new Integer(String.valueOf(obj));
        }
        return 0;
    }
	
    protected List<E> runPageQuery(String pageQuery, Map<String, Object> queryParameters, int resultIndex, int recordPerPage) throws Exception {
        if ( hibernateTemplate == null ) throw new IllegalArgumentException("Please inject hibernateTemplate into your bean.");
        List<String> keys = new ArrayList<String>();
        List<Object> values = new ArrayList<Object>();
        for ( String key : queryParameters.keySet() ) {
            Object value = queryParameters.get(key);
            keys.add(key);
            values.add(value);
        }
        String[] paramNames = new String[keys.size()];
        int i = 0;
        for ( String key : keys ) {
        	paramNames[i++]=key;
        }
        Object[] vls = values.toArray();
    	
        List result = hibernateTemplate.executeFind(
        		new HibernateCallbackImpl(
        				pageQuery,
        				paramNames, 
        				vls,
        				resultIndex,
        				recordPerPage
                )
        );
        
        if ( result != null && result.size()>0 ) {
            List<E> list = new ArrayList<E>();
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

    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
}
class HibernateCallbackImpl implements HibernateCallback {

	private String queryString;
	private String[] paramNames;
	private Object[] values;

	private int firstResult;
	private int maxResults;

	/**
	 * Fetches a {@link List} of entities from the database using pagination.
	 * Execute HQL query, binding a number of values to ":" named parameters in
	 * the query string.
	 * 
	 * @param queryString
	 *            a query expressed in Hibernate's query language
	 * @param paramNames
	 *            the names of the parameters
	 * @param values
	 *            the values of the parameters
	 * @param firstResult
	 *            a row number, numbered from 0
	 * @param maxResults
	 *            the maximum number of rows
	 */
	public HibernateCallbackImpl(String queryString, String[] paramNames,
	        Object[] values, int firstResult, int maxResults) {
		this.queryString = queryString;
		this.paramNames = paramNames;
		this.values = values;

		this.firstResult = firstResult;
		this.maxResults = maxResults;
	}

	public List doInHibernate(Session session) throws HibernateException,
	        SQLException {
		Query query = session.createQuery(queryString);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);

		// TODO: throw proper exception when paramNames.length != values.length

		for (int c = 0; c < paramNames.length; c++) {
			applyNamedParameterToQuery(query, paramNames[c], values[c]);
		}

		List result = query.list();
		return result;
	}

	/**
	 * Code borrowed from org.springframework.orm.hibernate3.HibernateTemplate.
	 * applyNamedParameterToQuery(Query, String, Object)
	 * 
	 * Apply the given name parameter to the given Query object.
	 * 
	 * @param queryObject
	 *            the Query object
	 * @param paramName
	 *            the name of the parameter
	 * @param value
	 *            the value of the parameter
	 * @throws HibernateException
	 *             if thrown by the Query object
	 */
	protected void applyNamedParameterToQuery(Query queryObject,
	        String paramName, Object value) throws HibernateException {

		if (value instanceof Collection) {
			queryObject.setParameterList(paramName, (Collection) value);
		} else if (value instanceof Object[]) {
			queryObject.setParameterList(paramName, (Object[]) value);
		} else {
			queryObject.setParameter(paramName, value);
		}
	}

}
