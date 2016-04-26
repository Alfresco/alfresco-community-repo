package org.alfresco.repo.search.impl.noindex;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.error.StackTraceUtil;
import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.solr.SolrSearchService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Andy
 * log4j:logger=org.alfresco.repo.search.impl.noindex.NoIndexSearchService
 */
public class NoIndexSearchService extends SolrSearchService
{
    private static Log s_logger = LogFactory.getLog(NoIndexSearchService.class);

    
    

    private void trace()
    {
        if (s_logger.isTraceEnabled())
        {
            Exception e = new Exception();
            e.fillInStackTrace();

            StringBuilder sb = new StringBuilder(1024);
            StackTraceUtil.buildStackTrace("Search trace ...", e.getStackTrace(), sb, -1);
            s_logger.trace(sb);
        }
    }

    

   
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.search.SearchParameters)
     */
    @Override
    public ResultSet query(SearchParameters searchParameters)
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("query   searchParameters = " + searchParameters);
        }
        trace();
        try
        {
            return super.query(searchParameters);
        }
        catch(SearcherException e)
        {
            return new EmptyResultSet();
        }
        catch(QueryModelException e)
        {
            return new EmptyResultSet();
        }
        catch(AlfrescoRuntimeException e)
        {
            return new EmptyResultSet();
        }
    }

    
}
