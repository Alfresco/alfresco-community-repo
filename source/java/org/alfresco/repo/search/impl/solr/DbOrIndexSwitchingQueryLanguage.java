/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.search.impl.solr;

import org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryLanguage;
import org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Andy
 *
 */
public class DbOrIndexSwitchingQueryLanguage extends AbstractLuceneQueryLanguage
{
    protected static final Log logger = LogFactory.getLog(DbOrIndexSwitchingQueryLanguage.class);
    
    LuceneQueryLanguageSPI dbQueryLanguage;
    
    LuceneQueryLanguageSPI indexQueryLanguage;
    
    QueryConsistency queryConsistency = QueryConsistency.DEFAULT;
    
    /**
     * @param dbQueryLanguage the dbQueryLanguage to set
     */
    public void setDbQueryLanguage(LuceneQueryLanguageSPI dbQueryLanguage)
    {
        this.dbQueryLanguage = dbQueryLanguage;
    }



    /**
     * @param solrQueryLanguage the solrQueryLanguage to set
     */
    public void setIndexQueryLanguage(LuceneQueryLanguageSPI indexQueryLanguage)
    {
        this.indexQueryLanguage = indexQueryLanguage;
    }



    /**
     * @param queryConsistency the queryConsistency to set
     */
    public void setQueryConsistency(QueryConsistency queryConsistency)
    {
        this.queryConsistency = queryConsistency;
    }



    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
        QueryConsistency consistency = searchParameters.getQueryConsistency();
        if(consistency == QueryConsistency.DEFAULT)
        {
            consistency = queryConsistency;
        }
 
        switch(consistency)
        {
        case EVENTUAL:
            if(indexQueryLanguage != null)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("Using SOLR query: "+dbQueryLanguage.getName()+" for "+searchParameters);
                }
                return indexQueryLanguage.executeQuery(searchParameters, admLuceneSearcher);
            }
            else
            {
                throw new QueryModelException("No query language available");
            }
        case TRANSACTIONAL:
            if(dbQueryLanguage != null)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("Trying db query for "+dbQueryLanguage.getName()+" for "+searchParameters);
                }
                return dbQueryLanguage.executeQuery(searchParameters, admLuceneSearcher);
            }
            else
            {
                throw new QueryModelException("No query language available");
            }
        case DEFAULT:
        case TRANSACTIONAL_IF_POSSIBLE:
        default:
            if(dbQueryLanguage != null)
            {
                try
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Trying db query for "+dbQueryLanguage.getName()+" for "+searchParameters);
                    }
                    return dbQueryLanguage.executeQuery(searchParameters, admLuceneSearcher);
                }
                catch(QueryModelException qme)
                {
                    // MNT-10323: Logging configuration on JBoss leads to clogging of the log with a lot of these errors because of INFO level when WQS module is installed
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("DB query failed for " + dbQueryLanguage.getName() + " for " + searchParameters, qme);
                    }

                    if(indexQueryLanguage != null)
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Using SOLR query: "+dbQueryLanguage.getName()+" for "+searchParameters);
                        }
                        return indexQueryLanguage.executeQuery(searchParameters, admLuceneSearcher);
                    }
                }
            }
            else
            {
                if(indexQueryLanguage != null)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("(No DB QL) Using SOLR query: "+dbQueryLanguage.getName()+" for "+searchParameters);
                    }
                    return indexQueryLanguage.executeQuery(searchParameters, admLuceneSearcher);
                }
            }
            throw new QueryModelException("No query language available");
        }
        
        
    }
}
