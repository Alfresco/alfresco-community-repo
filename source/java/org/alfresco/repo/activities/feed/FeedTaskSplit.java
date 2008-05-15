/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
