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
package org.alfresco.repo.activities.feed.local;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.activities.feed.FeedGridJob;
import org.alfresco.repo.activities.feed.FeedTaskProcessor;
import org.alfresco.repo.activities.feed.JobSettings;

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

        if (logger.isDebugEnabled())
        {
            logger.debug(">>> Execute: nodehash '" + js.getJobTaskNode() + "' from seq '" + js.getMinSeq() + "' to seq '" + js.getMaxSeq() + "' on this node");
        }

        feedTaskProcessor.process(js.getJobTaskNode(), js.getMinSeq(), js.getMaxSeq(), js.getWebScriptsCtx());

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
