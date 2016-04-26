package org.alfresco.repo.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.CronTriggerBean;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.JobDetail;
import org.springframework.context.ApplicationContext;

/**
 * Integration tests for the {@link PropTablesCleanupJob} class.
 * 
 * @author Matt Ward
 */
public class PropTablesCleanupJobIntegrationTest
{
    private static ApplicationContext ctx;
    private CronTriggerBean jobTrigger;
    
    @BeforeClass
    public static void setUpClass()
    {
        ctx = ApplicationContextHelper.getApplicationContext();
    }
    
    @Before
    public void setUp() throws Exception
    {
        jobTrigger = ctx.getBean("propTablesCleanupTrigger", CronTriggerBean.class);
    }
    
    @Test
    public void checkJobDetails()
    {
        JobDetail jobDetail = jobTrigger.getJobDetail();
        assertEquals(PropTablesCleanupJob.class, jobDetail.getJobClass());
        assertTrue("JobDetail did not contain PropTablesCleaner reference",
                    jobDetail.getJobDataMap().get("propTablesCleaner") instanceof PropTablesCleaner);
    }
}
