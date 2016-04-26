package org.alfresco.repo.content.caching.quota;

/**
 * Interface through which disk usage levels can be set and queried.
 * 
 * @author Matt Ward
 */
public interface UsageTracker
{
    long getCurrentUsageBytes();
    void setCurrentUsageBytes(long newDiskUsage);
    long addUsageBytes(long sizeDelta);
}
