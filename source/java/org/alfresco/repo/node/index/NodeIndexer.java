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
import org.alfresco.service.cmr.repository.StoreRef;
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
    
    public void indexDeleteStore(StoreRef storeRef)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("indexDeleteNode", new Exception("Stack Trace"));
        }
        indexer.deleteIndex(storeRef);
    }

    public void indexCreateNode(ChildAssociationRef childAssocRef)
    {
        if (enabled)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("indexCreateNode: " + childAssocRef, new Exception("Stack Trace"));
            }
            indexer.createNode(childAssocRef);
        }
    }

    public void indexUpdateNode(NodeRef nodeRef)
    {
        if (enabled)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("indexUpdateNode: " + nodeRef, new Exception("Stack Trace"));
            }
            indexer.updateNode(nodeRef);
        }
    }

    public void indexDeleteNode(ChildAssociationRef childAssocRef)
    {
        if (enabled)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("indexDeleteNode: " + childAssocRef, new Exception("Stack Trace"));
            }
            indexer.deleteNode(childAssocRef);
        }
    }

    public void indexCreateChildAssociation(ChildAssociationRef childAssocRef)
    {
        if (enabled)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("indexCreateChildAssociation: " + childAssocRef, new Exception("Stack Trace"));
            }
            indexer.createChildRelationship(childAssocRef);
        }
    }

    public void indexDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        if (enabled)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("indexDeleteChildAssociation: " + childAssocRef, new Exception("Stack Trace"));
            }
            indexer.deleteChildRelationship(childAssocRef);
        } 
    }
    
    public void indexUpdateChildAssociation(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        if (enabled)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("indexUpdateChildAssociation: " + oldChildAssocRef + " -> " + newChildAssocRef, new Exception("Stack Trace"));
            }
            indexer.updateChildRelationship(oldChildAssocRef, newChildAssocRef);
        }
    }
}
