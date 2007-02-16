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
package org.alfresco.util.perf;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An instance of this class keeps track of timings of method calls made against
 * a named entity.  Logging can occur either after each recorded time, or only on
 * VM shutdown or both.
 * <p>
 * Logging output is managed down to either the entity or entity-invocation level as follows:
 * <p>
 * <pre>
 *      performance.summary.method
 *      performance.summary.vm
 *          AND
 *      performance.targetEntityName
 *      performance.targetEntityName.methodName
 * </pre>
 * <p>
 * The following examples illustrate how it can be used:
 * <p>
 * <pre>
 *      performance.summary.method=DEBUG
 *      performance.myBean=DEBUG
 *          --> Output method invocation statistic on each call to myBean
 *          
 *      performance.summary.vm=DEBUG
 *      performance.myBean.doSomething=DEBUG
 *          --> Output summary for doSomething() invocations on myBean when VM terminates
 * 
 *      performance=DEBUG
 *          --> Output all performance data - after each invocation and upon VM closure          
 * </pre>
 * <p>
 * 
 * @author Derek Hulley
 */
public abstract class AbstractPerformanceMonitor
{
    /** logger for method level performance summaries */
    private static final Log methodSummaryLogger = LogFactory.getLog("performance.summary.method");
    /** logger for VM level performance summaries */
    private static final Log vmSummaryLogger = LogFactory.getLog("performance.summary.vm");
    
    private final String entityName;
    /** VM level summary */
    private SortedMap<String, MethodStats> stats;

    /**
     * Convenience method to check if there is some sort of performance logging enabled
     * 
     * @return Returns true if there is some sort of performance logging enabled, false otherwise
     */
    public static boolean isDebugEnabled()
    {
        return (vmSummaryLogger.isDebugEnabled() || methodSummaryLogger.isDebugEnabled());
    }
    
    /**
     * @param entityName the name of the entity for which the performance is being recorded 
     */
    public AbstractPerformanceMonitor(String entityName)
    {
        this.entityName = entityName;
        stats = new TreeMap<String, MethodStats>();
        
        // enable a VM shutdown hook if required
        if (vmSummaryLogger.isDebugEnabled())
        {
            Thread hook = new ShutdownThread();
            Runtime.getRuntime().addShutdownHook(hook);
        }
    }
    
    /**
     * Dumps the results of the method execution to:
     * <ul>
     *   <li>DEBUG output if the method level debug logging is active</li>
     *   <li>Performance store if required</li>
     * </ul>
     * 
     * @param methodName the name of the method against which to store the results
     * @param delayMs
     */
    protected void recordStats(String methodName, double delayMs)
    {
        Log methodLogger = LogFactory.getLog("performance." + entityName + "." + methodName);
        if (!methodLogger.isDebugEnabled())
        {
            // no recording for this method
            return;
        }

        DecimalFormat format = new DecimalFormat ();
        format.setMinimumFractionDigits (3);
        format.setMaximumFractionDigits (3);

        // must we log on a per-method call?
        if (methodSummaryLogger.isDebugEnabled())
        {
            methodLogger.debug("Executed " + entityName + "#" + methodName + " in " + format.format(delayMs) + "ms");
        }
        if (vmSummaryLogger.isDebugEnabled())
        {
            synchronized(this)  // only synchronize if absolutely necessary
            {
                // get stats
                MethodStats methodStats = stats.get(methodName);
                if (methodStats == null)
                {
                    methodStats = new MethodStats();
                    stats.put(methodName, methodStats);
                }
                methodStats.record(delayMs);
            }
        }
    }
    
    /**
     * Stores the execution count and total execution time for any method 
     */
    private class MethodStats
    {
        private int count;
        private double totalTimeMs;
        
        /**
         * Records the time for a method to execute and bumps up the execution count
         * 
         * @param delayMs the time the method took to execute in milliseconds
         */
        public void record(double delayMs)
        {
           count++;
           totalTimeMs += delayMs;
        }
        
        public String toString()
        {
            DecimalFormat format = new DecimalFormat ();
            format.setMinimumFractionDigits (3);
            format.setMaximumFractionDigits (3);
            double averageMs = totalTimeMs / (long) count;
            return ("Executed " + count + " times, averaging " + format.format(averageMs) + "ms per call");
        }
    }
    
    /**
     * Dumps the output of all recorded method statistics
     */
    private class ShutdownThread extends Thread
    {
        public void run()
        {
            String beanName = AbstractPerformanceMonitor.this.entityName;
            
            // prevent multiple ShutdownThread instances interleaving their output
            synchronized(ShutdownThread.class)
            {
                vmSummaryLogger.debug("\n==================== " + beanName.toUpperCase() + " ===================");
                Set<String> methodNames = stats.keySet();
                for (String methodName : methodNames)
                {
                    vmSummaryLogger.debug("\nMethod performance summary: \n" +
                            "   Bean: " + AbstractPerformanceMonitor.this.entityName + "\n" +
                            "   Method: " + methodName + "\n" +
                            "   Statistics: " + stats.get(methodName));
                }
            }
        }
    }
}
