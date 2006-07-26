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

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

/**
 * @author Derek Hulley
 */
public class ChildAssocImpl implements ChildAssoc, Serializable
{
    private static final long serialVersionUID = -8993272236626580410L;

    private Long id;
    private Node parent;
    private Node child;
    private QName typeQName;
    private QName qName;
    private boolean isPrimary;
    private int index;
    
    private transient ReadLock refReadLock;
    private transient WriteLock refWriteLock;
    private transient ChildAssociationRef childAssocRef;
    
    public ChildAssocImpl()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        refReadLock = lock.readLock();
        refWriteLock = lock.writeLock();

        setIndex(Integer.MAX_VALUE);              // comes last
    }

    public void buildAssociation(Node parentNode, Node childNode)
    {
        // add the forward associations
        this.setParent(parentNode);
        this.setChild(childNode);
        // add the inverse associations
        parentNode.getChildAssocs().add(this);
        childNode.getParentAssocs().add(this);
    }
    
    public void removeAssociation()
    {
        // maintain inverse assoc from parent node to this instance
        this.getParent().getChildAssocs().remove(this);
        // maintain inverse assoc from child node to this instance
        this.getChild().getParentAssocs().remove(this);
    }
    
    public ChildAssociationRef getChildAssocRef()
    {
        boolean trashReference = false;
        // first check if it is available
        refReadLock.lock();
        try
        {
            if (childAssocRef != null)
            {
                // double check that the parent and child node references match those of our reference
                if (childAssocRef.getParentRef() != parent.getNodeRef() ||
                        childAssocRef.getChildRef() != child.getNodeRef())
                {
                    trashReference = true;
                }
                else
                {
                    // we are sure that the reference is correct
                    return childAssocRef;
                }
            }
        }
        finally
        {
            refReadLock.unlock();
        }
        // get write lock
        refWriteLock.lock();
        try
        {
            // double check
            if (childAssocRef == null || trashReference)
            {
                childAssocRef = new ChildAssociationRef(
                        this.typeQName,
                        parent.getNodeRef(),
                        this.qName,
                        child.getNodeRef(),
                        this.isPrimary,
                        index);
            }
            return childAssocRef;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer(32);
        sb.append("ChildAssoc")
          .append("[ parent=").append(parent)
          .append(", child=").append(child)
          .append(", name=").append(getQname())
          .append(", isPrimary=").append(isPrimary)
          .append("]");
        return sb.toString();
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
        else if (!(obj instanceof ChildAssoc))
        {
            return false;
        }
        ChildAssoc that = (ChildAssoc) obj;
        if (EqualsHelper.nullSafeEquals(id, that.getId()))
        {
            return true;
        }
        else
        {
            return (
                    EqualsHelper.nullSafeEquals(this.getChild().getId(), that.getChild().getId())
                    && EqualsHelper.nullSafeEquals(this.getQname(), that.getQname())
                    && EqualsHelper.nullSafeEquals(this.getParent().getId(), that.getParent().getId())
                    && EqualsHelper.nullSafeEquals(this.getTypeQName(), that.getTypeQName())
                   );
        }
    }
    
    public int hashCode()
    {
        return (qName == null ? 0 : qName.hashCode());
    }

    /**
     * Orders the child associations by ID.  A smaller ID has a higher priority.
     * This may change once we introduce a changeable index against which to order.
     */
    public int compareTo(ChildAssoc another)
    {
        if (this == another)
        {
            return 0;
        }
        
        int thisIndex = this.getIndex();
        int anotherIndex = another.getIndex();
        
        Long thisId = this.getId();
        Long anotherId = another.getId();

        if (thisId == null)                     // this ID has not been set, make this instance greater
        {
            return -1; 
        }
        else if (anotherId == null)             // other ID has not been set, make this instance lesser
        {
            return 1;
        }
        else if (thisIndex == anotherIndex)     // use the explicit index
        {
            return thisId.compareTo(anotherId);
        }
        else                                    // fallback on order of creation 
        {
            return (thisIndex > anotherIndex) ? 1 : -1;     // a lower index, make this instance lesser
        }
    }

    public Long getId()
    {
        return id;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }

    public Node getParent()
    {
        return parent;
    }

    /**
     * For Hibernate use
     */
    private void setParent(Node parentNode)
    {
        refWriteLock.lock();
        try
        {
            this.parent = parentNode;
            this.childAssocRef = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public Node getChild()
    {
        return child;
    }

    /**
     * For Hibernate use
     */
    private void setChild(Node node)
    {
        refWriteLock.lock();
        try
        {
            child = node;
            this.childAssocRef = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
    
    public QName getTypeQName()
    {
        return typeQName;
    }
    
    public void setTypeQName(QName typeQName)
    {
        refWriteLock.lock();
        try
        {
            this.typeQName = typeQName;
            this.childAssocRef = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
    
    public QName getQname()
    {
        return qName;
    }

    public void setQname(QName qname)
    {
        refWriteLock.lock();
        try
        {
            this.qName = qname;
            this.childAssocRef = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public boolean getIsPrimary()
    {
        return isPrimary;
    }

    public void setIsPrimary(boolean isPrimary)
    {
        refWriteLock.lock();
        try
        {
            this.isPrimary = isPrimary;
            this.childAssocRef = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        refWriteLock.lock();
        try
        {
            this.index = index;
            this.childAssocRef = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
}
