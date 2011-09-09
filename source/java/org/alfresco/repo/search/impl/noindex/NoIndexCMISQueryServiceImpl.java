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

import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISJoinEnum;
import org.alfresco.cmis.CMISQueryEnum;
import org.alfresco.cmis.CMISQueryOptions;
import org.alfresco.cmis.CMISQueryService;
import org.alfresco.cmis.CMISResultSet;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISQueryOptions.CMISQueryMode;
import org.alfresco.cmis.search.CMISQueryParser;
import org.alfresco.cmis.search.CMISResultSetImpl;
import org.alfresco.cmis.search.CmisFunctionEvaluationContext;
import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryModelFactory;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * @author Andy
 *
 */
public class NoIndexCMISQueryServiceImpl implements CMISQueryService
{
    private CMISServices cmisService;

    private CMISDictionaryService cmisDictionaryService;

    private NodeService nodeService;

    private DictionaryService alfrescoDictionaryService;
    
    public void setCmisService(CMISServices cmisService)
    {
        this.cmisService = cmisService;
    }

    public void setCmisDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setAlfrescoDictionaryService(DictionaryService alfrescoDictionaryService)
    {
        this.alfrescoDictionaryService = alfrescoDictionaryService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISQueryService#query(org.alfresco.cmis.CMISQueryOptions)
     */
    @Override
    public CMISResultSet query(CMISQueryOptions options)
    {
        ResultSet rs = new EmptyResultSet();
        
        CMISJoinEnum joinSupport = getJoinSupport();
        if(options.getQueryMode() == CMISQueryOptions.CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS)
        {
            joinSupport = CMISJoinEnum.INNER_JOIN_SUPPORT;
        }
        
        // TODO: Refactor to avoid duplication of valid scopes here and in CMISQueryParser
        
        CMISScope[] validScopes = (options.getQueryMode() == CMISQueryMode.CMS_STRICT) ? CmisFunctionEvaluationContext.STRICT_SCOPES : CmisFunctionEvaluationContext.ALFRESCO_SCOPES;
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
        
        CMISResultSet cmis = new CMISResultSetImpl(wrapped, options, limitBy, nodeService, query, cmisDictionaryService, alfrescoDictionaryService);
        return cmis;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISQueryService#query(java.lang.String)
     */
    @Override
    public CMISResultSet query(String query)
    {
        CMISQueryOptions options = new CMISQueryOptions(query, cmisService.getDefaultRootStoreRef());
        return query(options);
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISQueryService#getQuerySupport()
     */
    @Override
    public CMISQueryEnum getQuerySupport()
    {
        return CMISQueryEnum.BOTH_COMBINED;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISQueryService#getJoinSupport()
     */
    @Override
    public CMISJoinEnum getJoinSupport()
    {
        return CMISJoinEnum.NO_JOIN_SUPPORT;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISQueryService#getPwcSearchable()
     */
    @Override
    public boolean getPwcSearchable()
    {
      return true;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISQueryService#getAllVersionsSearchable()
     */
    @Override
    public boolean getAllVersionsSearchable()
    {
        return false;
    }

  
}
