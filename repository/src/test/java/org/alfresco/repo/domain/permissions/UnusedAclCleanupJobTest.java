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
package org.alfresco.repo.domain.permissions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.alfresco.error.AlfrescoRuntimeException;

public class UnusedAclCleanupJobTest
{
    private final JobExecutionContext context = mock(JobExecutionContext.class);
    private final UnusedAclCleaner cleaner = mock(UnusedAclCleaner.class);
    private JobDetail jobDetail;

    @Before
    public void setUp()
    {
        jobDetail = JobBuilder.newJob(UnusedAclCleanupJob.class).withIdentity("unusedAclCleanupJob").build();
        jobDetail.getJobDataMap().put("unusedAclCleaner", cleaner);
        when(context.getJobDetail()).thenReturn(jobDetail);
    }

    @Test
    public void delegatesCleanup() throws JobExecutionException
    {
        new UnusedAclCleanupJob().executeJob(context);

        verify(cleaner).execute();
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void rejectsMissingCleaner() throws JobExecutionException
    {
        jobDetail.getJobDataMap().remove("unusedAclCleaner");
        new UnusedAclCleanupJob().executeJob(context);
    }
}