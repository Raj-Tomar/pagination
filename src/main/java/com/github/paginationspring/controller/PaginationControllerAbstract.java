package com.github.paginationspring.controller;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.paginationspring.bo.BoPaginationColumn;
import com.github.paginationspring.bo.BoPaginationParam;
import com.github.paginationspring.bo.BoPaginationResult;
import com.github.paginationspring.service.PaginationService;

public abstract class PaginationControllerAbstract<P extends BoPaginationParam> {
    private static Log log = LogFactory.getLog(PaginationControllerAbstract.class);

    protected static final String URL1="";
    protected static final String URL2="/{resultIndex}";
    protected static final String URL3="/sort/{sortName}/{sortAscDesc}";
    protected static final String URL4="/{resultIndex}/sort/{sortName}/{sortAscDesc}";
    
    protected static final String PPARAM		="pparam";
    protected static final String BUTTON_ACTION	="buttonAction";
    protected static final String SEARCH_BUTTON	="searchButton";
    protected static final String CLEAR_BUTTON	="clearButton";
    
    private boolean optionDisplaySerialNo=true;
    private boolean optionDisplayCheckbox=true;
    private int     optionWidth=950;
    private int     defaultRecordPerPage=25;
    private String  defaultSortName;
    private String  defaultSortAscDesc;

    private PaginationService<P> paginationService;
    private String pageLink;
    private boolean ajax=false;
    private boolean rewriteUrl=false;

