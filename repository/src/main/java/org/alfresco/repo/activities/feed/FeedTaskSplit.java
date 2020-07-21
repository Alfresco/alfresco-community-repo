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
package org.alfresco.repo.activities.feed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Responsible for splitting the feed task into feed jobs (to be executed locally or on a grid)
 */
public class FeedTaskSplit
{
    private static Log logger = LogFactory.getLog(FeedTaskSplit.class);
    
    public Collection<JobSettings> split(int gridSize, JobSettings splitSettings)
    {
        long maxSequence = splitSettings.getMaxSeq();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("split: start - gridSize = " + gridSize + ", maxSequence = " + maxSequence);
        }
        
        long minSequence = maxSequence - splitSettings.getMaxItemsPerCycle() + 1;
        
        splitSettings.setMinSeq((minSequence >= 0L ? minSequence : 0L));
            
        List<JobSettings> jobs = new ArrayList<JobSettings>(gridSize);
        
        int maxNodeHash = splitSettings.getJobTaskNode();
        
        // note: gridSize may change between runs, hence use maximum node hash/bucket for this cycle
        for (int n = 1; n <= maxNodeHash; n++)
        {
            // every job gets its own copy of the jobSettings (with different nodeHash) as an argument.
            JobSettings jobSettings = splitSettings.clone();
            jobSettings.setJobTaskNode(n);
            
            jobs.add(jobSettings);
        }

        return jobs;
    }
}
