/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.attributes;

import static org.junit.Assert.*;

import java.util.Date;

import org.alfresco.repo.domain.propval.PropertyValueDAO;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.CronTriggerBean;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;

import com.ibm.icu.util.Calendar;

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
    public void checkJobWillNeverRunByDefault() throws Exception
    {
        Date fireTime = jobTrigger.getTrigger().getFireTimeAfter(new Date());
        Calendar calendar = Calendar.getInstance();
        
        // Far into the future, we count this as never.
        calendar.setTime(fireTime);
        assertEquals(2099, calendar.get(Calendar.YEAR));
        
    }
    
    @Test
    public void checkJobDetails()
    {
        JobDetail jobDetail = jobTrigger.getJobDetail();
        assertEquals(PropTablesCleanupJob.class, jobDetail.getJobClass());
        assertTrue("JobDetail did not contain PropertyValueDAO reference",
                    jobDetail.getJobDataMap().get("propertyValueDAO") instanceof PropertyValueDAO);
    }
}
