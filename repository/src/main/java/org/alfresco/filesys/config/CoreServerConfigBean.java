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
package org.alfresco.filesys.config;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class CoreServerConfigBean.
 * 
 * @author dward
 */
public class CoreServerConfigBean
{
    /** The thread pool init. */
    private Integer threadPoolInit;

    /** The thread pool max. */
    private Integer threadPoolMax;

    /** The thread pool debug. */
    private boolean threadPoolDebug;

    /** The memory packet sizes. */
    private List<MemoryPacketConfigBean> memoryPacketSizes;

    /**
     * Gets the thread pool init.
     * 
     * @return the thread pool init
     */
    public Integer getThreadPoolInit()
    {
        return threadPoolInit;
    }

    /**
     * Sets the thread pool init.
     * 
     * @param threadPoolInit
     *            the new thread pool init
     */
    public void setThreadPoolInit(Integer threadPoolInit)
    {
        this.threadPoolInit = threadPoolInit;
    }

    /**
     * Gets the thread pool max.
     * 
     * @return the thread pool max
     */
    public Integer getThreadPoolMax()
    {
        return threadPoolMax;
    }

    /**
     * Sets the thread pool max.
     * 
     * @param threadPoolMax
     *            the new thread pool max
     */
    public void setThreadPoolMax(Integer threadPoolMax)
    {
        this.threadPoolMax = threadPoolMax;
    }

    /**
     * Checks if is thread pool debug.
     * 
     * @return true, if is thread pool debug
     */
    public boolean getThreadPoolDebug()
    {
        return threadPoolDebug;
    }

    /**
     * Sets the thread pool debug.
     * 
     * @param threadPoolDebug
     *            the new thread pool debug
     */
    public void setThreadPoolDebug(boolean threadPoolDebug)
    {
        this.threadPoolDebug = threadPoolDebug;
    }

    /**
     * Gets the memory packet sizes.
     * 
     * @return the memory packet sizes
     */
    public List<MemoryPacketConfigBean> getMemoryPacketSizes()
    {
        return memoryPacketSizes;
    }

    /**
     * Sets the memory packet sizes.
     * 
     * @param memoryPacketSizes
     *            the new memory packet sizes
     */
    public void setMemoryPacketSizes(List<MemoryPacketConfigBean> memoryPacketSizes)
    {
        this.memoryPacketSizes = memoryPacketSizes;
    }
}
