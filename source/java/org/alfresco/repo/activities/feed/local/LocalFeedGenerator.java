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

import java.util.Collection;
import java.util.Date;

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
        
        if (maxSequence != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(">>> Execute job cycle: " + gridName + " (maxSeq: " + maxSequence + ")");
            }
   
            long startTime = new Date().getTime();
            
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
            
            long endTime = new Date().getTime();
            
            if (logger.isDebugEnabled())
            {
                logger.debug(">>> Finish job cycle: " + gridName + " (time taken (secs) = " + ((endTime - startTime) / 1000) + ")");
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
