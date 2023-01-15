/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.jscript;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Allows to monitor memory usage
 */
public class AlfrescoScriptThreadMxBeanWrapper
{

    private ThreadMXBean threadMXBean = null;
    private boolean threadAllocatedMemorySupported = false;

    private final String THREAD_MX_BEAN_SUN = "com.sun.management.ThreadMXBean";

    public AlfrescoScriptThreadMxBeanWrapper()
    {
        checkThreadAllocatedMemory();
    }

    public long getThreadAllocatedBytes(long threadId)
    {
        if (threadMXBean != null && threadAllocatedMemorySupported)
        {
            return ((com.sun.management.ThreadMXBean) threadMXBean).getThreadAllocatedBytes(threadId);
        }

        return -1;
    }

    private void checkThreadAllocatedMemory()
    {
        try
        {
            Class<?> clazz = Class.forName(THREAD_MX_BEAN_SUN);
            if (clazz != null)
            {
                this.threadAllocatedMemorySupported = true;
                this.threadMXBean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
            }
        }
        catch (Exception e)
        {
            this.threadAllocatedMemorySupported = false;
        }
    }

    public boolean isThreadAllocatedMemorySupported()
    {
        return threadAllocatedMemorySupported;
    }
}