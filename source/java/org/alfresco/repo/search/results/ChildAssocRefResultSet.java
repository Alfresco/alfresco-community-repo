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
/*
 * Created on 07-Jun-2005
 *
 * TODO Comment this class
 * 
 * 
 */
package org.alfresco.repo.search.results;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.search.AbstractResultSet;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Child assoc result set
 * @author andyh
 *
 */
public class ChildAssocRefResultSet extends AbstractResultSet
{
    private List<ChildAssociationRef> cars;
    NodeService nodeService;
    
    /**
     * Normal constructor
     * @param nodeService
     * @param cars
     */
    public ChildAssocRefResultSet(NodeService nodeService, List<ChildAssociationRef> cars)
    {
        super();
        this.nodeService = nodeService;
        this.cars = cars;
    }
    
    /**
     * Constructor that may expand all child assoc parents provided
     * @param nodeService
     * @param nodeRefs
     * @param resolveAllParents
     */
    public ChildAssocRefResultSet(NodeService nodeService, List<NodeRef> nodeRefs, boolean resolveAllParents)
    {
        super();
        this.nodeService = nodeService;
        List<ChildAssociationRef> cars = new ArrayList<ChildAssociationRef>(nodeRefs.size());
        for(NodeRef nodeRef : nodeRefs)
        {
            if(resolveAllParents)
            {
                cars.addAll(nodeService.getParentAssocs(nodeRef));
            }
            else
            {
                cars.add(nodeService.getPrimaryParent(nodeRef));
            }
        }
        this.cars = cars;
    }

    public int length()
    {
        return cars.size();
    }

    public NodeRef getNodeRef(int n)
    {
        return cars.get(n).getChildRef();
    }
    
    public ChildAssociationRef getChildAssocRef(int n)
    {
        return cars.get(n);
    }

    public ResultSetRow getRow(int i)
    {
        return new ChildAssocRefResultSetRow(this, i);
    }

    public Iterator<ResultSetRow> iterator()
    {
        return new ChildAssocRefResultSetRowIterator(this);
    }
    
    
    /* package */ NodeService getNodeService()
    {
        return nodeService;
    }

    public ResultSetMetaData getResultSetMetaData()
    {
        return new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, new SearchParameters());
    }

    public int getStart()
    {
       throw new UnsupportedOperationException();
    }

    public boolean hasMore()
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNumberFound()
     */
    @Override
    public long getNumberFound()
    {
        return cars.size();
    }
}
