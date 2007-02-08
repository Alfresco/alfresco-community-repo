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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.domain;

import java.io.Serializable;

import org.alfresco.util.EqualsHelper;

/**
 * Compound key for persistence of {@link org.alfresco.repo.domain.Store}
 * 
 * @author Derek Hulley
 */
public class StoreKey implements Serializable
{
    private static final long serialVersionUID = 3618140052220096569L;

    private String protocol;
    private String identifier;
	
	public StoreKey()
	{
	}
	
	public StoreKey(String protocol, String identifier)
	{
		setProtocol(protocol);
		setIdentifier(identifier);
	}
	
	public String toString()
	{
		return ("StoreKey[" +
				" protocol=" + protocol +
				", identifier=" + identifier +
				"]");
	}
    
    public int hashCode()
    {
        return (this.protocol.hashCode() + this.identifier.hashCode());
    }
	
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (!(obj instanceof StoreKey))
		{
			return false;
		}
		StoreKey that = (StoreKey) obj;
		return (EqualsHelper.nullSafeEquals(this.protocol, that.protocol) &&
                EqualsHelper.nullSafeEquals(this.identifier, that.identifier));
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
