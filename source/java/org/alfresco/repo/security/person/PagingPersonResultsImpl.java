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
package org.alfresco.repo.security.person;

import java.util.List;

import org.alfresco.query.PermissionedResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PagingPersonResults;
import org.alfresco.util.Pair;

/**
 * Page of Person results
 * 
 * @author janv
 * @since 4.0
 */
/* package */ class PagingPersonResultsImpl implements PagingPersonResults, PermissionedResults
{
    private List<NodeRef> nodeRefs;
    
    private boolean hasMoreItems;
    private Pair<Integer, Integer> totalResultCount;
    private String queryExecutionId;
    private boolean permissionsApplied;
    
    public PagingPersonResultsImpl(List<NodeRef> nodeRefs, boolean hasMoreItems, Pair<Integer, Integer> totalResultCount, String queryExecutionId, boolean permissionsApplied)
    {
        this.nodeRefs = nodeRefs;
        this.hasMoreItems = hasMoreItems;
        this.totalResultCount = totalResultCount;
        this.queryExecutionId = queryExecutionId;
        this.permissionsApplied = permissionsApplied;
    }
    
    public List<NodeRef> getPage()
    {
        return nodeRefs;
    }
    
    public boolean hasMoreItems()
    {
        return hasMoreItems;
    }
    
    public Pair<Integer, Integer> getTotalResultCount()
    {
        return totalResultCount;
    }
    
    public String getQueryExecutionId()
    {
        return queryExecutionId;
    }
    
    public boolean permissionsApplied()
    {
        return permissionsApplied;
    }
}
