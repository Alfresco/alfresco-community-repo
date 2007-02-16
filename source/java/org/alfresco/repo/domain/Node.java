/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.domain;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
     * @return Returns the auto-generated ID
     */
    public Long getId();
    
    public Store getStore();
    
    public void setStore(Store store);
    
    public String getUuid();
    
    public void setUuid(String uuid);
    
    public QName getTypeQName();
    
    public void setTypeQName(QName typeQName);

    public Set<QName> getAspects();
    
    public Collection<ChildAssoc> getParentAssocs();

    public Map<QName, PropertyValue> getProperties();

    public DbAccessControlList getAccessControlList();

    public void setAccessControlList(DbAccessControlList accessControlList);
}
