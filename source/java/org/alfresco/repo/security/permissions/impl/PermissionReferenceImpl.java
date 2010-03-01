/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.security.permissions.impl;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.service.namespace.QName;

/**
 * A simple permission reference (not persisted). A permission is identified by name for a given type, which is
 * identified by its qualified name.
 * 
 * @author andyh
 */
public class PermissionReferenceImpl extends AbstractPermissionReference
{
    private static ReadWriteLock lock = new ReentrantReadWriteLock();

    private static HashMap<QName, HashMap<String, PermissionReferenceImpl>> instances = new HashMap<QName, HashMap<String, PermissionReferenceImpl>>();

    /**
     * 
     */
    private static final long serialVersionUID = -8639601925783501443L;

    private QName qName;

    private String name;

    /**
     * Factory method to create permission references
     * @param qName
     * @param name
     * @return the permissions reference
     */
    public static PermissionReferenceImpl getPermissionReference(QName qName, String name)
    {
        lock.readLock().lock();
        try
        {
            HashMap<String, PermissionReferenceImpl> typed = instances.get(qName);
            if(typed != null)
            {
                PermissionReferenceImpl instance = typed.get(name);
                if(instance != null)
                {
                    return instance;
                }
            }
        }
        finally
        {
            lock.readLock().unlock();
        }
        
        lock.writeLock().lock();
        try
        {
            HashMap<String, PermissionReferenceImpl> typed = instances.get(qName);
            if(typed == null)
            {
                typed = new HashMap<String, PermissionReferenceImpl>();
                instances.put(qName, typed);
            }
            PermissionReferenceImpl instance = typed.get(name);
            if(instance == null)
            {
                instance = new PermissionReferenceImpl(qName, name);
                typed.put(name, instance);
            }
            return instance;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    protected PermissionReferenceImpl(QName qName, String name)
    {
        this.qName = qName;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public QName getQName()
    {
        return qName;
    }
}
