/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeAssoc;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

/**
 * Hibernate-specific implementation of the generic node association
 * 
 * @author Derek Hulley
 */
public class NodeAssocImpl implements NodeAssoc, Serializable
{
    private static final long serialVersionUID = 864534636913524867L;

    private Long id;
    private Long version;
    private Node source;
    private Node target;
    private Long typeQNameId;
    
    private transient ReadLock refReadLock;
    private transient WriteLock refWriteLock;
    private transient AssociationRef nodeAssocRef;
    private transient QName typeQName;

    public NodeAssocImpl()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        refReadLock = lock.readLock();
        refWriteLock = lock.writeLock();
    }

    public void buildAssociation(Node sourceNode, Node targetNode)
    {
        // add the forward associations
        this.setTarget(targetNode);
        this.setSource(sourceNode);
    }
    
    public AssociationRef getNodeAssocRef(QNameDAO qnameDAO)
    {
        boolean trashReference = false;
        // first check if it is available
        refReadLock.lock();
        try
        {
            if (nodeAssocRef != null)
            {
                // double check that the parent and child node references match those of our reference
                if (nodeAssocRef.getSourceRef() != source.getNodeRef() ||
                        nodeAssocRef.getTargetRef() != target.getNodeRef())
                {
                    trashReference = true;
                }
                else
                {
                    // we are sure that the reference is correct
                    return nodeAssocRef;
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
            if (typeQName == null)
            {
                typeQName = qnameDAO.getQName(typeQNameId).getSecond();
            }
            // double check
            if (nodeAssocRef == null || trashReference)
            {
                nodeAssocRef = new AssociationRef(
                        this.id,
                        getSource().getNodeRef(),
                        this.typeQName,
                        getTarget().getNodeRef());
            }
            return nodeAssocRef;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

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
        // get write lock
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

    public String toString()
    {
        StringBuffer sb = new StringBuffer(32);
        sb.append("NodeAssoc")
          .append("[ source=").append(source)
          .append(", target=").append(target)
          .append(", type=").append(typeQNameId)
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
        else if (!(obj instanceof NodeAssoc))
        {
            return false;
        }
        NodeAssoc that = (NodeAssoc) obj;
        if (EqualsHelper.nullSafeEquals(this.typeQNameId, that.getId()))
        {
            return true;
        }
        else
        {
            return (EqualsHelper.nullSafeEquals(this.typeQNameId, that.getTypeQNameId())
                    && EqualsHelper.nullSafeEquals(this.getTarget(), that.getTarget())
                    && EqualsHelper.nullSafeEquals(this.getSource(), that.getSource())
                    );
        }
    }
    
    public int hashCode()
    {
        return (typeQNameId == null ? 0 : typeQNameId.hashCode());
    }

    public Long getId()
    {
        return id;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setId(long id)
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

    public Node getSource()
    {
        return source;
    }

    /**
     * For internal use
     */
    private void setSource(Node source)
    {
        refWriteLock.lock();
        try
        {
            this.source = source;
            this.nodeAssocRef = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public Node getTarget()
    {
        return target;
    }

    /**
     * For internal use
     */
    private void setTarget(Node target)
    {
        refWriteLock.lock();
        try
        {
            this.target = target;
            this.nodeAssocRef = null;
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
            this.nodeAssocRef = null;
            this.typeQName = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
}
