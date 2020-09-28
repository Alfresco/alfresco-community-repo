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
package org.alfresco.util.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.alfresco.api.AlfrescoPublicApi;   
import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * An generic registry of objects held by name.  This is effectively a strongly-typed,
 * synchronized map. 
 * 
 * @author Derek Hulley
 * @since 3.2
 */
@AlfrescoPublicApi
public class NamedObjectRegistry<T>
{
    private static final Log logger = LogFactory.getLog(NamedObjectRegistry.class);
    
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;
    
    private Class<T> storageType;
    private Pattern namePattern;
    private final Map<String, T> objects;

    /**
     * Default constructor.  The {@link #setStorageType(Class)} method must be called.
     */
    public NamedObjectRegistry()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        this.namePattern = null;                            // Deliberately null
        this.storageType = null;                            // Deliberately null
        this.objects = new HashMap<String, T>(13);
    }
    
    /**
     * Constructor that takes care of {@link #setStorageType(Class)}.
     * 
     * @see #setStorageType(Class)
     */
    public NamedObjectRegistry(Class<T> type)
    {
        this();
        setStorageType(type);
    }

    /**
     * Set the type of class that the registry holds.  Any attempt to register a
     * an instance of another type will be rejected.
     * 
     * @param clazz                     the type to store
     */
    public void setStorageType(Class<T> clazz)
    {
        writeLock.lock();
        try
        {
            this.storageType = clazz;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Optionally set a pattern to which all object names must conform
     * @param namePattern   a regular expression
     */
    public void setNamePattern(String namePattern)
    {
        writeLock.lock();
        try
        {
            this.namePattern = Pattern.compile(namePattern);
        }
        catch (PatternSyntaxException e)
        {
            throw new AlfrescoRuntimeException(
                    "Regular expression compilation failed for property 'namePrefix': " + e.getMessage(),
                    e);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Register a named object instance.
     * 
     * @param name          the name of the object
     * @param object        the instance to register, which correspond to the type
     */
    public void register(String name, T object)
    {
        ParameterCheck.mandatoryString("name", name);
        ParameterCheck.mandatory("object", object);
        
        if (!storageType.isAssignableFrom(object.getClass()))
        {
            throw new IllegalArgumentException(
                    "This NameObjectRegistry only accepts objects of type " + storageType);
        }
        writeLock.lock();
        try
        {
            if (storageType == null)
            {
                throw new IllegalStateException(
                        "The registry has not been configured (setStorageType not yet called yet)");
            }
            if (namePattern != null)
            {
                if (!namePattern.matcher(name).matches())
                {
                    throw new IllegalArgumentException(
                            "Object name '" + name + "' does not match required pattern: " + namePattern);
                }
            }
            T prevObject = objects.put(name, object);
            if (prevObject != null && prevObject != object)
            {
                logger.warn(
                        "Overwriting name object in registry: \n" +
                        "   Previous: " + prevObject + "\n" +
                        "   New:      " + object);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * Get a named object if it has been registered
     * 
     * @param name          the name of the object to retrieve
     * @return              Returns the instance of the object, which will necessarily
     *                      be of the correct type, or <tt>null</tt>
     */
    public T getNamedObject(String name)
    {
        readLock.lock();
        try
        {
            // Get it
            return objects.get(name);
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    /**
     * @return              Returns a copy of the map of instances 
     */
    public Map<String, T> getAllNamedObjects()
    {
        readLock.lock();
        try
        {
            // Get it
            return new HashMap<String, T>(objects);
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    public void reset()
    {
        writeLock.lock();
        try
        {
            if (storageType == null)
            objects.clear();
        }
        finally
        {
            writeLock.unlock();
        }
    }
}
