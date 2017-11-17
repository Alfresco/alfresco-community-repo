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
import org.alfresco.repo.scheduler.AlfrescoSchedulerFactory;
import org.alfresco.service.license.LicenseDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Arrays;
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
    private final String INVALID_CRON = "0 no no * * ?";
    private final HBBaseDataCollector validCollector1 = new SimpleHBDataCollector("validCollector1", VALID_CRON);
    private final HBBaseDataCollector validCollector2 = new SimpleHBDataCollector("validCollector2", VALID_CRON);
    private Scheduler scheduler;

    @Before
    public void before() throws Exception
    {
        // Create fresh scheduler
        SchedulerFactoryBean sfb = new SchedulerFactoryBean();
        sfb.setSchedulerFactoryClass(AlfrescoSchedulerFactory.class);
        sfb.setAutoStartup(false);
        sfb.afterPropertiesSet();
        scheduler = sfb.getScheduler();
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
        collectorService.setScheduler(scheduler);

        // Register few collectors
        registerValidCollectors(collectorService);

        // Check that the jobs are scheduled for all collectors when heartbeat is enabled
        assertTrue(collectorService.isEnabled());
        assertCollectorJobsScheduled("Job was not scheduled but HB is enabled", scheduler);
    }

    @Test
    public void testJobSchedulingWhenDisabled() throws Exception
    {
        // Disable heartbeat by setting the default enabled state ( as if set in prop file)
        final HBDataCollectorServiceImpl collectorService = new HBDataCollectorServiceImpl(false);
        collectorService.setScheduler(scheduler);

        // Register collectors
        registerValidCollectors(collectorService);

        // Check that the jobs are not scheduled for any collectors when heartbeat is disabled
        assertFalse(collectorService.isEnabled());
        assertCollectorJobsNotScheduled("Job was scheduled but HB is disabled", scheduler);
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
        collectorService.setScheduler(scheduler);

        // Register few collectors
        registerValidCollectors(collectorService);

        // Heartbeat disabled in licence
        enabledHbInLicense( collectorService,false);

        // Check heart beat is disabled and all collector jobs are unscheduled
        assertFalse(collectorService.isEnabled());
        assertCollectorJobsNotScheduled("Job was scheduled but HB is disabled", scheduler);

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
        collectorService.setScheduler(scheduler);

        // Register few collectors
        registerValidCollectors(collectorService);

        // Heartbeat enabled in licence
        enabledHbInLicense( collectorService,true);

        // Check heart beat is enabled and all collector jobs are scheduled
        assertTrue(collectorService.isEnabled());
        assertCollectorJobsScheduled("Job was not scheduled but HB is enabled", scheduler);

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
        collectorService.setScheduler(scheduler);

        // Register few collectors
        registerValidCollectors(collectorService);

        // Heartbeat disabled in licence
        enabledHbInLicense( collectorService,false);

        // Revert back to default state
        collectorService.onLicenseFail();

        // Check heartbeat state reverts to default enabled state and jobs are scheduled
        assertTrue(collectorService.isEnabled());
        assertCollectorJobsScheduled("Job should be unscheduled.", scheduler);
    }

    /**
     * Heartbeat revert back to default disabled state on license fail
     */
    @Test
    public void testOnLicenceFailRevertsToDisabled() throws Exception
    {
        // Disable heartbeat by setting the default enabled state ( as if set in prop file)
        final HBDataCollectorServiceImpl collectorService = new HBDataCollectorServiceImpl(false);
        collectorService.setScheduler(scheduler);

        // Register few collectors
        registerValidCollectors(collectorService);

        // Heartbeat enabled in licence
        enabledHbInLicense( collectorService,true);

        // Revert back to default state
        collectorService.onLicenseFail();

        // Check heartbeat is disabled and job unscheduled
        assertFalse(collectorService.isEnabled());
        assertCollectorJobsNotScheduled("Job should be unscheduled.",scheduler);
    }

    /**
     * Test scheduling job for collector with invalid cron expression
     */
    @Test
    public void testInvalidCronExpression() throws Exception
    {
        final HBDataCollectorServiceImpl collectorService = new HBDataCollectorServiceImpl(true);
        collectorService.setScheduler(scheduler);

        // Register collector with valid cron expression
        SimpleHBDataCollector c1 = new SimpleHBDataCollector("c1", VALID_CRON);
        collectorService.registerCollector(c1);

        // Register collector with invalid cron expression
        SimpleHBDataCollector c2 = new SimpleHBDataCollector("c2", INVALID_CRON);
        collectorService.registerCollector(c2);

        assertTrue(isJobScheduledForCollector(c1.getCollectorId(),scheduler));
        assertFalse(isJobScheduledForCollector(c2.getCollectorId(),scheduler));
    }

    /**
     *
     * Jobs are scheduled with cron expressions provided by collectors
     *
     */
    @Test
    public void testJobsScheduledWithDifferentCronExpressions() throws Exception
    {
        final HBDataCollectorServiceImpl collectorService = new HBDataCollectorServiceImpl(true);
        collectorService.setScheduler(scheduler);

        final String cron1 = "0 0/1 * * * ?";
        final String cron2 = "0 0/2 * * * ?";
        final String cron3 = "0 0/3 * * * ?";

        final HBBaseDataCollector c1 = new SimpleHBDataCollector("c1", cron1);
        final HBBaseDataCollector c2 = new SimpleHBDataCollector("c2", cron2);
        final HBBaseDataCollector c3 = new SimpleHBDataCollector("c3", cron3);

        final String triggerName1 = "heartbeat-" + c1.getCollectorId() + "-Trigger";
        final String triggerName2 = "heartbeat-" + c2.getCollectorId() + "-Trigger";
        final String triggerName3 = "heartbeat-" + c3.getCollectorId() + "-Trigger";

        // Register 3 collectors with 3 different cron expressions
        collectorService.registerCollector(c1);
        collectorService.registerCollector(c2);
        collectorService.registerCollector(c3);

        String testCron1 = ((CronTrigger) scheduler.getTrigger(triggerName1, Scheduler.DEFAULT_GROUP)).getCronExpression();
        String testCron2 = ((CronTrigger) scheduler.getTrigger(triggerName2, Scheduler.DEFAULT_GROUP)).getCronExpression();
        String testCron3 = ((CronTrigger) scheduler.getTrigger(triggerName3, Scheduler.DEFAULT_GROUP)).getCronExpression();

        assertEquals("Cron expression doesn't match", cron1, testCron1);
        assertEquals("Cron expression doesn't match", cron2, testCron2);
        assertEquals("Cron expression doesn't match", cron3, testCron3);
    }

    @Test
    public void testRegisterSameCollectorTwice() throws Exception
    {
        final HBDataCollectorServiceImpl collectorService = new HBDataCollectorServiceImpl(true);
        collectorService.setScheduler(scheduler);

        HBBaseDataCollector c1 = new SimpleHBDataCollector("c1", VALID_CRON);

        collectorService.registerCollector(c1);
        collectorService.registerCollector(c1);

        assertEquals("Expected only one collector to be scheduled.",1,scheduler.getJobNames(Scheduler.DEFAULT_GROUP).length );
    }

    // Helper methods

    private void registerValidCollectors(HBDataCollectorServiceImpl collectorService)
    {
        collectorService.registerCollector(validCollector1);
        collectorService.registerCollector(validCollector2);
    }

    private void assertCollectorJobsScheduled(String message, Scheduler scheduler) throws Exception
    {
        assertTrue(message, isJobScheduledForCollector(validCollector1.getCollectorId(), scheduler));
        assertTrue(message, isJobScheduledForCollector(validCollector2.getCollectorId(), scheduler));
    }

    private void assertCollectorJobsNotScheduled(String message, Scheduler scheduler) throws Exception
    {
        assertFalse(message, isJobScheduledForCollector(validCollector1.getCollectorId(), scheduler));
        assertFalse(message, isJobScheduledForCollector(validCollector2.getCollectorId(), scheduler));
    }

    private void enabledHbInLicense(HBDataCollectorServiceImpl collectorService, boolean activate)
    {
        LicenseDescriptor mockLicenseDescriptor = mock(LicenseDescriptor.class);
        when(mockLicenseDescriptor.isHeartBeatDisabled()).thenReturn(!activate);
        collectorService.onLicenseChange(mockLicenseDescriptor);
    }

    private boolean isJobScheduledForCollector(String collectorId, Scheduler scheduler) throws Exception
    {
        String jobName = "heartbeat-" + collectorId;
        String triggerName = jobName + "-Trigger";

        String[] jobs = scheduler.getJobNames(Scheduler.DEFAULT_GROUP);
        String[] triggers = scheduler.getTriggerNames(Scheduler.DEFAULT_GROUP);
        return Arrays.asList(jobs).contains(jobName) && Arrays.asList(triggers).contains(triggerName);
    }

    private class SimpleHBDataCollector extends HBBaseDataCollector
    {
        public SimpleHBDataCollector(String collectorId, String cron)
        {
            super(collectorId);
            this.setCronExpression(cron);
        }

        public List<HBData> collectData()
        {
            List<HBData> result = new LinkedList<>();
            return result;
        }
    }
}
