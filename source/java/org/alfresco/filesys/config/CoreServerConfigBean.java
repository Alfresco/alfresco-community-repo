/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
