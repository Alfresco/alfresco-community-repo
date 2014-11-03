/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.cache;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * Only to be used within a single transaction/thread.
 * 
 * @since 5.0
 * @author Matt Ward
 */
public class TransactionStats
{
    private Map<OpType, SummaryStatistics> timings = new HashMap<>();
    
    /**
     * Cache operation type.
     */
    public enum OpType
    {
        GET_HIT,
        GET_MISS,
        PUT,
        REMOVE,
        CLEAR
    }
    
    public long getCount(OpType op)
    {
        SummaryStatistics stats = getTimings(op);
        return stats.getN();
    }
    
    public SummaryStatistics getTimings(OpType op)
    {
        SummaryStatistics opTimings = timings.get(op);
        if (opTimings == null)
        {
            opTimings = new SummaryStatistics();
            timings.put(op, opTimings);
        }
        return opTimings;
    }
    
    public void record(long start, long end, OpType op)
    {   
        if (end < start)
        {
            throw new IllegalArgumentException("End time [" + end + "] occurs before start time [" + start + "].");
        }
        double timeTaken = end - start;
        addTiming(op, timeTaken);
    }

    private void addTiming(OpType op, double time)
    {
        SummaryStatistics opTimings = getTimings(op);
        opTimings.addValue(time);
    }
}
