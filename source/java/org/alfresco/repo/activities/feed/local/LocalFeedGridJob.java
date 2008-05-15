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

import java.io.Serializable;

import org.alfresco.repo.activities.feed.FeedGridJob;
import org.alfresco.repo.activities.feed.FeedTaskProcessor;
import org.alfresco.repo.activities.feed.JobSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation to execute local (ie. not grid) feed job
 */
public class LocalFeedGridJob implements FeedGridJob
{
    private static final Log logger = LogFactory.getLog(LocalFeedGridJob.class);
    
    private JobSettings arg;
    
    private FeedTaskProcessor feedTaskProcessor;
    
    public void setFeedTaskProcessor(FeedTaskProcessor feedTaskProcessor)
    {
        this.feedTaskProcessor = feedTaskProcessor;
    }
    
    public Serializable execute() throws Exception
    {
        JobSettings js = getArgument();
        
        if (logger.isDebugEnabled()) { logger.debug(">>> Execute: nodehash '" + js.getJobTaskNode() + "' from seq '" + js.getMinSeq() + "' to seq '" + js.getMaxSeq() + "' on this node"); }
        
        try 
        {
            feedTaskProcessor.process(js.getJobTaskNode(), js.getMinSeq(), js.getMaxSeq(), js.getWebScriptsCtx());
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new Exception(e.getMessage(), e.getCause());
        }

        // This job does not return any result.
        return null;
    }
    
    public void setArgument(JobSettings arg)
    {
        this.arg = arg;
    }
    
    public JobSettings getArgument()
    {
        return this.arg;
    }
}
