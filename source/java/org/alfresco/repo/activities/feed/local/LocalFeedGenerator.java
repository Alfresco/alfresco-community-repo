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
package org.alfresco.repo.activities.feed.local;

import java.util.Collection;

import org.alfresco.repo.activities.feed.AbstractFeedGenerator;
import org.alfresco.repo.activities.feed.FeedGridJob;
import org.alfresco.repo.activities.feed.FeedTaskProcessor;
import org.alfresco.repo.activities.feed.JobSettings;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The local (ie. not grid) feed generator component is responsible for generating feed entries
 */
public class LocalFeedGenerator extends AbstractFeedGenerator
{
    private static Log logger = LogFactory.getLog(LocalFeedGenerator.class);
   
    private FeedTaskProcessor feedTaskProcessor;
    
    public void setFeedTaskProcessor(FeedTaskProcessor feedTaskProcessor)
    {
        this.feedTaskProcessor = feedTaskProcessor;
    }
    
    @Override
    public int getEstimatedGridSize()
    {
        return 1;
    }
    
    public void init() throws Exception
    {
       super.init();
    }

    protected boolean generate() throws Exception
    {
        Long maxSequence = getPostDaoService().getMaxActivitySeq();
        Integer maxNodeHash = getPostDaoService().getMaxNodeHash();
        
        String gridName = "local";
        
        if ((maxSequence != null) && (maxNodeHash != null))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(">>> Execute job cycle: " + gridName + " (maxSeq: " + maxSequence + ")");
            }
            
            long startTime = System.currentTimeMillis();
            
            // TODO ... or push this upto to job scheduler ... ?
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork()
                {
                    getWebScriptsCtx().setTicket(getAuthenticationService().getCurrentTicket());
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName()); // need web scripts to support System-level authentication ... see RepositoryContainer !
            
            JobSettings js = new JobSettings();
            js.setMaxSeq(maxSequence);
            js.setJobTaskNode(maxNodeHash);
            js.setWebScriptsCtx(getWebScriptsCtx());
            js.setMaxItemsPerCycle(getMaxItemsPerCycle());
            
            LocalFeedTaskSplitter splitter = new LocalFeedTaskSplitter();
            splitter.setFeedTaskProcessor(feedTaskProcessor);
            
            Collection<FeedGridJob> jobs = splitter.split(getEstimatedGridSize(), js);
            
            for (FeedGridJob job : jobs)
            {
                job.execute();
            }
            
            if (logger.isDebugEnabled())
            {
                logger.debug(">>> Finish job cycle: " + gridName + " (in " + (System.currentTimeMillis() - startTime) + " msecs)");
            }
            return true;
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(">>> No work to be done for this job cycle: " + gridName);
            }
            return false;
        }
    }
}
