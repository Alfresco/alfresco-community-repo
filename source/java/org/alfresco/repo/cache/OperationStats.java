package org.alfresco.repo.cache;


/**
 * Represents a single cache operation type's statistics.
 * For example, the cummalative time spent performing a cache's remove operation
 * and the number of times that the remove was performed.
 * <p>
 * Instances are immutable.
 */
public final class OperationStats
{
    /** Total time spent in operations of this type */
    private final double totalTime;
    /** Count of how many instances of this operation occurred. */
    private final long count;
    
    public OperationStats(double totalTime, long count)
    {
        this.totalTime = totalTime;
        this.count = count;
    }
    
    public OperationStats(OperationStats source, double totalTime, long count)
    {
        if (Double.compare(source.totalTime, Double.NaN) == 0)
        {
            // No previous time to add new time to.
            this.totalTime = totalTime;
        }
        else
        {
            this.totalTime = source.totalTime + totalTime;
        }
        this.count = source.count + count;
    }
    
    public double meanTime()
    {
        return totalTime / count;
    }

    public double getTotalTime()
    {
        return this.totalTime;
    }

    public long getCount()
    {
        return this.count;
    }
}
