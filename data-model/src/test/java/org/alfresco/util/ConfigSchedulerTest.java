/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.util;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.CronExpression;

public class ConfigSchedulerTest extends TestCase
{
    private static class TestRegistry
    {
        private final AtomicInteger readConfigCount = new AtomicInteger();

        private CronExpression cronExpression;

        private CronExpression initialAndOnErrorCronExpression;

        static class Data
        {
            // Can be anything the registry needs to update
            private String registersData;

            @Override
            public String toString()
            {
                return "register's data: " + registersData;
            }
        }

        private final ConfigScheduler<Data> configScheduler = new ConfigScheduler(this) {
            @Override
            public boolean readConfig() throws IOException
            {
                return TestRegistry.this.readConfig();
            }

            @Override
            public Object createData()
            {
                return TestRegistry.this.createData();
            }
        };

        public Data createData()
        {
            return new Data();
        }

        public Data getData()
        {
            return configScheduler.getData();
        }

        public void setCronExpression(CronExpression cronExpression)
        {
            this.cronExpression = cronExpression;
        }

        public void setInitialAndOnErrorCronExpression(CronExpression initialAndOnErrorCronExpression)
        {
            this.initialAndOnErrorCronExpression = initialAndOnErrorCronExpression;
        }

        public void afterPropertiesSet()
        {
            // If we have a cronExpression it indicates that we will schedule reading.
            if (cronExpression != null)
            {
                PropertyCheck.mandatory(this, "initialAndOnErrorCronExpression", initialAndOnErrorCronExpression);
            }
            configScheduler.run(true, log, cronExpression, initialAndOnErrorCronExpression);
        }

        public int getReadConfigCount()
        {
            return readConfigCount.get();
        }

        /**
         * Cancels the scheduled job, so it cannot keep firing into later tests. Synchronized on the scheduler so a running job finishes switching schedules before its job is deleted, otherwise the job would be re-created after cancellation.
         */
        @SuppressWarnings("PMD.AvoidSynchronizedStatement")
        public void shutdown()
        {
            synchronized (configScheduler)
            {
                configScheduler.run(false, log, null, null);
            }
        }

        public boolean readConfig()
        {
            Data data = getData();
            data.registersData = "Can be anything " + readConfigCount.incrementAndGet();
            System.err.println(data.registersData);
            return mockSuccessReadingConfig;
        }
    }

    private static final Log log = LogFactory.getLog(ConfigSchedulerTest.class);
    private static boolean mockSuccessReadingConfig = true;

    private TestRegistry registry = new TestRegistry();
    private long startMs;

    @Before
    public void setUp() throws Exception
    {
        mockSuccessReadingConfig = true;
    }

    @After
    public void tearDown()
    {
        registry.shutdown();
    }

    private String getMs()
    {
        return (System.currentTimeMillis() - startMs) + "ms: ";
    }

    public TestRegistry.Data assertDataChanged(TestRegistry.Data prevData, String msg)
    {
        // If the data changes, there has been a read
        System.out.println(getMs() + msg);
        TestRegistry.Data data = registry.getData();
        assertNotEquals("The configuration data should have changed: " + msg, prevData, data);
        return data;
    }

    public TestRegistry.Data assertDataUnchanged(TestRegistry.Data prevData, String msg)
    {
        // If the data changes, there has been a read
        System.out.println(getMs() + msg);
        TestRegistry.Data data = registry.getData();
        assertEquals("The configuration data should be the same: " + msg, prevData, data);
        return data;
    }

    @Test
    public void testSwitchingOfSchedule() throws Exception
    {
        registry.setInitialAndOnErrorCronExpression(new CronExpression(("0/2 * * ? * * *"))); // every 2 seconds
        registry.setCronExpression(new CronExpression(("0/4 * * ? * * *"))); // every 4 seconds

        // Sleep until a 6 second boundary, in order to make testing clearer.
        // It avoids having to work out schedule offsets and extra quick runs that can otherwise take place.
        Thread.sleep(4000 - System.currentTimeMillis() % 4000);
        startMs = System.currentTimeMillis();
        mockSuccessReadingConfig = false;
        registry.afterPropertiesSet();
        TestRegistry.Data data = registry.getData();

        Thread.sleep(1000); // 1 seconds
        data = assertDataChanged(data, "There should have been a read after a few milliseconds that fails");

        Thread.sleep(2000); // 3 seconds
        data = assertDataChanged(data, "There should have been a read after 2 seconds that fails");

        Thread.sleep(2000); // 5 seconds
        data = assertDataChanged(data, "There should have been a read after 4 seconds that fails");

        Thread.sleep(2000); // 7 seconds
        data = assertDataChanged(data, "There should have been a read after 6 seconds that fails");

        // Should switch to normal 4s schedule after the next read, so the read at 12 seconds will be on that schedule.
        // It is always possible that another quick one gets scheduled almost straight away after the next read.
        mockSuccessReadingConfig = true;
        Thread.sleep(2000); // 9 seconds
        data = assertDataChanged(data, "There should have been a read after 8 seconds that succeeds");

        Thread.sleep(2000); // 11 seconds
        data = assertDataUnchanged(data, "There really should not have been a read until 12 seconds");

        Thread.sleep(2000); // 13 seconds
        data = assertDataChanged(data, "There should have been a read after 12 seconds that succeeds");

        // Should switch back to initial/error schedule after failure
        mockSuccessReadingConfig = false;
        Thread.sleep(4000); // 17 seconds
        data = assertDataChanged(data, "There should have been a read after 16 seconds that fails");

        Thread.sleep(2000); // 19 seconds
        data = assertDataChanged(data, "There should have been a read after 18 seconds");
    }

    @Test
    public void testConfigIsReadDuringStartup() throws Exception
    {
        // A cron that will not fire during the test, so any read can only be the startup one.
        registry.setInitialAndOnErrorCronExpression(new CronExpression("0 0 0 1 1 ? 2099"));
        registry.setCronExpression(new CronExpression("0 0 0 1 1 ? 2099"));
        startMs = System.currentTimeMillis();

        registry.afterPropertiesSet();

        // The read is handed to a scheduler thread, so it is not instant.
        await("the config to be read on startup")
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ZERO)
                .pollInterval(Duration.ofMillis(50))
                .until(() -> registry.getReadConfigCount() > 0);

        assertEquals("The config should have been read once on startup", 1, registry.getReadConfigCount());
    }
}