    private static final Pattern INTEGER_PATTERN = Pattern.compile("[0-9]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDERCOL_PATTERN = Pattern.compile("[a-zA-z\\.\\_\\,\\s]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDERDIR_PATTERN = Pattern.compile("^(asc|desc)(\\s*\\,\\s*(asc|desc))*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SORTASCDESC_PATTERN = Pattern.compile("(a|d)");
    
    protected Map<String, Object> assignModel(P pparam, String buttonAction) throws Exception {
    	return assignModel(pparam, buttonAction, true);
    }
    protected Map<String, Object> assignModel(P pparam, String buttonAction, boolean loadResult) throws Exception {
        log.debug("begin");
        if ( pparam == null ) throw new IllegalArgumentException("pparam cannot be null.");
        if ( paginationService == null ) throw new IllegalArgumentException("Please set paginationService into your bean.");
        if ( pageLink == null ) throw new IllegalArgumentException("Please set pageLink into your bean.");

        log.debug("buttonAction="+buttonAction);
        if ( StringUtils.isEmpty(pparam.getResultIndex()) ) pparam.setResultIndex("0");
        P internalParam = (P) pparam.getClass().newInstance();
        if ( CLEAR_BUTTON.equalsIgnoreCase(buttonAction) ) {
//            pparam = (P) pparam.getClass().newInstance();
            _clearProperties(pparam);
            pparam.setResultIndex("0");
            internalParam.setResultIndex("0");
            whenClearButtonIsClick(internalParam);
        } else if ( SEARCH_BUTTON.equalsIgnoreCase(buttonAction) ) {
            pparam.setResultIndex("0");
            pparam.setSelectedIds(null);
            _copyProperties(internalParam, pparam);
            whenSearchButtonIsClick(internalParam);
        } else {
            _copyProperties(internalParam, pparam);
        }

        if ( StringUtils.isEmpty(internalParam.getResultIndex()) )      internalParam.setResultIndex("0");
        if ( StringUtils.isEmpty(internalParam.getRecordPerPage()) )    internalParam.setRecordPerPage(String.valueOf(defaultRecordPerPage));
        if ( StringUtils.isEmpty(internalParam.getSortName()) )         internalParam.setSortName(defaultSortName);
        if ( StringUtils.isEmpty(internalParam.getSortAscDesc()) )      internalParam.setSortAscDesc(defaultSortAscDesc);
        
        // load columns definition
        List<BoPaginationColumn> columns = new ArrayList<BoPaginationColumn>();
        paginationService.assignColumnsDefinition(columns);
        if ( !StringUtils.isEmpty(internalParam.getSortName()) ) {
            for ( BoPaginationColumn column : columns ) {
                if ( internalParam.getSortName().equals(column.getColumnName()) ) {
                    internalParam.setOrderColumns(column.getOrderColumns());
                    if ( !StringUtils.isEmpty(internalParam.getSortAscDesc()) && !StringUtils.isEmpty(column.getOrderDirections()) ) {
                        if ( "a".equals(internalParam.getSortAscDesc()) && column.getOrderDirections().toLowerCase().startsWith("desc") ) {
                            internalParam.setOrderDirections(_reverseOrderDirections(column.getOrderDirections()));
                        } else if ( "d".equals(internalParam.getSortAscDesc()) && column.getOrderDirections().toLowerCase().startsWith("asc") ) {
                            internalParam.setOrderDirections(_reverseOrderDirections(column.getOrderDirections()));
                        } else {
                            internalParam.setOrderDirections(column.getOrderDirections());
                        }
                    }
                    break;
                }
            }
        }
        
        validation(internalParam);

        BoPaginationResult paginationResult = null;
        if ( loadResult ) {
	        paginationResult = paginationService.loadBoPaginationResult(internalParam);
	        paginationResult.setColumns(columns);
        } else {
        	paginationResult = new BoPaginationResult();
        }
        
        paginationResult.setOptionDisplaySerialNo(optionDisplaySerialNo);
        paginationResult.setOptionDisplayCheckbox(optionDisplayCheckbox);
        paginationResult.setOptionWidth(optionWidth);
        paginationResult.setDefaultRecordPerPage(defaultRecordPerPage);
        paginationResult.setDefaultSortName(defaultSortName);
        paginationResult.setDefaultSortAscDesc(defaultSortAscDesc);
        paginationResult.setPageLink(pageLink);
        paginationResult.setAjax(ajax);
        paginationResult.setRewriteUrl(rewriteUrl);
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pparam", pparam);
        map.put("paginationResult", paginationResult);
        return map;
    }
    
    protected void whenSearchButtonIsClick(final P internalParam) {
    	log.debug("whenSearchButtonIsClick");
    }
    
    protected void whenClearButtonIsClick(final P internalParam) {
    	log.debug("whenClearButtonIsClick");
    }
    
    protected void validation(final P internalParam) throws IllegalArgumentException {
        // do some validation
        Matcher matcher = null;
        if ( !StringUtils.isEmpty(internalParam.getOrderColumns()) ) {
        	/* need better implementation to check orderColumns format
            matcher = ORDERCOL_PATTERN.matcher(internalParam.getOrderColumns());
            if ( !matcher.matches() ) {
                throw new IllegalArgumentException("Format of OrderColumns is wrong: "+internalParam.getOrderColumns());
            }
            */
            if ( internalParam.getOrderColumns().indexOf(" asc") != -1 || internalParam.getOrderColumns().indexOf(" desc") != -1 ) {
                throw new IllegalArgumentException("OrderColumns must not contain asc/desc (should be in orderDirections): "+internalParam.getOrderColumns());
            }
        }
        if ( !StringUtils.isEmpty(internalParam.getOrderColumns()) && StringUtils.isEmpty(internalParam.getOrderDirections()) ) {
            throw new IllegalArgumentException("You must define OrderDirections on column: "+internalParam.getOrderColumns());
        }
        if ( !StringUtils.isEmpty(internalParam.getOrderDirections()) ) {
            matcher = ORDERDIR_PATTERN.matcher(internalParam.getOrderDirections());
            if ( !matcher.matches() ) {
                throw new IllegalArgumentException("Format of OrderDirection is wrong: "+internalParam.getOrderDirections());
            }
        }
        if ( !StringUtils.isEmpty(internalParam.getSortAscDesc()) ) {
            matcher = SORTASCDESC_PATTERN.matcher(internalParam.getSortAscDesc());
            if ( !matcher.matches() ) {
                throw new IllegalArgumentException("Format of SortAscDesc is wrong: "+internalParam.getSortAscDesc());
            }
        }
        matcher = INTEGER_PATTERN.matcher(internalParam.getResultIndex());
        if ( !matcher.matches() ) {
            throw new IllegalArgumentException("Format of ResultIndex is wrong: "+internalParam.getResultIndex());
        }
        matcher = INTEGER_PATTERN.matcher(internalParam.getRecordPerPage());
        if ( !matcher.matches() ) {
            throw new IllegalArgumentException("Format of RecordPerPage is wrong: "+internalParam.getRecordPerPage());
        }
    }
    private static void _copyProperties(Object pojoTarget, Object pojoSource) {
        // putting Pagination Parameter into paramsInTag
        Class<?> curcls = pojoSource.getClass();
        // loop for all the superclass until instanceof Object
        while ( curcls != null && !curcls.getName().equals(Object.class.getName()) ) {
            // all the declared methods
            Method[] methods = curcls.getDeclaredMethods();
            for ( int i =0; methods!= null && i<methods.length; i++ ) {
                Method method = methods[i];
                // all the getter
                if ( method.getName().startsWith("get") || method.getName().startsWith("is") ) {
                    try {
                        Pattern pattern = Pattern.compile("[A-Z].*");
                        Matcher matcher = pattern.matcher(method.getName());
                        String varname = null;
                        if ( matcher.find() ) {
                            varname = matcher.group();
                        }
                        Object srcResult = runMethod(pojoSource, method.getName(), null, null);
                        if ( srcResult != null ) {
                            runMethod(pojoTarget, "set"+varname, new Class[]{srcResult.getClass()}, new Object[]{srcResult});
                        }
                    } catch (Exception e) {
                        log.error("method="+method.getName(),e);
                    }
                }
            }
            curcls = curcls.getSuperclass();
        }
    }
    private static void _clearProperties(Object pojoSource) {
        // putting Pagination Parameter into paramsInTag
        Class<?> curcls = pojoSource.getClass();
        // loop for all the superclass until instanceof Object
        while ( curcls != null && !curcls.getName().equals(Object.class.getName()) ) {
            // all the declared methods
            Method[] methods = curcls.getDeclaredMethods();
            for ( int i =0; methods!= null && i<methods.length; i++ ) {
                Method method = methods[i];
                // all the getter
                if ( method.getName().startsWith("get") || method.getName().startsWith("is") ) {
                    try {
                        Pattern pattern = Pattern.compile("[A-Z].*");
                        Matcher matcher = pattern.matcher(method.getName());
                        String varname = null;
                        if ( matcher.find() ) {
                            varname = matcher.group();
                        }
                        Object srcResult = runMethod(pojoSource, method.getName(), null, null);
                        if ( srcResult != null ) {
                            runMethod(pojoSource, "set"+varname, new Class[]{srcResult.getClass()}, new Object[]{null});
                        }
                    } catch (Exception e) {
                        log.error("method="+method.getName(),e);
                    }
                }
            }
            curcls = curcls.getSuperclass();
        }
    }

    protected static Object runMethod(Object object, String methodname, Class[] classarr, Object[] methodargs) {
        try {
            Method method = object.getClass().getMethod(methodname, classarr );
            return method.invoke(object, methodargs );
        } catch (Exception e) {
//            log.error("Cannot run method "+methodname+" in bean "+object.getClass().getName());
        }
        return null;
    }

    private static String _reverseOrderDirections(String orderDirections) {
        log.debug("orderDirections="+orderDirections);
        if ( StringUtils.isEmpty(orderDirections) ) return "asc";
        StringBuilder sb = new StringBuilder();
        orderDirections = StringUtils.replace(orderDirections, " ", "");
        Matcher matcher = Pattern.compile("(asc|desc)", Pattern.CASE_INSENSITIVE).matcher(orderDirections);
        while (matcher.find()) {
            String ma = matcher.group();
            if ( sb.length()>0 ) sb.append(",");
            if ( "asc".equalsIgnoreCase(ma) ) {
                sb.append("desc");
            } else {
                sb.append("asc");
            }
        }
        return sb.toString();
    }

    public void setPaginationService(PaginationService<P> paginationService) {
        this.paginationService = paginationService;
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
	public void setOptionDisplayCheckbox(boolean optionDisplayCheckbox) {
        this.optionDisplayCheckbox = optionDisplayCheckbox;
    }

    public void setOptionDisplaySerialNo(boolean optionDisplaySerialNo) {
        this.optionDisplaySerialNo = optionDisplaySerialNo;
    }

    public void setOptionWidth(int optionWidth) {
        this.optionWidth = optionWidth;
    }

    public void setDefaultRecordPerPage(int defaultRecordPerPage) {
        this.defaultRecordPerPage = defaultRecordPerPage;
    }

	public void setDefaultSortName(String defaultSortName) {
        this.defaultSortName = defaultSortName;
    }

    public void setDefaultSortAscDesc(String defaultSortAscDesc) {
        this.defaultSortAscDesc = defaultSortAscDesc;
    }
}
