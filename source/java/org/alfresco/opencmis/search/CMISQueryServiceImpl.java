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
package org.alfresco.opencmis.search;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.search.CMISQueryOptions.CMISQueryMode;
import org.alfresco.repo.search.impl.lucene.PagingLuceneResultSet;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.QueryEngine;
import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.security.permissions.impl.acegi.FilteringResultSet;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.util.Pair;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;

/**
 * @author andyh
 */
public class CMISQueryServiceImpl implements CMISQueryService
{
    private CMISDictionaryService cmisDictionaryService;

    private QueryEngine luceneQueryEngine;
    private QueryEngine dbQueryEngine;

    private NodeService nodeService;

    private DictionaryService alfrescoDictionaryService;

    public void setOpenCMISDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }

    /**
     * @param queryEngine
     *            the luceneQueryEngine to set
     */
    public void setLuceneQueryEngine(QueryEngine queryEngine)
    {
        this.luceneQueryEngine = queryEngine;
    }

    /**
     * @param queryEngine
     *            the dbQueryEngine to set
     */
    public void setDbQueryEngine(QueryEngine queryEngine)
    {
        this.dbQueryEngine = queryEngine;
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param alfrescoDictionaryService
     *            the Alfresco Dictionary Service to set
     */
    public void setAlfrescoDictionaryService(DictionaryService alfrescoDictionaryService)
    {
        this.alfrescoDictionaryService = alfrescoDictionaryService;
    }

    public CMISResultSet query(CMISQueryOptions options)
    {
        Pair<Query, QueryEngineResults> resultPair = executeQuerySwitchingImpl(options);
        
        Query query = resultPair.getFirst();
        QueryEngineResults results = resultPair.getSecond();
        
        Map<String, ResultSet> wrapped = new HashMap<String, ResultSet>();
        Map<Set<String>, ResultSet> map = results.getResults();
        for (Set<String> group : map.keySet())
        {
            ResultSet current = map.get(group);
            for (String selector : group)
            {
                wrapped.put(selector, filterNotExistingNodes(current));
            }
        }
        LimitBy limitBy = null;
        if ((null != results.getResults()) && !results.getResults().isEmpty()
                && (null != results.getResults().values()) && !results.getResults().values().isEmpty())
        {
            limitBy = results.getResults().values().iterator().next().getResultSetMetaData().getLimitedBy();
        }
        CMISResultSet cmis = new CMISResultSet(wrapped, options, limitBy, nodeService, query, cmisDictionaryService,
                alfrescoDictionaryService);
        return cmis;
    }

    private Pair<Query, QueryEngineResults> executeQuerySwitchingImpl(CMISQueryOptions options)
    {
        switch (options.getQueryConsistency())
        {
            case TRANSACTIONAL_IF_POSSIBLE :
            {
                try
                {
                    return executeQueryUsingEngine(dbQueryEngine, options);
                }
                catch(QueryModelException qme)
                {
                    return executeQueryUsingEngine(luceneQueryEngine, options);
                }
            }
            case TRANSACTIONAL :
            {
                return executeQueryUsingEngine(dbQueryEngine, options);
            }
            case EVENTUAL :
            case DEFAULT :
            default :
            {
                return executeQueryUsingEngine(luceneQueryEngine, options);
            }
        }
    }
    
    private Pair<Query, QueryEngineResults> executeQueryUsingEngine(QueryEngine queryEngine, CMISQueryOptions options)
    {
        CapabilityJoin joinSupport = getJoinSupport();
        if (options.getQueryMode() == CMISQueryOptions.CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS)
        {
            joinSupport = CapabilityJoin.INNERONLY;
        }

        // TODO: Refactor to avoid duplication of valid scopes here and in
        // CMISQueryParser

        BaseTypeId[] validScopes = (options.getQueryMode() == CMISQueryMode.CMS_STRICT) ? CmisFunctionEvaluationContext.STRICT_SCOPES
                : CmisFunctionEvaluationContext.ALFRESCO_SCOPES;
        CmisFunctionEvaluationContext functionContext = new CmisFunctionEvaluationContext();
        functionContext.setCmisDictionaryService(cmisDictionaryService);
        functionContext.setNodeService(nodeService);
        functionContext.setValidScopes(validScopes);

        CMISQueryParser parser = new CMISQueryParser(options, cmisDictionaryService, joinSupport);
        QueryConsistency queryConsistency = options.getQueryConsistency();
        if (queryConsistency == QueryConsistency.DEFAULT)
        {
        	options.setQueryConsistency(QueryConsistency.EVENTUAL);
        }
        
        Query query = parser.parse(queryEngine.getQueryModelFactory(), functionContext);
        QueryEngineResults queryEngineResults = queryEngine.executeQuery(query, options, functionContext);
        
        return new Pair<Query, QueryEngineResults>(query, queryEngineResults);
    }

    /* MNT-8804 filter ResultSet for nodes with corrupted indexes */
    private ResultSet filterNotExistingNodes(ResultSet resultSet)
    {
        if (resultSet instanceof PagingLuceneResultSet)
        {
            ResultSet wrapped = ((PagingLuceneResultSet)resultSet).getWrapped();
            
            if (wrapped instanceof FilteringResultSet)
            {
                FilteringResultSet filteringResultSet = (FilteringResultSet)wrapped;
                
                for (int i = 0; i < filteringResultSet.length(); i++)
                {
                    NodeRef nodeRef = filteringResultSet.getNodeRef(i);
                    /* filter node if it does not exist */
                    if (!nodeService.exists(nodeRef))
                    {
                        filteringResultSet.setIncluded(i, false);
                    }
                }
            }
        }
        
        return resultSet;
    }

    public CMISResultSet query(String query, StoreRef storeRef)
    {
        CMISQueryOptions options = new CMISQueryOptions(query, storeRef);
        return query(options);
    }

    public boolean getPwcSearchable()
    {
        return true;
    }

    public boolean getAllVersionsSearchable()
    {
        return false;
    }

    public CapabilityQuery getQuerySupport()
    {
        return CapabilityQuery.BOTHCOMBINED;
    }

    public CapabilityJoin getJoinSupport()
    {
        return CapabilityJoin.NONE;
    }
}
