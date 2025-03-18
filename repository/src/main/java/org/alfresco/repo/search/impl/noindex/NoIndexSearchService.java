/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.search.impl.noindex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.error.StackTraceUtil;
import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.solr.SolrSearchService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * @author Andy log4j:logger=org.alfresco.repo.search.impl.noindex.NoIndexSearchService
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

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.search.SearchParameters) */
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
        catch (SearcherException e)
        {
            return new EmptyResultSet();
        }
        catch (QueryModelException e)
        {
            return new EmptyResultSet();
        }
        catch (AlfrescoRuntimeException e)
        {
            return new EmptyResultSet();
        }
    }

}
