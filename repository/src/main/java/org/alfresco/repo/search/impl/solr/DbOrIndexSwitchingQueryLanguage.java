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
package org.alfresco.repo.search.impl.solr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.solr.SearchDAO;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryLanguage;
import org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.results.ChildAssocRefResultSet;
import org.alfresco.repo.solr.NodeParameters;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

/**
 * @author Andy
 * @author Matt Ward
 */
public class DbOrIndexSwitchingQueryLanguage extends AbstractLuceneQueryLanguage
{
    protected static final Log logger = LogFactory.getLog(DbOrIndexSwitchingQueryLanguage.class);
    
    LuceneQueryLanguageSPI dbQueryLanguage;
    
    LuceneQueryLanguageSPI indexQueryLanguage;
    
    QueryConsistency queryConsistency = QueryConsistency.DEFAULT;
    
    private NodeService nodeService;
    
    private SearchDAO searchDao;
    
    private boolean hybridEnabled;
    
    /**
     * @param dbQueryLanguage the dbQueryLanguage to set
     */
    public void setDbQueryLanguage(LuceneQueryLanguageSPI dbQueryLanguage)
    {
        this.dbQueryLanguage = dbQueryLanguage;
    }

    /**
     * @param indexQueryLanguage the indexQueryLanguage to set
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

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchDao(SearchDAO searchDao)
    {
        this.searchDao = searchDao;
    }

    public void setHybridEnabled(boolean hybridEnabled)
    {
        this.hybridEnabled = hybridEnabled;
    }

    public ResultSet executeQuery(SearchParameters searchParameters)
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
                StopWatch stopWatch = new StopWatch("index only");
                stopWatch.start();
                ResultSet results = indexQueryLanguage.executeQuery(searchParameters);
                stopWatch.stop();
                if (logger.isDebugEnabled())
                {
                    logger.debug("SOLR returned " + results.length() + " results in " +
                                 stopWatch.getLastTaskTimeMillis() + "ms");
                }
                return results;
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
                StopWatch stopWatch = new StopWatch("database only");
                stopWatch.start();
                ResultSet results = dbQueryLanguage.executeQuery(flattenDBQuery(searchParameters));
                stopWatch.stop();
                if (logger.isDebugEnabled())
                {
                    logger.debug("DB returned " + results.length() + " results in " +
                                 stopWatch.getLastTaskTimeMillis() + "ms");
                }
                return results;
            }
            else
            {
                throw new QueryModelException("No query language available");
            }
        case HYBRID:
            if (!hybridEnabled)
            {
                throw new DisabledFeatureException("Hybrid query is disabled.");
            }
            return executeHybridQuery(searchParameters);
        case DEFAULT:
        case TRANSACTIONAL_IF_POSSIBLE:
        default:
            StopWatch stopWatch = new StopWatch("DB if possible");
            //SEARCH-347, exclude TMDQ calls if faceting present.
            if(dbQueryLanguage != null && !searchParameters.hasFaceting())
            {
                try
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Trying db query for "+dbQueryLanguage.getName()+" for "+searchParameters);
                    }
                    stopWatch.start();
                    ResultSet results = dbQueryLanguage.executeQuery(flattenDBQuery(searchParameters));
                    stopWatch.stop();
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("DB returned " + results.length() + " results in " +
                                     stopWatch.getLastTaskTimeMillis() + "ms");
                    }
                    return results;
                }
                catch(QueryModelException qme)
                {
                    if(stopWatch.isRunning())
                    {
                        stopWatch.stop();
                    }
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
                        stopWatch.start();

                        ResultSet results = indexQueryLanguage.executeQuery(searchParameters);
                        
                        stopWatch.stop();
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("SOLR returned " + results.length() + " results in " +
                                         stopWatch.getLastTaskTimeMillis() + "ms");
                        }
                        return results;
                    }
                }
            }
            else
            {
                if(indexQueryLanguage != null)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("(No DB QL) Using SOLR query: "+"dbQueryLanguage==null"+" for "+searchParameters);
                    }
                    stopWatch.start();
                    ResultSet results = indexQueryLanguage.executeQuery(searchParameters);
                    stopWatch.stop();
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("SOLR returned " + results.length() + " results in " +
                                     stopWatch.getLastTaskTimeMillis() + "ms");
                    }
                    return results;
                }
            }
            throw new QueryModelException("No query language available");
        }
    }

    private SearchParameters flattenDBQuery(SearchParameters sp)
    {
        if (sp.getFilterQueries().size() == 0)
        {
            return sp;
        }
        else
        {
            SearchParameters flatten = sp.copy();
            StringBuilder queryBuilder = new StringBuilder();
            
            queryBuilder.append("( ").append(sp.getQuery()).append(" )");
            for(String filter : sp.getFilterQueries())
            {
                Matcher matcher = LuceneQueryLanguageSPI.AFTS_QUERY.matcher(filter);
                if (matcher.find())
                {
                    queryBuilder.append("AND ( ").append(matcher.group(2)).append(" )");
                }
                else
                {
                    queryBuilder.append("AND ( ").append(filter).append(" )");
                }
            }
            flatten.setQuery(queryBuilder.toString());
            // the filter can be left and will be ignored by the DB query
            return flatten;
        }
    }

    private ResultSet executeHybridQuery(SearchParameters searchParameters)
    {        
        if (indexQueryLanguage == null || dbQueryLanguage == null)
        {
            throw new QueryModelException("Both index and DB query language required for hybrid search [index=" +
                                          indexQueryLanguage + ", DB=" + dbQueryLanguage + "]");
        }
        
        StopWatch stopWatch = new StopWatch("hybrid search");
        if (logger.isDebugEnabled())
        {
            logger.debug("Hybrid search, using SOLR query: "+dbQueryLanguage.getName()+" for "+searchParameters);
        }
        stopWatch.start("index query");
        ResultSet indexResults = indexQueryLanguage.executeQuery(searchParameters);
        stopWatch.stop();
        if (logger.isDebugEnabled())
        {
            logger.debug("SOLR query returned " + indexResults.length() + " results in " +
                         stopWatch.getLastTaskTimeMillis() + "ms");
        }
        // TODO: if the results are up-to-date, then nothing more to do - return the results.
        
        if (!(indexResults instanceof SolrJSONResultSet))
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("Hybrid search can only use database when SOLR is also in use. " +
                            "Skipping DB search, returning results from index.");
            }
            return indexResults;            
        }
        
        long lastTxId = ((SolrJSONResultSet) indexResults).getLastIndexedTxId();
        searchParameters.setSinceTxId(lastTxId);
        if(logger.isDebugEnabled())
        {
            logger.debug("Hybrid search, using DB query: "+dbQueryLanguage.getName()+" for "+searchParameters);
        }
        stopWatch.start("database query");
        ResultSet dbResults = dbQueryLanguage.executeQuery(searchParameters);
        stopWatch.stop();
        if (logger.isDebugEnabled())
        {
            logger.debug("DB query returned " + dbResults.length() + " results in " +
                         stopWatch.getLastTaskTimeMillis() + "ms");
        }
        // Merge result sets
        List<ChildAssociationRef> childAssocs = new ArrayList<>();
        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setFromTxnId(lastTxId+1);
        // TODO: setToTxnId(null) when SolrDAO behaviour is fixed.
        nodeParameters.setToTxnId(Long.MAX_VALUE);
        stopWatch.start("get changed nodes");
        List<Node> changedNodeList = searchDao.getNodes(nodeParameters, null, null);
        stopWatch.stop();
        if (logger.isDebugEnabled())
        {
            logger.debug("Nodes changed since last indexed transaction (ID " + lastTxId + ") = " +
                         changedNodeList.size() + " (took " + stopWatch.getLastTaskTimeMillis() + "ms)");
        }
        stopWatch.start("merge result sets");
        Set<NodeRef> nodeRefs = new HashSet<>(changedNodeList.size());
        for (Node n : changedNodeList)
        {
            nodeRefs.add(n.getNodeRef());
        }
        // Only use the SOLR results for nodes that haven't changed since indexing.
        for (ChildAssociationRef car : indexResults.getChildAssocRefs())
        {
            if (!nodeRefs.contains(car.getChildRef()))
            {
                childAssocs.add(car);
            }
        }
        // Merge in all the database results.
        childAssocs.addAll(dbResults.getChildAssocRefs());
        
        ResultSet results = new ChildAssocRefResultSet(nodeService, childAssocs);
        stopWatch.stop(); // merge result sets
        if (logger.isDebugEnabled())
        {
            String stats = String.format("SOLR=%d, DB=%d, total=%d",
                        indexResults.length(), dbResults.length(), results.length());
            logger.debug("Hybrid search returning combined results with counts: " + stats);
            logger.debug(stopWatch.prettyPrint());
        }
        return results;
    }
}
