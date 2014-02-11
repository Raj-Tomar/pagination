package com.github.paginationspring.dao;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.paginationspring.bo.BoPaginationParam;

public abstract class PaginationDaoDbAbstract<E, P extends BoPaginationParam> implements PaginationDao<E, P> {
	private static Log log = LogFactory.getLog(PaginationDaoDbAbstract.class);

    private static final Pattern SUBJECT_PATTERN = Pattern.compile("^select (\\w+((\\s+|\\.)\\w+)*)(\\s*\\,\\s*\\w+(\\.\\w+)*)*\\s+from", Pattern.CASE_INSENSITIVE);
    private static final Pattern FROM_PATTERN = Pattern.compile("(^|\\s)(from)\\s",       Pattern.CASE_INSENSITIVE);
    private static final Pattern WHERE_PATTERN = Pattern.compile("\\s(where)\\s",         Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDER_PATTERN = Pattern.compile("\\s(order)(\\s)+by\\s", Pattern.CASE_INSENSITIVE);
    private static final Pattern GROUP_PATTERN = Pattern.compile("\\s(group)(\\s)+by\\s", Pattern.CASE_INSENSITIVE);

    private static final Pattern ORDER_COLUMN_PATTERN = Pattern.compile("^\\w+(\\.\\w+)*$");

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("(\\:)(\\w+)", Pattern.CASE_INSENSITIVE);

    private static final String DIR_ASC = "asc";
    private static final String DIR_DESC = "desc";

    protected String ejbql;
    protected List<String> restrictions = new ArrayList<String>();
    protected String groupBy;
    protected Map<String, String> hints;
    
    /**
     * This is used to customize query parameters exists in the restrictions array.
     * @param map key is the query parameter name, value object is the object to be binded to query parameters; 
     * If query parameters exist in restrictions array but we did not defined in this object, pparam variable with the same name will be assigned.
     * For example, if :customerName exists in restrictions, then pparam.getCustomerName() will be assigned.
     * If the value object is assigned a null, the corresponding restriction will be ignored and will not be added to query.
     * @param pparam input parameters from your search form
     * @throws Exception
     */
    public abstract void customizeRestrictions(Map<String, Object> queryParameters, P pparam) throws Exception;
    
    protected abstract int runCountQuery(String countQuery, Map<String, Object> queryParameters) throws Exception;
    protected abstract List<E> runPageQuery(String pageQuery, Map<String, Object> queryParameters, int resultIndex, int recordPerPage) throws Exception;
    
    public int retrieveCountResult(P pparam) throws Exception {
        Map<String, Object> queryParameters = evaluateQueryParameters(pparam);
        String countEjbql = createCountQuery(pparam, queryParameters);
        return runCountQuery(countEjbql, queryParameters);
    }
    public List<E> retrievePageResult(P pparam)
            throws Exception {
        Map<String, Object> queryParameters = evaluateQueryParameters(pparam);
        String pageEjbql = createPageQuery(pparam, queryParameters);
        
        return runPageQuery(pageEjbql, queryParameters, Integer.parseInt(pparam.getResultIndex()), Integer.parseInt(pparam.getRecordPerPage()));
    }
    
    protected String createCountQuery(P pparam, Map<String, Object> queryParameters) throws Exception {

        String parsedEjbql = parseEjbql(pparam, queryParameters);

        String countEjbql = getCountEjbql(parsedEjbql);
        
        log.debug("countEjbql="+countEjbql);
        log.debug("queryParameters="+queryParameters);

        return countEjbql;
     }
     
    protected String createPageQuery(P pparam, Map<String, Object> queryParameters) throws Exception {

        String parsedEjbql = parseEjbql(pparam, queryParameters);
        
        String pageEjbql = getPageEjbql(parsedEjbql);
        
        log.debug("pageEjbql="+pageEjbql);
        log.debug("queryParameters="+queryParameters);

        return pageEjbql;
    }
    
    protected String getCountEjbql(String parsedEjbql) throws Exception {
        Matcher fromMatcher = FROM_PATTERN.matcher(parsedEjbql);
        if ( !fromMatcher.find() )
        {
           throw new IllegalArgumentException("no from clause found in query");
        }
        int fromLoc = fromMatcher.start(2);
        
        Matcher orderMatcher = ORDER_PATTERN.matcher(parsedEjbql);
        int orderLoc = orderMatcher.find() ? orderMatcher.start(1) : parsedEjbql.length();

        Matcher groupMatcher = GROUP_PATTERN.matcher(parsedEjbql);
        int groupLoc = groupMatcher.find() ? groupMatcher.start(1) : orderLoc;

        Matcher whereMatcher = WHERE_PATTERN.matcher(parsedEjbql);
        int whereLoc = whereMatcher.find() ? whereMatcher.start(1) : groupLoc;

        String subject;
        if (getGroupBy() != null) {
           subject = "distinct " + getGroupBy();
        }
//        else if (useWildcardAsCountQuerySubject) {
//           subject = "*";
//        }
        // to be JPA-compliant, we need to make this query like "select count(u) from User u"
        // however, Hibernate produces queries some databases cannot run when the primary key is composite
        else {
            Matcher subjectMatcher = SUBJECT_PATTERN.matcher(parsedEjbql);
            if ( subjectMatcher.find() )
            {
               subject = subjectMatcher.group(1);
            }
            else
            {
               throw new IllegalStateException("invalid select clause for query");
            }
        }
        
        return new StringBuilder(parsedEjbql.length() + 15).append("select count(").append(subject).append(") ").
           append(parsedEjbql.substring(fromLoc, whereLoc).replace("join fetch", "join")).
           append(parsedEjbql.substring(whereLoc, groupLoc)).toString().trim();
    }
    
    protected String getPageEjbql(String parsedEjbql) {
        return parsedEjbql;
    }

    protected Map<String, Object> evaluateQueryParameters(P pparam) throws Exception {
        Map<String, Object> tmpParameters = new HashMap<String, Object>();
        customizeRestrictions(tmpParameters, pparam);
        // fill query parameters
        for ( String res : restrictions ) {
            Matcher matcher = PARAMETER_PATTERN.matcher(res);
            if ( matcher.find() ){
                String paramname = matcher.group(2);
                if ( !tmpParameters.containsKey(paramname) ) {
                    try {
                        Object obj = null;
                        Method method = pparam.getClass().getMethod("get"+StringUtils.capitalize(paramname), (Class<?>[]) null );
                        obj = method.invoke(pparam, (Object[]) null );
                        tmpParameters.put(paramname, obj);
                    } catch (Exception e) {
                        log.debug("The following query parameter is null, or not defined in 'customizeRestrictions': "+paramname+". It will not be binded to sql.");
                    }
                }
            } else {
                log.warn("The following restrictions contains bad format: "+res);
            }
        }
        // putting not null parameters into queryParameters
        Map<String, Object> queryParameters = new HashMap<String, Object>();
        for ( String key : tmpParameters.keySet() ) {
            Object obj = tmpParameters.get(key);
            if ( obj != null ) {
                if ( obj instanceof String ) {
                    if ( !StringUtils.isEmpty((String) obj) ) {
                        queryParameters.put(key,obj);
                    }
                } else {
                    queryParameters.put(key,obj);
                }
            }
        }
        
        // print debug
        for ( String key : queryParameters.keySet() ) {
            Object obj = queryParameters.get(key);
            log.debug("key="+key+" value="+obj);
        }
        return queryParameters;
    }
    
    /**
     * Parse the query. When you want to programmatically change the query (instead of use customizeRestrictions method), override this method and change your query before calling super.parseEjbql.
     * @param pparam
     * @param queryParameters
     * @return
     * @throws Exception
     */
    protected String parseEjbql(P pparam, Map<String, Object> queryParameters) throws Exception {

        
        StringBuilder sb = new StringBuilder();
        sb.append(ejbql);

        boolean hasWhereClause = true;
        if ( !WHERE_PATTERN.matcher(ejbql).find() ) {
           sb.append(" where ");
           hasWhereClause = false;
        }
        
        int idx = 0;
        for ( String res : restrictions ) {
            Matcher matcher = PARAMETER_PATTERN.matcher(res);
            if ( matcher.find() ){
                String paramname = matcher.group(2);
                if ( queryParameters.get(paramname) != null ) {
                    res = StringUtils.strip(res);
                    if ( idx==0 && !hasWhereClause ) {
                        sb.append(" ");
                    } else {
                        sb.append(" and ");
                    }
                    sb.append(res);
                    sb.append(" ");
                }
            }
            idx++;
        }
        if (getGroupBy()!=null) {
            sb.append(" group by ").append(getGroupBy());
        }

        String orderBy = processQueryOrder(pparam);
        if (orderBy!=null) {
            sb.append(" order by ").append( orderBy );
        }
        
        return sb.toString();
    }
    
    
    
    protected String processQueryOrder(P pparam) throws Exception {
        if ( StringUtils.isEmpty(pparam.getOrderColumns()) ) return null;

        log.debug("pparam.getOrderColumns()="+pparam.getOrderColumns());
        log.debug("pparam.getOrderDirections()="+pparam.getOrderDirections());
        
        
        StringTokenizer tokens = new StringTokenizer(pparam.getOrderColumns(), ",");
        String propertyPath;
        String propertyDir;
        String token;
        String orderByParameters = new String();
        int tokenpos = 0;
        while (tokens.hasMoreTokens()) {
            
            token = tokens.nextToken().trim();
            
            int dirIndex = token.lastIndexOf(' ');  // Direction, if included, must be preceded by ' '
            
            if (dirIndex != -1) {
                // it is in wrong format
                throw new IllegalArgumentException("Format of OrderColumns is wrong: "+pparam.getOrderColumns());
            } else {
                propertyPath = sanitizeOrderColumn(token);
                if ( !StringUtils.isEmpty(pparam.getOrderDirections()) ) {
                    String dirtoken = null;
                    StringTokenizer dirtokens = new StringTokenizer(pparam.getOrderDirections(), ",");
                    for ( int i=0; dirtokens.hasMoreTokens() && i<=tokenpos; i++ ) {
                        dirtoken = dirtokens.nextToken().trim();
                    }
                    if ( dirtoken != null ) {
                        propertyDir = sanitizeOrderDirection(dirtoken);
                    } else {
                        propertyDir = DIR_ASC;
                    }
                } else {
                    propertyDir = DIR_ASC;
                }
            }

            orderByParameters +=  propertyPath + " " + propertyDir;
            
            if (tokens.hasMoreTokens()) orderByParameters += ", ";
            tokenpos++;
        }
        
        return orderByParameters;
    }

    protected String sanitizeOrderColumn(String columnName) {
        if (columnName == null || columnName.trim().length() == 0) {
            return null;
        } else {
        	return columnName;
        }
    }

    protected String sanitizeOrderDirection(String direction) {
        if (direction == null || direction.length()==0) {
            return null;
        } else if (direction.equalsIgnoreCase(DIR_ASC)) {
            return DIR_ASC;
        } else if (direction.equalsIgnoreCase(DIR_DESC)) {
            return DIR_DESC;
        } else {
            throw new IllegalArgumentException("invalid order direction");
        }
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public Map<String, String> getHints() {
        return hints;
    }

    public void setHints(Map<String, String> hints) {
        this.hints = hints;
    }
}
