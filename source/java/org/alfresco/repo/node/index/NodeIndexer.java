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
package org.alfresco.repo.node.index;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Passes index information to the index services.
 * 
 * @author Derek Hulley
 */
@SuppressWarnings("unused")
public class NodeIndexer
{
    private static Log logger = LogFactory.getLog(NodeIndexer.class);
    
    /** the component to index the node hierarchy */
    private Indexer indexer;
    /** enabled or disabled */
    private boolean enabled;
    
    public NodeIndexer()
    {
        enabled = true;
    }
    
    /**
     * @param indexer the indexer that will be index
     */
    public void setIndexer(Indexer indexer)
    {
        this.indexer = indexer;
    }
    
    /**
     * Explicit property to disable in-transaction indexing.
     * 
     * @param disabled      <tt>true</tt> to index nothing in-line
     */
    public void setDisabled(boolean disabled)
    {
        this.enabled = !disabled;
    }

    /* package */ void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * @deprecated
     */
    public void init()
    {
        logger.warn("NodeIndexer.init() has been deprecated.");
    }

    public void indexCreateNode(ChildAssociationRef childAssocRef)
    {
        if (enabled)
        {
            indexer.createNode(childAssocRef);
        }
    }

    public void indexUpdateNode(NodeRef nodeRef)
    {
        if (enabled)
        {
            indexer.updateNode(nodeRef);
        }
    }

    public void indexDeleteNode(ChildAssociationRef childAssocRef)
    {
        if (enabled)
        {
            indexer.deleteNode(childAssocRef);
        }
    }

    public void indexCreateChildAssociation(ChildAssociationRef childAssocRef)
    {
        if (enabled)
        {
            indexer.createChildRelationship(childAssocRef);
        }
    }

    public void indexDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        if (enabled)
        {
            indexer.deleteChildRelationship(childAssocRef);
        } 
    }
    
    public void indexUpdateChildAssociation(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        if (enabled)
        {
            indexer.updateChildRelationship(oldChildAssocRef, newChildAssocRef);
        }
    }
}
