/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.service.namespace;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class QNameCache
{
    private final int maxSize;
    private final Map<QName, String> qNameToNameCache = new HashMap<QName, String>();
    private final Map<String, QName> nameToQNameCache = new HashMap<String, QName>();
    
    private final ReentrantReadWriteLock qNameToNameLock = new ReentrantReadWriteLock();
    private final WriteLock qNameToNameWriteLock = qNameToNameLock.writeLock();
    private final ReadLock qNameToNameReadLock = qNameToNameLock.readLock();

    private final ReentrantReadWriteLock nameToQNameLock = new ReentrantReadWriteLock();
    private final WriteLock nameToQNameWriteLock = nameToQNameLock.writeLock();
    private final ReadLock nameToQNameReadLock = nameToQNameLock.readLock();
    
    public QNameCache(int maxSize)
    {
        this.maxSize = maxSize;
    }

    public String getName(QName qName)
    {
        qNameToNameReadLock.lock();
        try
        {
            return qNameToNameCache.get(qName);
        }
        finally
        {
            qNameToNameReadLock.unlock();
        }
    }
    
    public void putQNameToName(QName qName, String name)
    {
        qNameToNameWriteLock.lock();
        try
        {
            if(qNameToNameCache.size()>maxSize)
            {
                qNameToNameCache.clear();
            }
            qNameToNameCache.put(qName, name);
        }
        finally
        {
            qNameToNameWriteLock.unlock();
        }
    }
    
    public QName getQName(String name)
    {
        nameToQNameReadLock.lock();
        try
        {
            if(nameToQNameCache.size()>maxSize)
            {
                nameToQNameCache.clear();
            }
            return nameToQNameCache.get(name);
        }
        finally
        {
            nameToQNameReadLock.unlock();
        }
    }
    
    public void putNameToQName(String name, QName qName)
    {
        nameToQNameWriteLock.lock();
        try
        {
            nameToQNameCache.put(name, qName);
        }
        finally
        {
            nameToQNameWriteLock.unlock();
        }
    }
    
    public void clear()
    {
        nameToQNameWriteLock.lock();
        try
        {
            nameToQNameCache.clear();
        }
        finally
        {
            nameToQNameWriteLock.unlock();
        }
        qNameToNameWriteLock.lock();
        try
        {
            qNameToNameCache.clear();
        }
        finally
        {
            qNameToNameWriteLock.unlock();
        }
        
    }
}
