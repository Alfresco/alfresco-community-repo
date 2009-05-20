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
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;

public class ChildAssocRefResultSet extends AbstractResultSet
{
    private List<ChildAssociationRef> cars;
    NodeService nodeService;
    
    public ChildAssocRefResultSet(NodeService nodeService, List<ChildAssociationRef> cars, Path[] propertyPaths)
    {
        super(propertyPaths);
        this.nodeService = nodeService;
        this.cars = cars;
    }
    
    public ChildAssocRefResultSet(NodeService nodeService, List<NodeRef> nodeRefs, Path[] propertyPaths, boolean resolveAllParents)
    {
        super(propertyPaths);
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
    
    public NodeService getNodeService()
    {
        return nodeService;
    }

    public ResultSetMetaData getResultSetMetaData()
    {
        return new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, new SearchParameters());
    }
}
