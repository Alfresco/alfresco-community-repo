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
package org.alfresco.repo.domain.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Interface for beans carrying general information for <b>alf_node</b> data.
 * 
 * @author andyh
 */
public interface Node extends NodeIdAndAclId
{
    /**
     * Helper method to get a key that includes the node and its current version number
     */
    public NodeVersionKey getNodeVersionKey();
    
    /**
     * Helper method to force the instance to be read-only
     */
    public void lock();
    
    public abstract NodeRef getNodeRef();

    public NodeRef.Status getNodeStatus(QNameDAO qnameDAO);
    
    public abstract Pair<Long, NodeRef> getNodePair();
    
    /**
     * Checks the {@link #getTypeQNameId() type} of the node to determine if the node is deleted
     * @param qnameDAO          DAO to work out type IDs
     * @return                  <tt>true</tt> if the node is {@link ContentModel#TYPE_DELETED}
     */
    public abstract boolean getDeleted(QNameDAO qnameDAO);

    public abstract Long getVersion();

    public abstract StoreEntity getStore();

    public abstract String getUuid();

    public abstract Long getTypeQNameId();
    
    public abstract Long getLocaleId();

    public abstract TransactionEntity getTransaction();

    public abstract AuditablePropertiesEntity getAuditableProperties();

}