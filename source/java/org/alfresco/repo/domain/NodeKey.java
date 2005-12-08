/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.domain;

import java.io.Serializable;

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
