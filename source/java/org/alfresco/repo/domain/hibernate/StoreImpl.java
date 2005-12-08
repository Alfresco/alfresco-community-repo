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
package org.alfresco.repo.domain.hibernate;

import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.domain.StoreKey;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Hibernate-specific implementation of the domain entity <b>store</b>.
 * 
 * @author Derek Hulley
 */
public class StoreImpl implements Store
{
	private StoreKey key;
    private Node rootNode;
    private transient StoreRef storeRef;

    public StoreImpl()
    {
    }
    
    /**
     * @see #getKey()
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof Node))
        {
            return false;
        }
        Node that = (Node) obj;
        return (this.getKey().equals(that.getKey()));
    }
    
    /**
     * @see #getKey()
     */
    public int hashCode()
    {
        return getKey().hashCode();
    }
    
    /**
     * @see #getStoreRef()()
     */
    public String toString()
    {
        return getStoreRef().toString();
    }

    public StoreKey getKey() {
		return key;
	}

	public synchronized void setKey(StoreKey key) {
		this.key = key;
        this.storeRef = null;
	}

    public Node getRootNode()
    {
        return rootNode;
    }

    public void setRootNode(Node rootNode)
    {
        this.rootNode = rootNode;
    }
    
    /**
     * Lazily constructs <code>StoreRef</code> instance referencing this entity
     */
    public synchronized StoreRef getStoreRef()
    {
        if (storeRef == null && key != null)
        {
            storeRef = new StoreRef(key.getProtocol(), key.getIdentifier());
        }
        return storeRef;
    }
}