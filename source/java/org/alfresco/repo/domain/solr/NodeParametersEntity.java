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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.solr.NodeParameters;

/**
 * Stores node query parameters for use in SOLR DAO queries
 * 
 * @since 4.0
 */
public class NodeParametersEntity extends NodeParameters
{
    private List<Long> includeTypeIds;
    private List<Long> excludeTypeIds;
    
    private List<Long> includeAspectIds;
    private List<Long> excludeAspectIds;
    
    /**
     * Public constructor, but not generally useful
     */
    public NodeParametersEntity()
    {
        
    }
    
    /**
     * Construct from higher-level parameters
     */
    public NodeParametersEntity(NodeParameters params, QNameDAO qnameDAO)
    {
        this.setFromNodeId(params.getFromNodeId());
        this.setToNodeId(params.getToNodeId());

        this.setFromTxnId(params.getFromTxnId());
        this.setToTxnId(params.getToTxnId());
        this.setTransactionIds(params.getTransactionIds());

        this.setStoreIdentifier(params.getStoreIdentifier());
        this.setStoreProtocol(params.getStoreProtocol());
        
        // Translate the QNames, if provided
        if (params.getIncludeNodeTypes() != null)
        {
            Set<Long> qnamesIds = qnameDAO.convertQNamesToIds(params.getIncludeNodeTypes(), false);
            this.setIncludeTypeIds(new ArrayList<Long>(qnamesIds));
        }

        if (params.getExcludeNodeTypes() != null)
        {
            Set<Long> qnamesIds = qnameDAO.convertQNamesToIds(params.getExcludeNodeTypes(), false);
            this.setExcludeTypeIds(new ArrayList<Long>(qnamesIds));
        }
        
        if (params.getExcludeAspects() != null)
        {
            Set<Long> qnamesIds = qnameDAO.convertQNamesToIds(params.getExcludeAspects(), false);
            this.setExcludeAspectIds(new ArrayList<Long>(qnamesIds));
        }

        if (params.getIncludeAspects() != null)
        {
            Set<Long> qnamesIds = qnameDAO.convertQNamesToIds(params.getIncludeAspects(), false);
            this.setIncludeAspectIds(new ArrayList<Long>(qnamesIds));
        }
    }
    
    public boolean getStoreFilter()
    {
        return (getStoreProtocol() != null || getStoreIdentifier() != null);
    }

    public List<Long> getIncludeAspectIds()
    {
        return includeAspectIds;
    }

    public void setIncludeAspectIds(List<Long> includeAspectIds)
    {
        this.includeAspectIds = includeAspectIds;
    }

    public List<Long> getExcludeAspectIds()
    {
        return excludeAspectIds;
    }

    public void setExcludeAspectIds(List<Long> excludeAspectIds)
    {
        this.excludeAspectIds = excludeAspectIds;
    }

    public List<Long> getIncludeTypeIds()
    {
        return includeTypeIds;
    }

    public void setIncludeTypeIds(List<Long> includeTypeIds)
    {
        this.includeTypeIds = includeTypeIds;
    }

    public List<Long> getExcludeTypeIds()
    {
        return excludeTypeIds;
    }

    public void setExcludeTypeIds(List<Long> excludeTypeIds)
    {
        this.excludeTypeIds = excludeTypeIds;
    }
    
    public boolean isIncludeNodesTable()
    {
        return (getFromNodeId() != null || getToNodeId() != null || getIncludeTypeIds() != null || getExcludeTypeIds() != null || getIncludeAspectIds() != null || getExcludeAspectIds() != null);
    }
}
