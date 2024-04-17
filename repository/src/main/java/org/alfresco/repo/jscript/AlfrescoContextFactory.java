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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

/**
 * Custom factory that allows to apply configured limits during script executions
 * 
 * @see ContextFactory
 */
public class AlfrescoContextFactory extends ContextFactory
{
    private static final Log LOGGER = LogFactory.getLog(AlfrescoContextFactory.class);

    private int optimizationLevel = -1;
    private int maxScriptExecutionSeconds = -1;
    private int maxStackDepth = -1;
    private long maxMemoryUsedInBytes = -1L;
    private int observeInstructionCount = -1;

    private AlfrescoScriptThreadMxBeanWrapper threadMxBeanWrapper;

    private final int INTERPRETIVE_MODE = -1;

    @Override
    protected Context makeContext()
    {
        AlfrescoScriptContext context = new AlfrescoScriptContext();

        context.setOptimizationLevel(optimizationLevel);

        // Needed for both time and memory measurement
        if (maxScriptExecutionSeconds > 0 || maxMemoryUsedInBytes > 0L)
        {
            if (observeInstructionCount > 0)
            {
                LOGGER.info("Enabling observer count...");
                context.setGenerateObserverCount(true);
                context.setInstructionObserverThreshold(observeInstructionCount);
            }
            else
            {
                LOGGER.info("Disabling observer count...");
                context.setGenerateObserverCount(false);
            }
        }

        // Memory limit
        if (maxMemoryUsedInBytes > 0)
        {
            context.setThreadId(Thread.currentThread().getId());
        }

        // Max stack depth
        if (maxStackDepth > 0)
        {
            if (optimizationLevel != INTERPRETIVE_MODE)
            {
                LOGGER.warn("Changing optimization level from " + optimizationLevel + " to " + INTERPRETIVE_MODE);
            }
            // stack depth can only be set when no optimizations are applied
            context.setOptimizationLevel(INTERPRETIVE_MODE);
            context.setMaximumInterpreterStackDepth(maxStackDepth);
        }

        return context;
    }

    @Override
    protected void observeInstructionCount(Context cx, int instructionCount)
    {
        AlfrescoScriptContext acx = (AlfrescoScriptContext) cx;

        if (acx.isLimitsEnabled())
        {
            // Time limit
            if (maxScriptExecutionSeconds > 0)
            {
                long currentTime = System.currentTimeMillis();
                if (currentTime - acx.getStartTime() > maxScriptExecutionSeconds * 1000)
                {
                    throw new Error("Maximum script time of " + maxScriptExecutionSeconds + " seconds exceeded");
                }
            }

            // Memory
            if (maxMemoryUsedInBytes > 0 && threadMxBeanWrapper != null && threadMxBeanWrapper.isThreadAllocatedMemorySupported())
            {

                if (acx.getStartMemory() <= 0)
                {
                    acx.setStartMemory(threadMxBeanWrapper.getThreadAllocatedBytes(acx.getThreadId()));
                }
                else
                {
                    long currentAllocatedBytes = threadMxBeanWrapper.getThreadAllocatedBytes(acx.getThreadId());
                    if (currentAllocatedBytes - acx.getStartMemory() >= maxMemoryUsedInBytes)
                    {
                        throw new Error("Memory limit of " + maxMemoryUsedInBytes + " bytes reached");
                    }
                }
            }
        }
    }

    @Override
    protected Object doTopCall(Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args)
    {
        AlfrescoScriptContext acx = (AlfrescoScriptContext) cx;
        acx.setStartTime(System.currentTimeMillis());
        return super.doTopCall(callable, cx, scope, thisObj, args);
    }

    public int getOptimizationLevel()
    {
        return optimizationLevel;
    }

    public void setOptimizationLevel(int optimizationLevel)
    {
        this.optimizationLevel = optimizationLevel;
    }

    public int getMaxScriptExecutionSeconds()
    {
        return maxScriptExecutionSeconds;
    }

    public void setMaxScriptExecutionSeconds(int maxScriptExecutionSeconds)
    {
        this.maxScriptExecutionSeconds = maxScriptExecutionSeconds;
    }

    public int getMaxStackDepth()
    {
        return maxStackDepth;
    }

    public void setMaxStackDepth(int maxStackDepth)
    {
        this.maxStackDepth = maxStackDepth;
    }

    public long getMaxMemoryUsedInBytes()
    {
        return maxMemoryUsedInBytes;
    }

    public void setMaxMemoryUsedInBytes(long maxMemoryUsedInBytes)
    {
        this.maxMemoryUsedInBytes = maxMemoryUsedInBytes;
        if (maxMemoryUsedInBytes > 0)
        {
            this.threadMxBeanWrapper = new AlfrescoScriptThreadMxBeanWrapper();
            if (!threadMxBeanWrapper.isThreadAllocatedMemorySupported())
            {
                LOGGER.warn("com.sun.management.ThreadMXBean was not found on the classpath. "
                        + "This means that the limiting the memory usage for a script will NOT work.");
            }
        }
    }

    public int getObserveInstructionCount()
    {
        return observeInstructionCount;
    }

    public void setObserveInstructionCount(int observeInstructionCount)
    {
        this.observeInstructionCount = observeInstructionCount;
    }
}