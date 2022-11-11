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

import org.mozilla.javascript.Context;

/**
 * Custom Rhino context that holds data as start time and memory
 * 
 * @see Context
 */
public class AlfrescoScriptContext extends Context
{
    private long startTime;
    private long threadId;
    private long startMemory;
    private boolean limitsEnabled = false;

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public long getThreadId()
    {
        return threadId;
    }

    public void setThreadId(long threadId)
    {
        this.threadId = threadId;
    }

    public long getStartMemory()
    {
        return startMemory;
    }

    public void setStartMemory(long startMemory)
    {
        this.startMemory = startMemory;
    }

    public boolean isLimitsEnabled()
    {
        return limitsEnabled;
    }

    public void setLimitsEnabled(boolean limitsEnabled)
    {
        this.limitsEnabled = limitsEnabled;
    }
}