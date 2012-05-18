/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.cluster;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.util.PropertyCheck;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * Provides a way of lazily creating HazelcastInstances for a given configuration.
 * The HazelcastInstance will not be created until {@link #getInstance()} is called.
 * <p>
 * An intermediary class such as this is required in order to avoid starting
 * Hazelcast instances when clustering is not configured/required. Otherwise
 * simply by defining a HazelcastInstance bean clustering would spring into life.
 * <p>
 * Please note this class provides non-static access deliberately, and should be
 * injected into any clients that require its services.
 * 
 * @author Matt Ward
 */
public class HazelcastInstanceFactory
{
    private Config config;
    private HazelcastInstance hazelcastInstance;
    /** Guards {@link #config} and {@link #hazelcastInstance} */
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    
    public HazelcastInstance getInstance()
    {
        rwLock.readLock().lock();
        try
        {
            if (hazelcastInstance != null)
            {
                return hazelcastInstance;
            }
        }
        finally
        {
            rwLock.readLock().unlock();
        }
        
        // hazelcastInstance is null, so create it.
        rwLock.writeLock().lock();
        try
        {
            // Double check condition hasn't changed in between locks.
            if (hazelcastInstance == null)
            {
                hazelcastInstance = Hazelcast.newHazelcastInstance(config);
            }
            return hazelcastInstance;
        }
        finally
        {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Checks whether hazelcast has been given a valid cluster name. If so,
     * then clustering is considered enabled. This condition should be checked
     * before calling {@link #getInstance()}.
     * 
     * @return true if clustering is enabled, false otherwise.
     */
    public boolean isClusteringEnabled()
    {
        rwLock.readLock().lock();
        try
        {
            String clusterName = config.getGroupConfig().getName();
            return (PropertyCheck.isValidPropertyString(clusterName));
        }
        finally
        {
            rwLock.readLock().unlock();
        }
    }
    
    /**
     * Retrieve the name of the cluster for the configuration used by this factory.
     * 
     * @return String - the cluster name.
     */
    public String getClusterName()
    {
        rwLock.readLock().lock();
        try
        {
            String clusterName = config.getGroupConfig().getName();
            return clusterName;
        }
        finally
        {
            rwLock.readLock().unlock();
        }
    }
    
    /**
     * Sets the Hazelcast configuration that will be used by this factory when
     * creating the HazelcastInstance.
     * 
     * @param config Hazelcast configuration
     */
    public void setConfig(Config config)
    {
        rwLock.writeLock().lock();
        try
        {
            this.config = config;
        }
        finally
        {
            rwLock.writeLock().unlock();
        }
    }
}
