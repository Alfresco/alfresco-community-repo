/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
import java.util.SortedSet;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.PagingDetails;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the deletednodes.get web script.
 * 
 * @author Neil McErlean
 * @since 3.5
 */
public class ArchivedNodesGet extends AbstractArchivedNodeWebScript
{
    private static final String MAX_ITEMS          = "maxItems";
    private static final String SKIP_COUNT         = "skipCount";
    
    List<ArchivedNodesFilter> nodeFilters = new ArrayList<ArchivedNodesFilter>();

    /**
     * This method is used to inject {@link ArchivedNodeFilter node filters} on this GET call.
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

        // We want to get all nodes in the archive which were originally contained in the
        // following StoreRef.
        StoreRef storeRef = parseRequestForStoreRef(req);
        
        
        SortedSet<ChildAssociationRef> orderedChildren = getArchivedNodesFrom(storeRef);
        
        List<ArchivedNodeState> deletedNodes = new ArrayList<ArchivedNodeState>(orderedChildren.size());

        for (ChildAssociationRef chAssRef : orderedChildren)
        {
            NodeRef nextArchivedNode = chAssRef.getChildRef();
            boolean nodeIsFilteredOut = false;
            for (ArchivedNodesFilter filter : nodeFilters)
            {
                if (filter.accept(nextArchivedNode) == false)
                {
                    nodeIsFilteredOut = true;
                    break; // Break from the loop over filters.
                }
            }
            if (!nodeIsFilteredOut)
            {
                ArchivedNodeState state = ArchivedNodeState.create(nextArchivedNode, serviceRegistry);
                deletedNodes.add(state);
            }
        }
        
        // Grab the paging parameters
        PagingDetails paging = new PagingDetails(
                    getIntParameter(req, MAX_ITEMS, deletedNodes.size()),
                    getIntParameter(req, SKIP_COUNT, 0)
        );
        
        // Now do the paging
        model.put(DELETED_NODES, ModelUtil.page(deletedNodes, paging)); 
        model.put("paging", ModelUtil.buildPaging(paging));

        return model;
    }
}

