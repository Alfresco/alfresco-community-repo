package org.alfresco.repo.content.caching.quota;

/**
 * QuotaManagerStrategy that doesn't enforce any quota limits whatsoever.
 * 
 * @author Matt Ward
 */
public class UnlimitedQuotaStrategy implements QuotaManagerStrategy
{

    @Override
    public boolean beforeWritingCacheFile(long contentSize)
    {
        // Always write cache files.
        return true;
    }

    @Override
    public boolean afterWritingCacheFile(long contentSize)
    {
        // Always allow cache files to remain.
        return true;
    }

}
