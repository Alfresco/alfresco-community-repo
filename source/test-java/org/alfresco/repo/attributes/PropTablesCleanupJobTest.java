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
