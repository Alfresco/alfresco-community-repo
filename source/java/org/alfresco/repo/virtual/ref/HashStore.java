/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.virtual.ref;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A synchronized string hash code mapping store.<br>
 * Associates a string with a given string-hash.
 */
public class HashStore
{

    private HashMap<String, String> hashStore = new HashMap<>();

    private HashMap<String, String> lookupStore = new HashMap<>();

    private ReentrantReadWriteLock configurationLock = new ReentrantReadWriteLock();

    public void put(String string, String hash)
    {
        configurationLock.writeLock().lock();
        try
        {
            hashStore.put(string,
                          hash);
            lookupStore.put(hash,
                            string);
        }
        finally
        {
            configurationLock.writeLock().unlock();
        }
    }

    public String hash(String string)
    {
        configurationLock.readLock().lock();
        try
        {
            return hashStore.get(string);
        }
        finally
        {
            configurationLock.readLock().unlock();
        }

    }

    public String lookup(String hash)
    {
        configurationLock.readLock().lock();
        try
        {
            return lookupStore.get(hash);
        }
        finally
        {
            configurationLock.readLock().unlock();
        }

    }
}
