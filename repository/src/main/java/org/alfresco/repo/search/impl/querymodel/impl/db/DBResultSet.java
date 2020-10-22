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
package org.alfresco.repo.search.impl.querymodel.impl.db;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.search.AbstractResultSet;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * @author Andy
 *
 */
public class DBResultSet extends AbstractResultSet
{
    private List<Node> nodes;
    
    private NodeDAO nodeDao;
    
    private NodeService nodeService;
    
    private TenantService tenantService;
    
    private SimpleResultSetMetaData resultSetMetaData;
    
    private BitSet prefetch;
    
    private int numberFound;
    
    public DBResultSet(SearchParameters searchParameters, List<Node> nodes, NodeDAO nodeDao,  NodeService nodeService, TenantService tenantService, int maximumResultsFromUnlimitedQuery)
    {
        this.nodeDao = nodeDao;
        this.nodes = nodes;
        this.nodeService = nodeService;
        this.tenantService = tenantService;
        this.prefetch = new BitSet(nodes.size());
        this.numberFound = nodes.size();
        
        final LimitBy limitBy;
        int maxResults = -1;
        if (searchParameters.getMaxItems() >= 0)
        {
            maxResults = searchParameters.getMaxItems();
            limitBy = LimitBy.FINAL_SIZE;
        }
        else if(searchParameters.getLimitBy() == LimitBy.FINAL_SIZE && searchParameters.getLimit() >= 0)
        {
            maxResults = searchParameters.getLimit();
            limitBy = LimitBy.FINAL_SIZE;
        }
        else
        {
            maxResults = searchParameters.getMaxPermissionChecks();
            if (maxResults < 0)
            {
                maxResults = maximumResultsFromUnlimitedQuery;
            }
            limitBy = LimitBy.NUMBER_OF_PERMISSION_EVALUATIONS;
        }
        
        this.resultSetMetaData = new SimpleResultSetMetaData(
                maxResults > 0 && nodes.size() < maxResults ? LimitBy.UNLIMITED : limitBy,
                PermissionEvaluationMode.EAGER, searchParameters);
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#length()
     */
    @Override
    public int length()
    {
        return nodes.size();
    }
    
    public void setNumberFound(int numFound)
    {
        this.numberFound = numFound;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNumberFound()
     */
    @Override
    public long getNumberFound()
    {
        return numberFound;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNodeRef(int)
     */
    @Override
    public NodeRef getNodeRef(int n)
    {
        NodeRef nodeRef = nodes.get(n).getNodeRef();
        nodeRef = tenantService.getBaseName(nodeRef);
        return nodeRef;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getRow(int)
     */
    @Override
    public ResultSetRow getRow(int i)
    {
        return new DBResultSetRow(this, i);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getChildAssocRef(int)
     */
    @Override
    public ChildAssociationRef getChildAssocRef(int n)
    {
        ChildAssociationRef primaryParentAssoc = nodeService.getPrimaryParent(getNodeRef(n));
        if(primaryParentAssoc != null)
        {
            return primaryParentAssoc;
        }
        else
        {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getResultSetMetaData()
     */
    @Override
    public ResultSetMetaData getResultSetMetaData()
    {
        return resultSetMetaData;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getStart()
     */
    @Override
    public int getStart()
    {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#hasMore()
     */
    @Override
    public boolean hasMore()
    {
        return false; 
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<ResultSetRow> iterator()
    {
        return new DBResultSetRowIterator(this);
    }
    
    public NodeService getNodeService()
    {
        return nodeService;
    }
    
    public Node getNode(int n)
    {
        return nodes.get(n);
    }
}
