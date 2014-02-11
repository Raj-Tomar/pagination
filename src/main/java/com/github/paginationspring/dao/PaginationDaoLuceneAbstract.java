package com.github.paginationspring.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.FSDirectory;

import com.github.paginationspring.bo.BoPaginationParam;

public abstract class PaginationDaoLuceneAbstract<P extends BoPaginationParam> implements PaginationDao<Document, P> {
	private static Log log = LogFactory.getLog(PaginationDaoLuceneAbstract.class);
    private static final String DIR_ASC = "asc";
    private static final String DIR_DESC = "desc";

    protected abstract File loadLuceneIndexDirectory();
    
    protected abstract QueryParser loadQueryParser();
    
    protected abstract String composeQuery(P pparam);
    
	public int retrieveCountResult(P pparam) throws Exception {
        QueryParser parser = loadQueryParser();
        Query query = parser.parse(composeQuery(pparam));
        
        SortField[] sortfields = processQueryOrder(pparam);

		IndexReader reader = null;
        IndexSearcher searcher = null;
		int numTotalHits = 0;
		try {
	        reader = IndexReader.open(FSDirectory.open(loadLuceneIndexDirectory()));
	        searcher = new IndexSearcher(reader);
	
	        Sort sort = null;
	        if ( sortfields !=null && sortfields.length>0 ) {
	        	sort = new Sort(sortfields);
	        } else {
	        	sort = new Sort();
	        }
	        TopFieldDocs results = searcher.search(query, 5, sort);
	        numTotalHits = results.totalHits;

	        if ( query != null ) log.debug("query="+query.toString());
            if ( sortfields != null ) {
            	for ( SortField sortField : sortfields ) {
            		log.debug("sort field="+sortField.getField() + " isReverse"+sortField.getReverse());
            	}
            }
		} catch (Exception e) {
			log.error("",e);
		} finally {
			if ( searcher != null ) {
				searcher.close();
			}
			if ( reader != null ) {
				reader.close();
			}
		}
	    return numTotalHits;
    }

	public List<Document> retrievePageResult(P pparam) throws Exception {
		List<Document> documents = new ArrayList<Document>();
        QueryParser parser = loadQueryParser();
        Query query = parser.parse(composeQuery(pparam));
        
        SortField[] sortfields = processQueryOrder(pparam);

		IndexReader reader = null;
        IndexSearcher searcher = null;
		int numTotalHits = 0;
		try {
	        reader = IndexReader.open(FSDirectory.open(loadLuceneIndexDirectory()));
	        searcher = new IndexSearcher(reader);
	
	        Sort sort = null;
	        if ( sortfields !=null && sortfields.length>0 ) {
	        	sort = new Sort(sortfields);
	        } else {
	        	sort = new Sort();
	        }
	        TopFieldDocs results = searcher.search(query, 5, sort);
	        numTotalHits = results.totalHits;
	        
	        if ( results.totalHits > 0 ) {
	            ScoreDoc[] hits = searcher.search(query, numTotalHits, sort).scoreDocs;
	            int readpos = 0;
	            int NO_OF_RESULT_PER_QUERY = 25;
	            if ( !StringUtils.isEmpty(pparam.getResultIndex()) ) {
	            	readpos = Integer.parseInt(pparam.getResultIndex());
	            }
	            if ( !StringUtils.isEmpty(pparam.getRecordPerPage()) ) {
	            	NO_OF_RESULT_PER_QUERY=Integer.parseInt(pparam.getRecordPerPage());
	            }
                for ( int i = readpos; i < readpos+NO_OF_RESULT_PER_QUERY && i < numTotalHits; i++ ) {
                    Document tmpdoc = searcher.doc(hits[i].doc);
                    documents.add(tmpdoc);
                }
	        }
            if ( query != null ) log.debug("query="+query.toString());
            if ( sortfields != null ) {
            	for ( SortField sortField : sortfields ) {
            		log.debug("sort field="+sortField.getField() + " isReverse="+sortField.getReverse());
            	}
            }

		} catch (Exception e) {
			log.error("",e);
		} finally {
			if ( searcher != null ) {
				searcher.close();
			}
			if ( reader != null ) {
				reader.close();
			}
		}
	    return documents;
    }

    protected SortField[] processQueryOrder(P pparam) throws Exception {
        if ( StringUtils.isEmpty(pparam.getOrderColumns()) ) return null;
        
        StringTokenizer tokens = new StringTokenizer(pparam.getOrderColumns(), ",");
        String propertyPath;
        String propertyDir;
        String token;
        List<SortField> sortFields = new ArrayList<SortField>();
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

            SortField sortField = new SortField(propertyPath, SortField.STRING, DIR_ASC.equals(propertyDir)? false:true);
            sortFields.add(sortField);
            
            tokenpos++;
        }
        if ( sortFields.size()== 0 ) return null;
        SortField[] arr = new SortField[sortFields.size()];
        for ( int i = 0; i<sortFields.size(); i++ ) {
        	arr[i]=sortFields.get(i);
        }
        return arr;
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

}
