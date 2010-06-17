/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.node;

import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Bean to capture <tt>StoreRef</tt> results.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class StoreEntity
{
    private Long id;
    private Long version;
    private String protocol;
    private String identifier;
    private NodeEntity rootNode;
    
    /**
     * Required default constructor
     */
    public StoreEntity()
    {
    }
        
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("StoreEntity")
          .append("[ ID=").append(id)
          .append(", protocol=").append(protocol)
          .append(", identifier=").append(identifier)
          .append(", rootNode=").append(rootNode)
          .append("]");
        return sb.toString();
    }
    
    public StoreRef getStoreRef()
    {
        return new StoreRef(protocol, identifier);
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public NodeEntity getRootNode()
    {
        return rootNode;
    }

    public void setRootNode(NodeEntity rootNode)
    {
        this.rootNode = rootNode;
    }
}
