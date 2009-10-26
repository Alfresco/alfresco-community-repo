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
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.zip.CRC32;

import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.QNameDAO;
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
    private Long typeQNameId;
    private Long qnameNamespaceId;
    private String qnameLocalName;
    private long qnameCrc;
    private String childNodeName;
    private long childNodeNameCrc;
    private boolean isPrimary;
    private int index;
    
    private transient ReadLock refReadLock;
    private transient WriteLock refWriteLock;
    private transient ChildAssociationRef childAssocRef;
    private transient QName typeQName;
    private transient QName qname;
    
    public ChildAssocImpl()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        refReadLock = lock.readLock();
        refWriteLock = lock.writeLock();

        index = -1;                     // The index is irrelevant
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
    public ChildAssociationRef getChildAssocRef(QNameDAO qnameDAO)
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
                if (typeQName == null)
                {
                    typeQName = qnameDAO.getQName(this.typeQNameId).getSecond();
                }
                if (qname == null )
                {
                    String qnameNamespace = qnameDAO.getNamespace(qnameNamespaceId).getSecond();
                    qname = QName.createQName(qnameNamespace, qnameLocalName);
                }
                childAssocRef = new ChildAssociationRef(
                        typeQName,
                        parent.getNodeRef(),
                        qname,
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
     */
    public QName getTypeQName(QNameDAO qnameDAO)
    {
        refReadLock.lock();
        try
        {
            if (typeQName != null)
            {
                return typeQName;
            }
        }
        finally
        {
            refReadLock.unlock();
        }
        refWriteLock.lock();
        try
        {
            typeQName = qnameDAO.getQName(typeQNameId).getSecond();
            return typeQName;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public void setTypeQName(QNameDAO qnameDAO, QName typeQName)
    {
        Long typeQNameId = qnameDAO.getOrCreateQName(typeQName).getFirst();
        refWriteLock.lock();
        try
        {
            setTypeQNameId(typeQNameId);
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
    public QName getQName(QNameDAO qnameDAO)
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
                String qnameNamespace = qnameDAO.getNamespace(qnameNamespaceId).getSecond();
                qname = QName.createQName(qnameNamespace, qnameLocalName);
            }
            return qname;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public void setQName(QNameDAO qnameDAO, QName qname)
    {
        String assocQNameNamespace = qname.getNamespaceURI();
        String assocQNameLocalName = qname.getLocalName();
        Long assocQNameNamespaceId = qnameDAO.getOrCreateNamespace(assocQNameNamespace).getFirst();
        Long assocQNameCrc = getCrc(qname);
        // get write lock
        refWriteLock.lock();
        try
        {
            setQnameNamespaceId(assocQNameNamespaceId);
            setQnameLocalName(assocQNameLocalName);
            setQnameCrc(assocQNameCrc);
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
    
    public static long getCrc(QName qname)
    {
        CRC32 crc = new CRC32();
        try
        {
            crc.update(qname.getNamespaceURI().getBytes("UTF-8"));
            crc.update(qname.getLocalName().getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 encoding is not supported");
        }
        return crc.getValue();
        
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
            return (EqualsHelper.nullSafeEquals(this.getParent(), that.getParent())
                    && EqualsHelper.nullSafeEquals(this.typeQNameId, that.getTypeQNameId())
                    && EqualsHelper.nullSafeEquals(this.getChild(), that.getChild())
                    && EqualsHelper.nullSafeEquals(this.qnameLocalName, that.getQnameLocalName())
                    && EqualsHelper.nullSafeEquals(this.qnameNamespaceId, that.getQnameNamespaceId())
                    );
        }
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
          .append(", assoc type=").append(typeQNameId)
          .append(", assoc qname ns=").append(qnameNamespaceId)
          .append(", assoc qname localname=").append(qnameLocalName)
          .append(", assoc qname crc=").append(qnameCrc)
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
    
    public Long getTypeQNameId()
    {
        return typeQNameId;
    }

    public void setTypeQNameId(Long typeQNameId)
    {
        refWriteLock.lock();
        try
        {
            this.typeQNameId = typeQNameId;
            this.childAssocRef = null;
            this.typeQName = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
    
    public Long getQnameNamespaceId()
    {
        return qnameNamespaceId;
    }

    public void setQnameNamespaceId(Long qnameNamespaceId)
    {
        refWriteLock.lock();
        try
        {
            this.qnameNamespaceId = qnameNamespaceId;
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
    
    public long getQnameCrc()
    {
        return qnameCrc;
    }

    public void setQnameCrc(long crc)
    {
        this.qnameCrc = crc;
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
