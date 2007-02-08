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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeAssoc;
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

    private long id;
    private Node source;
    private Node target;
    private QName typeQName;
    
    private transient ReadLock refReadLock;
    private transient WriteLock refWriteLock;
    private transient AssociationRef nodeAssocRef;

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
    
    public AssociationRef getNodeAssocRef()
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
            // double check
            if (nodeAssocRef == null || trashReference)
            {
                nodeAssocRef = new AssociationRef(
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

    public String toString()
    {
        StringBuffer sb = new StringBuffer(32);
        sb.append("NodeAssoc")
          .append("[ source=").append(source)
          .append(", target=").append(target)
          .append(", name=").append(getTypeQName())
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
        return (EqualsHelper.nullSafeEquals(this.getTypeQName(), that.getTypeQName())
                && EqualsHelper.nullSafeEquals(this.getTarget(), that.getTarget())
                && EqualsHelper.nullSafeEquals(this.getSource(), that.getSource()));
    }
    
    public int hashCode()
    {
        return (typeQName == null ? 0 : typeQName.hashCode());
    }

    public long getId()
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
            this.nodeAssocRef = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
}
