/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
