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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

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
    private List<Long> dbids;
    
    private NodeRef[] nodeRefs;
    
    private NodeDAO nodeDao;
    
    private NodeService nodeService;
    
    private TenantService tenantService;
    
    private SimpleResultSetMetaData resultSetMetaData;
    
    private BitSet prefetch;
    
    public DBResultSet(SearchParameters searchParameters, List<Long> dbids, NodeDAO nodeDao,  NodeService nodeService, TenantService tenantService, int maximumResultsFromUnlimitedQuery)
    {
        this.nodeDao = nodeDao;
        this.dbids = dbids;
        this.nodeService = nodeService;
        this.tenantService = tenantService;
        this.prefetch = new BitSet(dbids.size());
        nodeRefs= new NodeRef[(dbids.size())];
        
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
                maxResults > 0 && dbids.size() < maxResults ? LimitBy.UNLIMITED : limitBy,
                PermissionEvaluationMode.EAGER, searchParameters);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#length()
     */
    @Override
    public int length()
    {
        return dbids.size();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNumberFound()
     */
    @Override
    public long getNumberFound()
    {
        return dbids.size();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNodeRef(int)
     */
    @Override
    public NodeRef getNodeRef(int n)
    {
        prefetch(n);
        return nodeRefs[n];
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
    
    private void prefetch(int n)
    {
       
        if (prefetch.get(n))
        {
            // The document was already processed
            return;
        }
        // Start at 'n' and process the the next bulk set
        int bulkFetchSize = getBulkFetchSize();
        if(bulkFetchSize < 1)
        {
            NodeRef nodeRef = nodeDao.getNodePair(dbids.get(n)).getSecond();
            nodeRefs[n] = nodeRef == null ? null : tenantService.getBaseName(nodeRef);
            return;
        }
        
        List<Long> fetchList = new ArrayList<Long>(bulkFetchSize);
        BitSet done = new BitSet(bulkFetchSize);
        int totalHits = dbids.size();
        for (int i = 0; i < bulkFetchSize; i++)
        {
            int next = n + i;
            if (next >= totalHits)
            {
                // We've hit the end
                break;
            }
            if (prefetch.get(next))
            {
                // This one is in there already
                continue;
            }
            // We store the node and mark it as prefetched
            prefetch.set(next);
            
            fetchList.add(dbids.get(next));
            done.set(next);
        }
        // Now bulk fetch
        if (fetchList.size() > 1)
        {
            nodeDao.cacheNodesById(fetchList);
            for (int i = done.nextSetBit(0); i >= 0; i = done.nextSetBit(i+1)) 
            {
                NodeRef nodeRef = nodeDao.getNodePair(fetchList.get(i)).getSecond();
                nodeRefs[n+1] = nodeRef == null ? null : tenantService.getBaseName(nodeRef);
            }
        }
    }

}
