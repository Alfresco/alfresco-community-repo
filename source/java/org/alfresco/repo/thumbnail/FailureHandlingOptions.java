/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.thumbnail;

import org.alfresco.repo.thumbnail.conditions.NodeEligibleForRethumbnailingEvaluator;

/**
 * This class holds failure-related configuration data for a {@link ThumbnailDefinition}.
 * <p/>
 * Note that a failed thumbnail creation is not the same as a creation which was not attempted for lack of transformers.
 * 
 * @author Neil Mc Erlean
 * @since 3.5.0
 * @see NodeEligibleForRethumbnailingEvaluator for a description of how these configuration parameters are used.
 */
public class FailureHandlingOptions
{
    public static final int DEFAULT_PERIOD = 0;
    public static final int DEFAULT_RETRY_COUNT = 2;
    public static final boolean DEFAULT_QUIET_PERIOD_RETRIES_ENABLED = true;
    
    /**
     * The minimum amount of time (in seconds) before a 'difficult' piece of content should be reattempted.
     */
    private long quietPeriod = DEFAULT_PERIOD;
    
    /**
     * The minimum amount of time (in seconds) before a {@link ThumbnailDefinition} should be initially reattempted.
     */
    private long retryPeriod = DEFAULT_PERIOD;
    
    /**
     * The maximum number of times to try to thumbnail a normal piece of content.
     */
    private int retryCount = DEFAULT_RETRY_COUNT;
    
    /**
     * Are thumbnail retries enabled for difficult content?
     */
    private boolean quietPeriodRetriesEnabled = DEFAULT_QUIET_PERIOD_RETRIES_ENABLED;
    
    public int getRetryCount()
    {
        return retryCount;
    }

    public void setRetryCount(int retryCount)
    {
        this.retryCount = retryCount;
    }

    public boolean getQuietPeriodRetriesEnabled()
    {
        return quietPeriodRetriesEnabled;
    }
    
    public void setQuietPeriodRetriesEnabled(boolean quietPeriodRetriesEnabled)
    {
        this.quietPeriodRetriesEnabled = quietPeriodRetriesEnabled;
    }
    
    /**
     * Sets the initial minimum retry period for thumbnail creation/update.
     * @param initialMinimumRetryPeriod minimum retry period in ms.
     */
    public void setRetryPeriod(long retryPeriod)
    {
        this.retryPeriod = retryPeriod;
    }
    
    public long getRetryPeriod()
    {
        return this.retryPeriod;
    }

    /**
     * Sets the minimum retry period for thumbnail creation/update.
     * @param minimumRetryPeriodLong minimum retry period in ms.
     */
    public void setQuietPeriod(long quietPeriod)
    {
        this.quietPeriod = quietPeriod;
    }
    
    public long getQuietPeriod()
    {
        return this.quietPeriod;
    }
}
