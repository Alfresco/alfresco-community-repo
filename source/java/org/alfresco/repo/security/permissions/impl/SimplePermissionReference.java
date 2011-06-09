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
 * A simple permission reference.
 * 
 * @author andyh
 */
public final class SimplePermissionReference extends AbstractPermissionReference
{   
    private static final long serialVersionUID = 637302438293417818L;

    private static ReadWriteLock lock = new ReentrantReadWriteLock();

    private static HashMap<QName, HashMap<String, SimplePermissionReference>> instances = new HashMap<QName, HashMap<String, SimplePermissionReference>>();

    /**
     * Factory method to create simple permission refrences
     * 
     * @return a simple permission reference
     */
    public static SimplePermissionReference getPermissionReference(QName qName, String name)
    {
        lock.readLock().lock();
        try
        {
            HashMap<String, SimplePermissionReference> typed = instances.get(qName);
            if(typed != null)
            {
                SimplePermissionReference instance = typed.get(name);
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
            HashMap<String, SimplePermissionReference> typed = instances.get(qName);
            if(typed == null)
            {
                typed = new HashMap<String, SimplePermissionReference>();
                instances.put(qName, typed);
            }
            SimplePermissionReference instance = typed.get(name);
            if(instance == null)
            {
                instance = new SimplePermissionReference(qName, name);
                typed.put(name, instance);
            }
            return instance;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    
    
    /*
     * The type
     */
    private QName qName;
    
    /*
     * The name of the permission
     */
    private String name;
    
    
    protected SimplePermissionReference(QName qName, String name)
    {
        super();
        this.qName = qName;
        this.name = name;
    }

    public QName getQName()
    {
        return qName;
    }

    public String getName()
    {
        return name;
    }

}
