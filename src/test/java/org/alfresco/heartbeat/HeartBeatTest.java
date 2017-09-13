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
package org.alfresco.heartbeat;

import org.alfresco.heartbeat.datasender.HBDataSenderService;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.service.license.LicenseService;
import org.junit.Before;
import org.junit.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.util.Arrays;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author eknizat
 */
public class HeartBeatTest
{

    private static final String[] CONFIG_LOCATIONS = new String[] {
            "classpath:alfresco/scheduler-core-context.xml",
            "classpath:org/alfresco/heartbeat/test-heartbeat-context.xml"};
    private ApplicationContext context;

    LicenseService mockLicenseService;
    HBDataSenderService mockDataSenderService;
    HBDataCollectorService mockDataCollectorService;

    @Before
    public void setUp()
    {
        // New context with scheduler
        context = new ClassPathXmlApplicationContext(CONFIG_LOCATIONS);

        // Add services to context
        mockLicenseService = mock(LicenseService.class);
        mockDataCollectorService = mock(HBDataCollectorService.class);
        ((ConfigurableApplicationContext) context).getBeanFactory().registerSingleton("licenseService",mockLicenseService);
        ((ConfigurableApplicationContext) context).getBeanFactory().registerSingleton("hbDataCollectorService",mockDataCollectorService);

        mockDataSenderService = mock(HBDataSenderService.class);
    }

    @Test
    public void testHBRegistersWithLicenceService()
    {
        HeartBeat heartbeat = new HeartBeat(context,false);

        // Check that HearBeat registers itself with the licence service
        verify(mockLicenseService).registerOnLicenseChange(heartbeat);
    }

    @Test
    public void testJobSchedulingWhenEnabled()
    {
        // Enable heartbeat in data collector service ( as if set in prop file)
        when(mockDataCollectorService.isEnabledByDefault()).thenReturn(true);

        HeartBeat heartbeat = new HeartBeat(context,true);

        // Check that the job is scheduled when heartbeat is enabled
        assertTrue("Job was not scheduled but HB is enabled", isJobScheduled());
    }

    @Test
    public void testJobSchedulingWhenDisabled()
    {
        // Disable heartbeat in data collector service ( as if set in prop file)
        when(mockDataCollectorService.isEnabledByDefault()).thenReturn(false);

        HeartBeat heartbeat = new HeartBeat(context,true);

        // Check that the job is not scheduled when heartbeat is disabled
        assertFalse("Job was scheduled but HB is disabled", isJobScheduled());
    }

    /**
     * Heartbeat enabled by default but disabled in licence on onLicenseChange
     */
    @Test
    public void testOnLicenseChangeOverridesDefaultEnabled()
    {
        // Enable heartbeat in data collector service ( as if set in prop file)
        when(mockDataCollectorService.isEnabledByDefault()).thenReturn(true);

        HeartBeat heartbeat = new HeartBeat(context,true);

        // heartbeat disabled in licence
        LicenseDescriptor mockLicenseDescriptor =  mock(LicenseDescriptor.class);
        when(mockLicenseDescriptor.isHeartBeatDisabled()).thenReturn(true);

        assertTrue(heartbeat.isEnabled());
        assertTrue("Job should be scheduled at this point.",isJobScheduled());

        heartbeat.onLicenseChange(mockLicenseDescriptor);

        // Check heartbeat is disabled and job unscheduled
        assertFalse(heartbeat.isEnabled());
        assertFalse("Job should be unscheduled.",isJobScheduled());
    }

    /**
     * heartbeat disabled by default but enabled in licence
     */
    @Test
    public void testOnLicenseChangeOverridesDefaultDisabled()
    {
        // Disable heartbeat in data collector service ( as if set in prop file)
        when(mockDataCollectorService.isEnabledByDefault()).thenReturn(false);

        HeartBeat heartbeat = new HeartBeat(context,true);

        // heartbeat enabled in licence
        LicenseDescriptor mockLicenseDescriptor =  mock(LicenseDescriptor.class);
        when(mockLicenseDescriptor.isHeartBeatDisabled()).thenReturn(false);

        assertFalse(heartbeat.isEnabled());
        assertFalse("Job should not be scheduled at this point.",isJobScheduled());

        heartbeat.onLicenseChange(mockLicenseDescriptor);

        // Check heartbeat is disabled and job unscheduled
        assertTrue(heartbeat.isEnabled());
        assertTrue("Job should be scheduled.",isJobScheduled());
    }

    @Test
    public void testOnLicenceFailRevertsToEnabled()
    {
        // Enable heartbeat in data collector service ( as if set in prop file)
        when(mockDataCollectorService.isEnabledByDefault()).thenReturn(true);

        HeartBeat heartbeat = new HeartBeat(context,true);

        // heartbeat disabled in licence
        LicenseDescriptor mockLicenseDescriptor =  mock(LicenseDescriptor.class);
        when(mockLicenseDescriptor.isHeartBeatDisabled()).thenReturn(true);
        heartbeat.onLicenseChange(mockLicenseDescriptor);

        assertFalse(heartbeat.isEnabled());
        assertFalse("Job should not be scheduled at this point.",isJobScheduled());

        // Revert back to default state
        heartbeat.onLicenseFail();

        // Check heartbeat is disabled and job unscheduled
        assertTrue(heartbeat.isEnabled());
        assertTrue("Job should be unscheduled.",isJobScheduled());
    }

    @Test
    public void testOnLicenceFailRevertsToDisabled()
    {
        // Disable heartbeat in data collector service ( as if set in prop file)
        when(mockDataCollectorService.isEnabledByDefault()).thenReturn(false);

        HeartBeat heartbeat = new HeartBeat(context,true);

        // heartbeat enabled in licence
        LicenseDescriptor mockLicenseDescriptor =  mock(LicenseDescriptor.class);
        when(mockLicenseDescriptor.isHeartBeatDisabled()).thenReturn(false);
        heartbeat.onLicenseChange(mockLicenseDescriptor);

        assertTrue(heartbeat.isEnabled());
        assertTrue("Job should be scheduled at this point.",isJobScheduled());

        // Revert back to default state
        heartbeat.onLicenseFail();

        // Check heartbeat is disabled and job unscheduled
        assertFalse(heartbeat.isEnabled());
        assertFalse("Job should be unscheduled.",isJobScheduled());
    }

    private boolean isJobScheduled()
    {
        Scheduler scheduler = (Scheduler) context.getBean("schedulerFactory");
        String[] jobs = {};
        try
        {
            jobs = scheduler.getJobNames( Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException e)
        {
            e.printStackTrace();
            fail("Exception before assertion.");
        }

        return Arrays.asList(jobs).contains("heartbeat");
    }
}
