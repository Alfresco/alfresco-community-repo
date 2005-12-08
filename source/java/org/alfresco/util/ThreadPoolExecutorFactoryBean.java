/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
