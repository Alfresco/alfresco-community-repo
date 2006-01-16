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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeAssoc;
import org.alfresco.repo.domain.NodeKey;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.Store;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Bean containing all the persistence data representing a <b>node</b>.
 * <p>
 * This implementation of the {@link org.alfresco.repo.domain.Node Node} interface is
 * Hibernate specific.
 * 
 * @author Derek Hulley
 */
public class NodeImpl implements Node
{
    private NodeKey key;
    private Store store;
    private QName typeQName;
    private NodeStatus status;
    private Set<QName> aspects;
    private Collection<NodeAssoc> sourceNodeAssocs;
    private Collection<NodeAssoc> targetNodeAssocs;
    private Collection<ChildAssoc> parentAssocs;
    private Collection<ChildAssoc> childAssocs;
    private Map<QName, PropertyValue> properties;
    private transient NodeRef nodeRef;

    public NodeImpl()
    {
        aspects = new HashSet<QName>(5);
        sourceNodeAssocs = new ArrayList<NodeAssoc>(3);
        targetNodeAssocs = new ArrayList<NodeAssoc>(3);
        parentAssocs = new ArrayList<ChildAssoc>(3);
        childAssocs = new ArrayList<ChildAssoc>(3);
        properties = new HashMap<QName, PropertyValue>(5);
    }
    
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
    
    public int hashCode()
    {
        return getKey().hashCode();
    }

    public NodeKey getKey() {
		return key;
	}

	public void setKey(NodeKey key) {
		this.key = key;
	}
    
    public Store getStore()
    {
        return store;
    }

    public synchronized void setStore(Store store)
    {
        this.store = store;
        this.nodeRef = null;
    }

    public QName getTypeQName()
    {
        return typeQName;
    }

    public void setTypeQName(QName typeQName)
    {
        this.typeQName = typeQName;
    }

    public NodeStatus getStatus()
    {
        return status;
    }

    public void setStatus(NodeStatus status)
    {
        this.status = status;
    }

    public Set<QName> getAspects()
    {
        return aspects;
    }
    
    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setAspects(Set<QName> aspects)
    {
        this.aspects = aspects;
    }

    public Collection<NodeAssoc> getSourceNodeAssocs()
    {
        return sourceNodeAssocs;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setSourceNodeAssocs(Collection<NodeAssoc> sourceNodeAssocs)
    {
        this.sourceNodeAssocs = sourceNodeAssocs;
    }

    public Collection<NodeAssoc> getTargetNodeAssocs()
    {
        return targetNodeAssocs;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setTargetNodeAssocs(Collection<NodeAssoc> targetNodeAssocs)
    {
        this.targetNodeAssocs = targetNodeAssocs;
    }
    
    public Collection<ChildAssoc> getParentAssocs()
    {
        return parentAssocs;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setParentAssocs(Collection<ChildAssoc> parentAssocs)
    {
        this.parentAssocs = parentAssocs;
    }

    public Collection<ChildAssoc> getChildAssocs()
    {
        return childAssocs;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setChildAssocs(Collection<ChildAssoc> childAssocs)
    {
        this.childAssocs = childAssocs;
    }

    public Map<QName, PropertyValue> getProperties()
    {
        return properties;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setProperties(Map<QName, PropertyValue> properties)
    {
        this.properties = properties;
    }

    /**
     * Thread-safe caching of the reference is provided
     */
    public synchronized NodeRef getNodeRef()
    {
        if (nodeRef == null && key != null)
        {
            nodeRef = new NodeRef(getStore().getStoreRef(), getKey().getGuid());
        }
        return nodeRef;
    }
    
    /**
     * @see #getNodeRef()
     */
    public String toString()
    {
        return getNodeRef().toString();
    }
}
