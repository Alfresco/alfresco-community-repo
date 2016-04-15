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
package org.alfresco.repo.attributes;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.error.AlfrescoRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * Tests for the {@link PropTablesCleanupJob} class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class PropTablesCleanupJobTest
{
    private PropTablesCleanupJob cleanupJob;
    private @Mock JobExecutionContext jobCtx;
    private @Mock PropTablesCleaner propTablesCleaner;
    private JobDetail jobDetail;
    
    @Before
    public void setUp() throws Exception
    {
        jobDetail = new JobDetail("propTablesCleanupJob", PropTablesCleanupJob.class);
        jobDetail.getJobDataMap().put("propTablesCleaner", propTablesCleaner);
        cleanupJob = new PropTablesCleanupJob();
        
        when(jobCtx.getJobDetail()).thenReturn(jobDetail);
    }

    @Test
    public void testExecute() throws JobExecutionException
    {
        cleanupJob.execute(jobCtx);
        
        verify(propTablesCleaner).execute();
    }
    
    @Test(expected=AlfrescoRuntimeException.class)
    public void testMissingPropTablesCleaner() throws JobExecutionException
    {
        jobDetail.getJobDataMap().put("propTablesCleaner", null);
        cleanupJob.execute(jobCtx);
    }
    
    @Test(expected=AlfrescoRuntimeException.class)
    public void testWrongTypeForPropTablesCleaner() throws JobExecutionException
    {
        jobDetail.getJobDataMap().put("propTablesCleaner", "This is not a PropTablesCleaner");
        cleanupJob.execute(jobCtx);
    }

}
