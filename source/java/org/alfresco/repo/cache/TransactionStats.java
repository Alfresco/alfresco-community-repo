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
