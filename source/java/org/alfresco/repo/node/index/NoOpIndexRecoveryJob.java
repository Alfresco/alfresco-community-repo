/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.node.index;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A do-nothing implementation of the {@link Job} interface. This behaviour is overriden
 * in the enterprise edition when clustering is enabled. 
 * 
 * @author Matt Ward
 */
public class NoOpIndexRecoveryJob implements Job
{
    private static final Log log = LogFactory.getLog(NoOpIndexRecoveryJob.class);
    
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        if (log.isDebugEnabled())
        {
            log.debug("Skipping reindexing.");
        }
        // NOOP
    }
}
