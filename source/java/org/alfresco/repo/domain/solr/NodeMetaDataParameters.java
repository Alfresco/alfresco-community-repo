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
package org.alfresco.repo.domain.solr;

import java.util.List;

/**
 * Stores node meta data query parameters for use in SOLR DAO queries
 * 
 * @since 4.0
 */
public class NodeMetaDataParameters
{
    private List<Long> transactionIds;
    private Long fromTxnId;
    private Long toTxnId;

    // default is 'all' results
    private int maxResults = 0;
    
    private Long fromNodeId;
    private Long toNodeId;
    private List<Long> nodeIds;

    
    public int getMaxResults()
    {
        return maxResults;
    }

    public void setMaxResults(int maxResults)
    {
        this.maxResults = maxResults;
    }

    public List<Long> getNodeIds()
    {
        return nodeIds;
    }

    public void setNodeIds(List<Long> nodeIds)
    {
        this.nodeIds = nodeIds;
    }

    public void setTransactionIds(List<Long> txnIds)
    {
        this.transactionIds = txnIds;
    }

    public List<Long> getTransactionIds()
    {
        return transactionIds;
    }

    public Long getFromTxnId()
    {
        return fromTxnId;
    }

    public void setFromTxnId(Long fromTxnId)
    {
        this.fromTxnId = fromTxnId;
    }

    public Long getToTxnId()
    {
        return toTxnId;
    }

    public void setToTxnId(Long toTxnId)
    {
        this.toTxnId = toTxnId;
    }

    public Long getFromNodeId()
    {
        return fromNodeId;
    }

    public void setFromNodeId(Long fromNodeId)
    {
        this.fromNodeId = fromNodeId;
    }

    public Long getToNodeId()
    {
        return toNodeId;
    }

    public void setToNodeId(Long toNodeId)
    {
        this.toNodeId = toNodeId;
    }
}
