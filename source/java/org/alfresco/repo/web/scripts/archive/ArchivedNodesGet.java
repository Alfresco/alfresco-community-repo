/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

package org.alfresco.repo.web.scripts.archive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.ScriptPagingDetails;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the archivednodes.get web script.
 * 
 * @author Neil McErlean, Jamal Kaabi-Mofrad
 * @since 3.5
 */
public class ArchivedNodesGet extends AbstractArchivedNodeWebScript
{
    private static final String MAX_ITEMS = "maxItems";
    private static final String SKIP_COUNT = "skipCount";
    private static final String NAME_FILTER = "nf";

    List<ArchivedNodesFilter> nodeFilters = new ArrayList<ArchivedNodesFilter>();

    /**
     * This method is used to inject {@link ArchivedNodeFilter node filters} on this GET call.
     * 
     * @param nodeFilters
     */
    public void setArchivedNodeFilters(List<ArchivedNodesFilter> nodeFilters)
    {
        this.nodeFilters = nodeFilters;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        // We want to get all nodes in the archive which were originally
        // contained in the following StoreRef.
        StoreRef storeRef = parseRequestForStoreRef(req);
        
        // Create paging
        ScriptPagingDetails paging = new ScriptPagingDetails(getIntParameter(req, MAX_ITEMS, DEFAULT_MAX_ITEMS_PER_PAGE),
                    getIntParameter(req, SKIP_COUNT, 0));
        
        PagingResults<NodeRef> result = getArchivedNodesFrom(storeRef, paging, req.getParameter(NAME_FILTER));
        List<NodeRef> nodeRefs = result.getPage();

        List<ArchivedNodeState> deletedNodes = new ArrayList<ArchivedNodeState>(nodeRefs.size());
        for (NodeRef archivedNode : nodeRefs)
        {
            ArchivedNodeState state = ArchivedNodeState.create(archivedNode, serviceRegistry);
            deletedNodes.add(state);
        }

        // Now do the paging
        // ALF-19111. Note: Archived nodes CQ, supports Paging, 
        // so no need to use the ModelUtil.page method to build the page again.        
        model.put(DELETED_NODES, deletedNodes);
        // Because we haven't used ModelUtil.page method, we need to set the total items manually.
        paging.setTotalItems(deletedNodes.size());
        model.put("paging", ModelUtil.buildPaging(paging));
        
        return model;
    }
}
