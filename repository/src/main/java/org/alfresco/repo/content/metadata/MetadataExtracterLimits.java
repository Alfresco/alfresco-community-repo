/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.content.metadata;

import org.alfresco.api.AlfrescoPublicApi;    

/**
 * Represents maximum values (that result in exceptions if exceeded) or
 * limits on values (that result in EOF (End Of File) being returned early).
 * The current options are elapsed time, document size and concurrent extractions limit.
 * 
 * @author Ray Gauss II
 */
@AlfrescoPublicApi
public class MetadataExtracterLimits
{
    private long timeoutMs = Long.MAX_VALUE;
    private double maxDocumentSizeMB = Double.MAX_VALUE;
    private int maxConcurrentExtractionsCount = Integer.MAX_VALUE;
    
    /**
     * Gets the time in milliseconds after which the metadata extracter will be stopped.
     * 
     * @return the timeout
     */
    public long getTimeoutMs()
    {
        return timeoutMs;
    }

    /**
     * Sets the time in milliseconds after which the metadata extracter will be stopped.
     * 
     * @param timeoutMs the timeout
     */
    public void setTimeoutMs(long timeoutMs)
    {
        this.timeoutMs = timeoutMs;
    }
    /**
     * Gets the maximum size(MB) allowed for a transformation
     * 
     * @return maximum size
     */
    public double getMaxDocumentSizeMB()
    {
        return maxDocumentSizeMB;
    }

    /**
     * Sets the maximum size(MB) allowed for a transformation
     * 
     * @param maxDocumentSizeMB
     */
    public void setMaxDocumentSizeMB(double maxDocumentSizeMB)
    {
        this.maxDocumentSizeMB = maxDocumentSizeMB;
    }

    /**
     * Sets the maximum number of allowed concurrent extractions
     * 
     * @param maxConcurrentExtractionsCount
     */
    public void setMaxConcurrentExtractionsCount(int maxConcurrentExtractionsCount)
    {
        this.maxConcurrentExtractionsCount = maxConcurrentExtractionsCount;
    }

    /**
     * Gets the maximum count of allowed concurrent extractions
     * 
     * @return maximum count
     */
    public int getMaxConcurrentExtractionsCount()
    {
        return maxConcurrentExtractionsCount;
    }
}
