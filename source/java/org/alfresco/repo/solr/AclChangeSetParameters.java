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
package org.alfresco.repo.solr;

import java.util.List;
import java.util.Set;

import org.alfresco.service.namespace.QName;

/**
 * Stores ACL change set query parameters
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class AclChangeSetParameters
{
    private List<Long> transactionIds;
    private Long fromTxnId;
    private Long toTxnId;

    private Long fromNodeId;
    private Long toNodeId;

    // default is 'all' results
    private int maxResults = 0;

    private String storeProtocol;
    private String storeIdentifier;
    
    private Set<QName> includeNodeTypes;
    private Set<QName> excludeNodeTypes;
    
    private Set<QName> includeAspects;
    private Set<QName> excludeAspects;
    
    public int getMaxResults()
    {
        return maxResults;
    }

    public void setMaxResults(int maxResults)
    {
        this.maxResults = maxResults;
    }

    public boolean getStoreFilter()
    {
        return (storeProtocol != null || storeIdentifier != null);
    }
    
    public void setStoreProtocol(String storeProtocol)
    {
        this.storeProtocol = storeProtocol;
    }

    public String getStoreProtocol()
    {
        return storeProtocol;
    }

    public void setStoreIdentifier(String storeIdentifier)
    {
        this.storeIdentifier = storeIdentifier;
    }

    public String getStoreIdentifier()
    {
        return storeIdentifier;
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

    public Set<QName> getIncludeNodeTypes()
    {
        return includeNodeTypes;
    }

    public Set<QName> getExcludeNodeTypes()
    {
        return excludeNodeTypes;
    }

    public Set<QName> getIncludeAspects()
    {
        return includeAspects;
    }

    public Set<QName> getExcludeAspects()
    {
        return excludeAspects;
    }

    public void setIncludeNodeTypes(Set<QName> includeNodeTypes)
    {
        this.includeNodeTypes = includeNodeTypes;
    }

    public void setExcludeNodeTypes(Set<QName> excludeNodeTypes)
    {
        this.excludeNodeTypes = excludeNodeTypes;
    }

    public void setIncludeAspects(Set<QName> includeAspects)
    {
        this.includeAspects = includeAspects;
    }

    public void setExcludeAspects(Set<QName> excludeAspects)
    {
        this.excludeAspects = excludeAspects;
    }
}
