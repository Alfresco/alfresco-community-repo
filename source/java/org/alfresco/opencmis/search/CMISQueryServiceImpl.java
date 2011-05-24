/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.opencmis.search;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.search.CMISQueryOptions.CMISQueryMode;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.QueryEngine;
import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;

/**
 * @author andyh
 */
public class CMISQueryServiceImpl implements CMISQueryService
{
    private CMISDictionaryService cmisDictionaryService;

    private QueryEngine queryEngine;

    private NodeService nodeService;

    private DictionaryService alfrescoDictionaryService;

    public void setOpenCMISDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }

    /**
     * @param queryEngine
     *            the queryEngine to set
     */
    public void setQueryEngine(QueryEngine queryEngine)
    {
        this.queryEngine = queryEngine;
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
        Query query = parser.parse(queryEngine.getQueryModelFactory(), functionContext);

        QueryEngineResults results = queryEngine.executeQuery(query, options, functionContext);
        Map<String, ResultSet> wrapped = new HashMap<String, ResultSet>();
        Map<Set<String>, ResultSet> map = results.getResults();
        for (Set<String> group : map.keySet())
        {
            ResultSet current = map.get(group);
            for (String selector : group)
            {
                wrapped.put(selector, current);
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
