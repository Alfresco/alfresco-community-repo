/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import static org.alfresco.repo.content.transform.TransformerConfig.ANY;

import org.alfresco.service.cmr.repository.MimetypeService;


/**
 * Implementation of a {@link TransformerStatistics}.
 * 
 * @author Alan Davis
 */
public class TransformerStatisticsImpl implements TransformerStatistics
{
    private final MimetypeService mimetypeService;
    private final String sourceMimetype;
    private final String targetMimetype;
    private final ContentTransformer transformer;
    private final TransformerStatistics parent;
    private final long errorTime;
    
    private double averageTime;
    private long count = 0L;
    private long errorCount = 0L;
    
    public TransformerStatisticsImpl(MimetypeService mimetypeService, String sourceMimetype, String targetMimetype,
            ContentTransformer transformer, TransformerStatistics parent, long errorTime,
            long initialAverageTime, long initialCount)
    {
        this.mimetypeService = mimetypeService;
        this.sourceMimetype = sourceMimetype;
        this.targetMimetype = targetMimetype;
        this.transformer = transformer;
        this.parent = parent;
        this.errorTime = errorTime;
        
        averageTime = initialAverageTime;
        count = initialCount;
    }

    @Override
    public String getSourceExt()
    {
        return ANY.equals(sourceMimetype) ? ANY : mimetypeService.getExtension(sourceMimetype);
    }

    @Override
    public String getTargetExt()
    {
        return ANY.equals(targetMimetype) ? ANY : mimetypeService.getExtension(targetMimetype);
    }

    @Override
    public String getTransformerName()
    {
        return transformer == null ? TransformerConfig.SUMMARY_TRANSFORMER_NAME : transformer.getName();
    }

    @Override
    public synchronized void recordTime(long transformationTime)
    {
        if (count == Long.MAX_VALUE)
        {
            // we have reached the max count - reduce it by half
            // the average fluctuation won't be extreme
            count /= 2L;
        }
        // adjust the average
        count++;
        double diffTime = ((double) transformationTime) - averageTime;
        averageTime += diffTime / (double) count;
        
        if (parent != null)
        {
            parent.recordTime(transformationTime);
        }
    }

    @Override
    public synchronized void recordError(long transformationTime)
    {
        if (errorCount < Long.MAX_VALUE)
        {
            errorCount++;
        }

        // Error time is only recorded for transformer, source and target combinations
        recordTime((parent == null || transformer == null || errorTime <= 0 ? transformationTime : errorTime));

        if (parent != null)
        {
            parent.recordError(transformationTime);
        }
    }
    
    @Override
    public long getCount()
    {
        return count;
    }

    @Override
    public void setCount(long count)
    {
        this.count = count;
    }

    @Override
    public long getErrorCount()
    {
        return errorCount;
    }

    @Override
    public void setErrorCount(long errorCount)
    {
        this.errorCount = errorCount;
    }

    @Override
    public long getAverageTime()
    {
        return (long)averageTime;
    }

    @Override
    public void setAverageTime(long averageTime)
    {
        this.averageTime = (double)averageTime;
    }

    public boolean isSummary()
    {
        return TransformerConfig.ANY.equals(sourceMimetype) && TransformerConfig.ANY.equals(targetMimetype);
    }
}
