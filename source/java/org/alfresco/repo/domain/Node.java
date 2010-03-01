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
package org.alfresco.repo.domain;

import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Interface for persistent <b>node</b> objects.
 * <p>
 * Specific instances of nodes are unique, but may share GUIDs across stores.
 * 
 * @author Derek Hulley
 */
public interface Node
{
    /**
     * Convenience method to get the reference to the node
     * 
     * @return Returns the reference to this node
     */
    public NodeRef getNodeRef();

    /**
     * Convenience method to get the node's type
     * 
     * @param qnameDAO          the helper DAO
     * @return                  the node's type QName
     */
    public QName getTypeQName(QNameDAO qnameDAO);
    
    /**
     * Convenience method to set the node's type
     * 
     * @param qnameDAO          the helper DAO
     * @param qname             the node's type QName
     */
    public void setTypeQName(QNameDAO qnameDAO, QName qname);
    
    /**
     * @return Returns the auto-generated ID
     */
    public Long getId();
    
    /**
     * @return  Returns the current version number
     */
    public Long getVersion();
    
    public Store getStore();
    
    public void setStore(Store store);
    
    public String getUuid();
    
    public void setUuid(String uuid);

    public Transaction getTransaction();
    
    public void setTransaction(Transaction transaction);
    
    public boolean getDeleted();

    public void setDeleted(boolean deleted);
    
    public Long getTypeQNameId();
    
    public void setTypeQNameId(Long typeQNameId);

    public DbAccessControlList getAccessControlList();

    public void setAccessControlList(DbAccessControlList accessControlList);

    public Set<Long> getAspects();
    
    public Map<PropertyMapKey, NodePropertyValue> getProperties();
    
    public AuditableProperties getAuditableProperties();

    public void setAuditableProperties(AuditableProperties auditableProperties);
}
