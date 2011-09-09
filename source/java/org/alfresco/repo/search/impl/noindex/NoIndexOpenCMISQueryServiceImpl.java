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
package org.alfresco.repo.search.impl.noindex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.search.CMISQueryOptions;
import org.alfresco.opencmis.search.CMISQueryOptions.CMISQueryMode;
import org.alfresco.opencmis.search.CMISQueryParser;
import org.alfresco.opencmis.search.CMISQueryService;
import org.alfresco.opencmis.search.CMISResultSet;
import org.alfresco.opencmis.search.CmisFunctionEvaluationContext;
import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryModelFactory;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;

/**
 * @author Andy
 *
 */
public class NoIndexOpenCMISQueryServiceImpl implements CMISQueryService
{
    private NodeService nodeService;

    private DictionaryService alfrescoDictionaryService;

    private CMISDictionaryService cmisDictionaryService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setAlfrescoDictionaryService(DictionaryService alfrescoDictionaryService)
    {
        this.alfrescoDictionaryService = alfrescoDictionaryService;
    }

    public void setCmisDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.opencmis.search.CMISQueryService#query(org.alfresco.opencmis.search.CMISQueryOptions)
     */
    @Override
    public CMISResultSet query(CMISQueryOptions options)
    {
        ResultSet rs = new EmptyResultSet();
        
        CapabilityJoin joinSupport = getJoinSupport();
        if(options.getQueryMode() == CMISQueryOptions.CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS)
        {
            joinSupport = CapabilityJoin.INNERONLY;
        }
        
        // TODO: Refactor to avoid duplication of valid scopes here and in CMISQueryParser
        
        BaseTypeId[] validScopes = (options.getQueryMode() == CMISQueryMode.CMS_STRICT) ? CmisFunctionEvaluationContext.STRICT_SCOPES : CmisFunctionEvaluationContext.ALFRESCO_SCOPES;
        CmisFunctionEvaluationContext functionContext = new CmisFunctionEvaluationContext();
        functionContext.setCmisDictionaryService(cmisDictionaryService);
        functionContext.setNodeService(nodeService);
        functionContext.setValidScopes(validScopes);
        
        CMISQueryParser parser = new CMISQueryParser(options, cmisDictionaryService, joinSupport);
        Query query = parser.parse(new LuceneQueryModelFactory(), functionContext);

        Map<String, ResultSet> wrapped = new HashMap<String, ResultSet>();
        for (Set<String> group : query.getSource().getSelectorGroups(functionContext))
        {
            for (String selector : group)
            {
                wrapped.put(selector, rs);
            }
        }
        LimitBy limitBy = null;
        limitBy = rs.getResultSetMetaData().getLimitedBy();
        
        CMISResultSet cmis = new CMISResultSet(wrapped, options, limitBy, nodeService, query, cmisDictionaryService, alfrescoDictionaryService);
        return cmis;
    }

    /* (non-Javadoc)
     * @see org.alfresco.opencmis.search.CMISQueryService#query(java.lang.String, org.alfresco.service.cmr.repository.StoreRef)
     */
    @Override
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
