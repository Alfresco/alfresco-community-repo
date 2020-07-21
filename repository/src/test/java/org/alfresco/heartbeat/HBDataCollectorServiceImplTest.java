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

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.datasender.HBDataSenderService;
import org.alfresco.heartbeat.jobs.HeartBeatJobScheduler;
import org.alfresco.service.license.LicenseDescriptor;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author eknizat
 */
public class HBDataCollectorServiceImplTest
{

    private final String VALID_CRON = "0 0/2 * * * ?";
    private final HBBaseDataCollector validCollector1 = new SimpleHBDataCollector("validCollector1", VALID_CRON);
    private final HBBaseDataCollector validCollector2 = new SimpleHBDataCollector("validCollector2", VALID_CRON);

    List<HBBaseDataCollector> scheduledCollectors = new LinkedList<>();

    @Before
    public void before() throws Exception
    {
        scheduledCollectors = new LinkedList<>();

    }

    @Test
    public void testInitialEnabledEqualsDefaultState() throws Exception
    {
        HBDataCollectorServiceImpl dataCollectorService = new HBDataCollectorServiceImpl(true);
        assertTrue(dataCollectorService.isEnabledByDefault());

        dataCollectorService = new HBDataCollectorServiceImpl(false);
        assertFalse(dataCollectorService.isEnabledByDefault());
    }

    @Test
    public void testJobSchedulingWhenEnabled() throws Exception
    {
        // Enable heartbeat by setting the default enabled state ( as if set in prop file)
        final HBDataCollectorServiceImpl collectorService = new HBDataCollectorServiceImpl(true);

        // Register few collectors
        registerValidCollectors(collectorService);

        // Check that the jobs are scheduled for all collectors when heartbeat is enabled
        assertTrue(collectorService.isEnabled());
        assertCollectorJobsScheduled("Job was not scheduled but HB is enabled");
    }

    @Test
    public void testJobSchedulingWhenDisabled() throws Exception
    {
        // Disable heartbeat by setting the default enabled state ( as if set in prop file)
        final HBDataCollectorServiceImpl collectorService = new HBDataCollectorServiceImpl(false);

        // Register collectors
        registerValidCollectors(collectorService);

        // Check that the jobs are not scheduled for any collectors when heartbeat is disabled
        assertFalse(collectorService.isEnabled());
        assertCollectorJobsNotScheduled("Job was scheduled but HB is disabled");
    }

    @Test
    // Based on testJobSchedulingWhenEnabled() and then calls deregister(..) and register(...).
    public void testDeregister() throws Exception
    {
        // Enable heartbeat by setting the default enabled state ( as if set in prop file)
        final HBDataCollectorServiceImpl collectorService = new HBDataCollectorServiceImpl(true);

        // Register few collectors
        registerValidCollectors(collectorService);

        // Check that the jobs are scheduled for all collectors when heartbeat is enabled
        assertTrue(collectorService.isEnabled());
        assertTrue("Job was not scheduled",            isJobScheduledForCollector(validCollector1));
        assertTrue("Job was not scheduled",            isJobScheduledForCollector(validCollector2));

        collectorService.deregisterCollector(validCollector1);

        assertFalse("Job should have be unregistered", isJobScheduledForCollector(validCollector1));
        assertTrue( "Job was not scheduled",           isJobScheduledForCollector(validCollector2));

        collectorService.registerCollector(validCollector1);
        collectorService.deregisterCollector(validCollector2);

        assertTrue( "Job was not scheduled",           isJobScheduledForCollector(validCollector1));
        assertFalse("Job should have be unregistered", isJobScheduledForCollector(validCollector2));
    }

    /**
     * Heartbeat enabled by default but disabled in licence on onLicenseChange
     */
    @Test
    public void testOnLicenseChangeOverridesDefaultEnabled() throws Exception
    {
        // Enable heartbeat by setting the default enabled state ( as if set in prop file)
        final HBDataCollectorServiceImpl collectorService = new HBDataCollectorServiceImpl(true);
        final HBDataSenderService sender = mock( HBDataSenderService.class);
        collectorService.setHbDataSenderService(sender);

        // Register few collectors
        registerValidCollectors(collectorService);

        // Heartbeat disabled in licence
        enabledHbInLicense( collectorService,false);

        // Check heart beat is disabled and all collector jobs are unscheduled
        assertFalse(collectorService.isEnabled());
        assertCollectorJobsNotScheduled("Job was scheduled but HB is disabled");

        // Also check sender is updated
        verify(sender).enable(false);
    }

