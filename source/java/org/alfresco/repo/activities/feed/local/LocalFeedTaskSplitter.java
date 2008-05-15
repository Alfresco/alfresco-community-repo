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
package org.alfresco.repo.activities.feed.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.activities.feed.FeedGridJob;
import org.alfresco.repo.activities.feed.FeedTaskProcessor;
import org.alfresco.repo.activities.feed.FeedTaskSplit;
import org.alfresco.repo.activities.feed.JobSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The local feed task splitter is responsible for splitting the feed task into feed jobs
 */
public class LocalFeedTaskSplitter
{
    private static final Log logger = LogFactory.getLog(LocalFeedTaskSplitter.class);
    
    private FeedTaskProcessor feedTaskProcessor;
    
    public void setFeedTaskProcessor(FeedTaskProcessor feedTaskProcessor)
    {
        this.feedTaskProcessor = feedTaskProcessor;
    }
    
    public Collection<FeedGridJob> split(int gridSize, Object o) throws Exception
    {
        try
        {
            FeedTaskSplit feedSplitter = new FeedTaskSplit();
            Collection<JobSettings> jobs = feedSplitter.split(gridSize, (JobSettings)o);
            
            List<FeedGridJob> gridJobs = new ArrayList<FeedGridJob>(jobs.size());
            for (JobSettings job : jobs)
            {
                LocalFeedGridJob gridJob = new LocalFeedGridJob();
                gridJob.setFeedTaskProcessor(feedTaskProcessor);
                gridJob.setArgument(job);
                gridJobs.add(gridJob);
            }
            return gridJobs;
            //return (Collection<FeedGridJob>)feedSplitter.split(gridSize, (JobSettings)o, new LocalFeedGridJob());
        }
        catch (Exception e)
        {
            logger.equals(e);
            throw new Exception(e.getMessage());
        }
    }
}
