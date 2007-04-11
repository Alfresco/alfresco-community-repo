/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.util.resource;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * A controller of system or in-transaction resources.  Given a few statistics
 * regarding a method's call history, and using whatever other measurements
 * are needed, implementations will decide whether and how to clear up
 * sufficient system resources.
 * 
 * @author Derek Hulley
 */
public interface MethodResourceManager
{
    /**
     * Helper class to carry basic method call statistics.
     */
    public static class MethodStatistics
    {
        private long callCount;
        private long accumulatedTimeNs;
        public void accumulateNs(long durationNs)
        {
            accumulatedTimeNs += durationNs;
            callCount++;
        }
        public long getAccumulatedTimeNs()
        {
            return accumulatedTimeNs;
        }
        public long getCallCount()
        {
            return callCount;
        }
        /**
         * @return  Returns the average call time in nanoseconds
         */
        public double getAverageCallTimeNs()
        {
            if (callCount == 0)
            {
                return 0.0D;
            }
            return (double) accumulatedTimeNs / (double) callCount;
        }
    }
    
    /**
     * Check and free any required resources for an imminent.  Details of the
     * current transaction and some gathered information about previous calls
     * to associated methods is also provided.
     * 
     * @param methodStatsByMethod       all known methods and their basic call stats
     * @param transactionElapsedTimeNs  the elapsed time in the current transaction
     * @param currentMethod             the method about to be called
     */
    public void manageResources(
            Map<Method, MethodStatistics> methodStatsByMethod,
            long transactionElapsedTimeNs,
            Method currentMethod);
}
