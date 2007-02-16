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

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.EqualsHelper;

/**
 * Compound key for persistence of {@link org.alfresco.repo.domain.Node}
 * 
 * @author Derek Hulley
 */
public class NodeKey implements Serializable
{
    private static final long serialVersionUID = 3258695403221300023L;
    
    private String guid;
    private String protocol;
	private String identifier;

    public NodeKey()
    {
    }
    
    public NodeKey(NodeRef nodeRef)
    {
        this(nodeRef.getStoreRef(), nodeRef.getId());
    }
	
    public NodeKey(StoreRef storeRef, String guid)
    {
        setGuid(guid);
        setProtocol(storeRef.getProtocol());
        setIdentifier(storeRef.getIdentifier());
    }
    
    public NodeKey(StoreKey storeKey, String guid)
    {
        setGuid(guid);
        setProtocol(storeKey.getProtocol());
        setIdentifier(storeKey.getIdentifier());
    }
    
	public NodeKey(String protocol, String identifier, String guid)
	{
		setGuid(guid);
		setProtocol(protocol);
		setIdentifier(identifier);
	}
	
	public String toString()
	{
		return ("NodeKey[" +
				" id=" + guid +
				", protocol=" + protocol +
				", identifier=" + identifier +
				"]");
	}
    
    public int hashCode()
    {
        return this.guid.hashCode();
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (!(obj instanceof NodeKey))
        {
            return false;
        }
        NodeKey that = (NodeKey) obj;
        return (EqualsHelper.nullSafeEquals(this.guid, that.guid) &&
                EqualsHelper.nullSafeEquals(this.protocol, that.protocol) &&
                EqualsHelper.nullSafeEquals(this.identifier, that.identifier)
                );
    }
    
    public String getGuid()
    {
        return guid;
    }
    
    /**
     * Tamper-proof method only to be used by introspectors
     */
    private void setGuid(String id)
    {
        this.guid = id;
    }
    
    public String getProtocol()
    {
        return protocol;
    }
    
    /**
     * Tamper-proof method only to be used by introspectors
     */
    private void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }
    
    public String getIdentifier()
    {
        return identifier;
    }
    
    /**
     * Tamper-proof method only to be used by introspectors
     */
    private void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }
}
