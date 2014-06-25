package org.alfresco.repo.attributes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.repo.domain.propval.PropertyValueDAO;
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
    private @Mock PropertyValueDAO propValueDAO;
    private JobDetail jobDetail;
    
    @Before
    public void setUp() throws Exception
    {
        jobDetail = new JobDetail("propTablesCleanupJob", PropTablesCleanupJob.class);
        jobDetail.getJobDataMap().put(PropTablesCleanupJob.PROPERTY_VALUE_DAO_KEY, propValueDAO);
        cleanupJob = new PropTablesCleanupJob();
        
        when(jobCtx.getJobDetail()).thenReturn(jobDetail);
    }

    @Test
    public void testExecute() throws JobExecutionException
    {
        cleanupJob.execute(jobCtx);
        
        verify(propValueDAO).cleanupUnusedValues();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMissingPropertyValueDAO() throws JobExecutionException
    {
        jobDetail.getJobDataMap().put(PropTablesCleanupJob.PROPERTY_VALUE_DAO_KEY, null);
        cleanupJob.execute(jobCtx);
    }
    
    @Test(expected=ClassCastException.class)
    public void testWrongTypeForPropertyValueDAO() throws JobExecutionException
    {
        jobDetail.getJobDataMap().put(PropTablesCleanupJob.PROPERTY_VALUE_DAO_KEY, "This is not a PropertyValueDAO");
        cleanupJob.execute(jobCtx);
    }

}
