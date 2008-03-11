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

import org.alfresco.repo.domain.NamespaceEntity;
import org.alfresco.repo.domain.QNameEntity;
import org.alfresco.service.namespace.QName;

/**
 * Hibernate-specific implementation of the domain entity <b>QnameEntity</b>.
 * 
 * @author Derek Hulley
 */
public class QNameEntityImpl implements QNameEntity, Serializable
{
    private static final long serialVersionUID = -4211902156023915846L;

    private Long id;
    private Long version;
    private NamespaceEntity namespace;
    private String localName;

    private transient ReadLock refReadLock;
    private transient WriteLock refWriteLock;
    private transient QName qname;

    public QNameEntityImpl()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        refReadLock = lock.readLock();
        refWriteLock = lock.writeLock();
    }
    
    /**
     * Lazily constructs a <code>QName</code> instance referencing this entity
     */
    public QName getQName()
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
                String namespaceUri = namespace.getUri();
                if (namespaceUri.equals(NamespaceEntityImpl.EMPTY_URI_SUBSTITUTE)) 
                {
                    namespaceUri = "";
                }
                qname = QName.createQName(namespaceUri, localName);
            }
            return qname;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
    
    /**
     * @see #getStoreRef()()
     */
    public String toString()
    {
        return getQName().toString();
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
        else if (!(obj instanceof QNameEntity))
        {
            return false;
        }
        QNameEntity that = (QNameEntity) obj;
        return (this.getQName().equals(that.getQName()));
    }
    
    /**
     * @see #getKey()
     */
    public int hashCode()
    {
        return getQName().hashCode();
    }
    
    public Long getId()
    {
        return id;
    }

    /**
     * For Hibernate use.
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

    public NamespaceEntity getNamespace()
    {
        return namespace;
    }
    
    public void setNamespace(NamespaceEntity namespace)
    {
        refWriteLock.lock();
        try
        {
            this.namespace = namespace;
            this.qname = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public String getLocalName()
    {
        return localName;
    }
    
    public void setLocalName(String localName)
    {
        refWriteLock.lock();
        try
        {
            this.localName = localName;
            this.qname = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
}