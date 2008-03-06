/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.NamespaceEntity;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.QNameEntity;
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
    private Long version;
    private Node parent;
    private Node child;
    private QNameEntity typeQName;
    private NamespaceEntity qnameNamespace;
    private String qnameLocalName;
    private String childNodeName;
    private long childNodeNameCrc;
    private boolean isPrimary;
    private int index;
    
    private transient ReadLock refReadLock;
    private transient WriteLock refWriteLock;
    private transient ChildAssociationRef childAssocRef;
    private transient QName qname;
    
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
    }
    
    public void removeAssociation()
    {
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * This method is thread-safe and lazily creates the required references, if required.
     */
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
                        this.typeQName.getQName(),
                        parent.getNodeRef(),
                        this.getQname(),
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

    /**
     * {@inheritDoc}
     * <p>
     * This method is thread-safe and lazily creates the required references, if required.
     */
    public QName getQname()
    {
        // first check if it is available
        refReadLock.lock();
        try
        {
            if (qname != null)
            {
                return qname;
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
            if (qname == null )
            {
                qname = QName.createQName(qnameNamespace.getUri(), qnameLocalName);
            }
            return qname;
        }
        finally
        {
            refWriteLock.unlock();
        }
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
        return (EqualsHelper.nullSafeEquals(this.getTypeQName(), that.getTypeQName())
                && EqualsHelper.nullSafeEquals(this.getQname(), that.getQname())
                && EqualsHelper.nullSafeEquals(this.getChild(), that.getChild())
                && EqualsHelper.nullSafeEquals(this.getParent(), that.getParent()));
    }
    
    public int hashCode()
    {
        return (child == null ? 0 : child.hashCode());
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer(32);
        sb.append("ChildAssoc")
          .append("[ id=").append(id)
          .append(", parent=").append(parent.getId())
          .append(", child=").append(child.getId())
          .append(", child name=").append(childNodeName)
          .append(", child name crc=").append(childNodeNameCrc)
          .append(", assoc type=").append(getTypeQName().getQName())
          .append(", assoc name=").append(getQname())
          .append(", isPrimary=").append(isPrimary)
          .append("]");
        return sb.toString();
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

    public Long getVersion()
    {
        return version;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setVersion(Long version)
    {
        this.version = version;
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
    
    public QNameEntity getTypeQName()
    {
        return typeQName;
    }

    public void setTypeQName(QNameEntity typeQName)
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
    
    public NamespaceEntity getQnameNamespace()
    {
        return qnameNamespace;
    }

    public void setQnameNamespace(NamespaceEntity qnameNamespace)
    {
        refWriteLock.lock();
        try
        {
            this.qnameNamespace = qnameNamespace;
            this.childAssocRef = null;
            this.qname = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public String getQnameLocalName()
    {
        return qnameLocalName;
    }

    public void setQnameLocalName(String qnameLocalName)
    {
        refWriteLock.lock();
        try
        {
            this.qnameLocalName = qnameLocalName;
            this.childAssocRef = null;
            this.qname = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public String getChildNodeName()
    {
        return childNodeName;
    }

    public void setChildNodeName(String childNodeName)
    {
        this.childNodeName = childNodeName;
    }

    public long getChildNodeNameCrc()
    {
        return childNodeNameCrc;
    }

    public void setChildNodeNameCrc(long crc)
    {
        this.childNodeNameCrc = crc;
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
