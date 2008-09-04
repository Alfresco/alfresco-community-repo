/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.search.impl;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.cmis.CMISService;
import org.alfresco.cmis.dictionary.CMISDictionaryService;
import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.cmis.property.CMISPropertyService;
import org.alfresco.cmis.search.CMISQueryOptions;
import org.alfresco.cmis.search.CMISQueryService;
import org.alfresco.cmis.search.CMISResultSet;
import org.alfresco.cmis.search.CMISResultSetImpl;
import org.alfresco.cmis.search.CmisFunctionEvaluationContext;
import org.alfresco.cmis.search.FullTextSearchSupport;
import org.alfresco.cmis.search.JoinSupport;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.QueryEngine;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;

/**
 * @author andyh
 */
public class CMISQueryServiceImpl implements CMISQueryService
{
    private CMISService cmisService;

    private CMISDictionaryService cmisDictionaryService;

    private CMISPropertyService cmisPropertyService;

    private CMISMapping cmisMapping;

    private QueryEngine queryEngine;

    private NodeService nodeService;

    /**
     * @param service
     *            the service to set
     */
    public void setCmisService(CMISService cmisService)
    {
        this.cmisService = cmisService;
    }

    /**
     * @param cmisDictionaryService
     *            the cmisDictionaryService to set
     */
    public void setCmisDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }

    /**
     * @param cmisMapping
     *            the cmisMapping to set
     */
    public void setCmisMapping(CMISMapping cmisMapping)
    {
        this.cmisMapping = cmisMapping;
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
     * @param cmisPropertyService
     *            the cmisPropertyService to set
     */
    public void setCmisPropertyService(CMISPropertyService cmisPropertyService)
    {
        this.cmisPropertyService = cmisPropertyService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISQueryService#query(org.alfresco.cmis.search.CMISQueryOptions)
     */
    public CMISResultSet query(CMISQueryOptions options)
    {
        CMISQueryParser parser = new CMISQueryParser(options, cmisDictionaryService, cmisMapping, getJoinSupport());
        Query query = parser.parse(queryEngine.getQueryModelFactory());
        System.out.println(query);
        
        CmisFunctionEvaluationContext functionContext = new CmisFunctionEvaluationContext();
        functionContext.setCmisDictionaryService(cmisDictionaryService);
        functionContext.setCmisPropertyService(cmisPropertyService);
        functionContext.setNodeService(nodeService);

        ResultSet lucene = queryEngine.executeQuery(query, query.getSource().getSelector(), options, functionContext);
        Map<String, ResultSet> wrapped = new HashMap<String, ResultSet>();
        wrapped.put(query.getSource().getSelector(), lucene);
        CMISResultSet cmis = new CMISResultSetImpl(wrapped, options, nodeService, query, cmisDictionaryService, cmisPropertyService);
        return cmis;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISQueryService#query(java.lang.String)
     */
    public CMISResultSet query(String query)
    {
        CMISQueryOptions options = new CMISQueryOptions(query, cmisService.getDefaultRootStoreRef());
        return query(options);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISQueryService#getAllVersionsSearchable()
     */
    public boolean getAllVersionsSearchable()
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISQueryService#getFullTextSearchSupport()
     */
    public FullTextSearchSupport getFullTextSearchSupport()
    {
        return FullTextSearchSupport.FULL_TEXT_AND_STRUCTURED;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISQueryService#getJoinSupport()
     */
    public JoinSupport getJoinSupport()
    {
        return JoinSupport.NO_JOIN_SUPPORT;
    }

}
