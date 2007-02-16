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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Factory for {@link java.util.concurrent.ThreadPoolExecutor} instances,
 * which cannot easily be constructed using constructor injection.
 * <p>
 * This factory provides the a singleton instance of the pool.
 * 
 * @author Derek Hulley
 */
public class ThreadPoolExecutorFactoryBean implements FactoryBean, InitializingBean
{
    private int corePoolSize;
    private int maximumPoolSize;
    private int keepAliveTime;
    private BlockingQueue<Runnable> workQueue;
    private ThreadPoolExecutor instance;
    
    /**
     * Constructor setting default properties:
     * <ul>
     *   <li>corePoolSize: 5</li>
     *   <li>maximumPoolSize: 20</li>
     *   <li>keepAliveTime: 60s</li>
     *   <li>workQueue: {@link ArrayBlockingQueue}</li>
     * </ul>
     */
    public ThreadPoolExecutorFactoryBean()
    {
        corePoolSize = 5;
        maximumPoolSize = 20;
        keepAliveTime = 30;
    }
    
    /**
     * The number of threads to keep in the pool, even if idle.
     * 
     * @param corePoolSize core thread count
     */
    public void setCorePoolSize(int corePoolSize)
    {
        this.corePoolSize = corePoolSize;
    }

    /**
     * The maximum number of threads to keep in the pool
     * 
     * @param maximumPoolSize the maximum number of threads in the pool
     */
    public void setMaximumPoolSize(int maximumPoolSize)
    {
        this.maximumPoolSize = maximumPoolSize;
    }

    /**
     * The time (in seconds) to keep non-core idle threads in the pool
     * 
     * @param keepAliveTime time to stay idle in seconds
     */
    public void setKeepAliveTime(int keepAliveTime)
    {
        this.keepAliveTime = keepAliveTime;
    }

    /**
     * The optional queue instance to use
     * 
     * @param workQueue optional queue implementation
     */
    public void setWorkQueue(BlockingQueue<Runnable> workQueue)
    {
        this.workQueue = workQueue;
    }

    public void afterPropertiesSet() throws Exception
    {
        if (workQueue == null)
        {
            workQueue = new ArrayBlockingQueue<Runnable>(corePoolSize);
        }
        // construct the instance
        instance = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
    }

    /**
     * @return Returns true always.
     */
    public boolean isSingleton()
    {
        return true;
    }

    /**
     * @return Returns the singleton {@link ThreadPoolExecutor instance}.
     */
    public Object getObject() throws Exception
    {
        if (instance == null)
        {
            throw new AlfrescoRuntimeException("The ThreadPoolExecutor instance has not been created");
        }
        return instance;
    }

    /**
     * @see ThreadPoolExecutor
     */
    public Class getObjectType()
    {
        return ThreadPoolExecutor.class;
    }
}