    /**
     * Heartbeat disabled by default but enabled in licence on onLicenseChange
     */
    @Test
    public void testOnLicenseChangeOverridesDefaultDisabled() throws Exception
    {
        // Disable heartbeat by setting the default enabled state ( as if set in prop file)
        final HBDataCollectorServiceImpl collectorService = new HBDataCollectorServiceImpl(false);
        final HBDataSenderService sender = mock( HBDataSenderService.class);
        collectorService.setHbDataSenderService(sender);

        // Register few collectors
        registerValidCollectors(collectorService);

        // Heartbeat enabled in licence
        enabledHbInLicense( collectorService,true);

        // Check heart beat is enabled and all collector jobs are scheduled
        assertTrue(collectorService.isEnabled());
        assertCollectorJobsScheduled("Job was not scheduled but HB is enabled");

        // Also check sender is updated
        verify(sender).enable(true);
    }

    /**
     * Heartbeat revert back to default enabled state on license fail
     */
    @Test
    public void testOnLicenceFailRevertsToEnabled() throws Exception
    {
        // Enable heartbeat by setting the default enabled state ( as if set in prop file)
        final HBDataCollectorServiceImpl collectorService = new HBDataCollectorServiceImpl(true);

        // Register few collectors
        registerValidCollectors(collectorService);

        // Heartbeat disabled in licence
        enabledHbInLicense( collectorService,false);

        // Revert back to default state
        collectorService.onLicenseFail();

        // Check heartbeat state reverts to default enabled state and jobs are scheduled
        assertTrue(collectorService.isEnabled());
        assertCollectorJobsScheduled("Job should be unscheduled.");
    }

    /**
     * Heartbeat revert back to default disabled state on license fail
     */
    @Test
    public void testOnLicenceFailRevertsToDisabled() throws Exception
    {
        // Disable heartbeat by setting the default enabled state ( as if set in prop file)
        final HBDataCollectorServiceImpl collectorService = new HBDataCollectorServiceImpl(false);

        // Register few collectors
        registerValidCollectors(collectorService);

        // Heartbeat enabled in licence
        enabledHbInLicense( collectorService,true);

        // Revert back to default state
        collectorService.onLicenseFail();

        // Check heartbeat is disabled and job unscheduled
        assertFalse(collectorService.isEnabled());
        assertCollectorJobsNotScheduled("Job should be unscheduled.");
    }


    @Test(expected=IllegalArgumentException.class)
    public void testRegisterSameCollectorTwice() throws Exception
    {
        final HBDataCollectorServiceImpl collectorService = new HBDataCollectorServiceImpl(true);

        HBBaseDataCollector c1 = new SimpleHBDataCollector("c1", VALID_CRON);

        collectorService.registerCollector(c1);
        collectorService.registerCollector(c1);
    }

    // Helper methods

    private void registerValidCollectors(HBDataCollectorServiceImpl collectorService)
    {
        collectorService.registerCollector(validCollector1);
        collectorService.registerCollector(validCollector2);
    }

    private void assertCollectorJobsScheduled(String message) throws Exception
    {
        assertTrue(message, isJobScheduledForCollector(validCollector1));
        assertTrue(message, isJobScheduledForCollector(validCollector2));
    }

    private void assertCollectorJobsNotScheduled(String message) throws Exception
    {
        assertFalse(message, isJobScheduledForCollector(validCollector1));
        assertFalse(message, isJobScheduledForCollector(validCollector2));
    }

    private void enabledHbInLicense(HBDataCollectorServiceImpl collectorService, boolean activate)
    {
        LicenseDescriptor mockLicenseDescriptor = mock(LicenseDescriptor.class);
        when(mockLicenseDescriptor.isHeartBeatDisabled()).thenReturn(!activate);
        collectorService.onLicenseChange(mockLicenseDescriptor);
    }

    private boolean isJobScheduledForCollector(HBBaseDataCollector collector) throws Exception
    {
        return scheduledCollectors.contains(collector);
    }

    private class SimpleHBDataCollector extends HBBaseDataCollector
    {
        public SimpleHBDataCollector(String collectorId, String cron)
        {
            super(collectorId,"1.0",cron, new SimpleHBJobScheduler());
        }

        public List<HBData> collectData()
        {
            List<HBData> result = new LinkedList<>();
            return result;
        }
    }

    private class SimpleHBJobScheduler implements HeartBeatJobScheduler
    {
        @Override
        public void scheduleJob(HBBaseDataCollector collector)
        {
            scheduledCollectors.add(collector);
        }

        @Override
        public void unscheduleJob(HBBaseDataCollector collector)
        {
            scheduledCollectors.remove(collector);
        }
    }
}
