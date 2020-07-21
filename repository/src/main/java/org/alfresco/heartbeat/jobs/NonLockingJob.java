/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.heartbeat.jobs;

import org.alfresco.heartbeat.HBBaseDataCollector;
import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.datasender.HBDataSenderService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 *
 *  This Heartbeat job collects data and passes it to the {@link HBDataSenderService}.
 *  @author eknizat
 */
public class NonLockingJob implements Job
{
    /** The logger. */
    private static final Log logger = LogFactory.getLog(NonLockingJob.class);

    public static final String COLLECTOR_KEY = "collector";
    public static final String DATA_SENDER_SERVICE_KEY = "hbDataSenderService";

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        final JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        final HBBaseDataCollector collector = (HBBaseDataCollector) dataMap.get(COLLECTOR_KEY);
        final HBDataSenderService hbDataSenderService = (HBDataSenderService) dataMap.get(DATA_SENDER_SERVICE_KEY);

        ParameterCheck.mandatory( COLLECTOR_KEY, collector);
        ParameterCheck.mandatory( DATA_SENDER_SERVICE_KEY, hbDataSenderService);

        try
        {
            List<HBData> data = collector.collectData();
            hbDataSenderService.sendData(data);

            if (logger.isDebugEnabled())
            {
                logger.debug("Finished collector job. ID:" + collector.getCollectorId());
            }
        }
        catch (final Exception e)
        {
            // Log the error but don't rethrow, collector errors are non fatal
            logger.error("Heartbeat failed to collect data for collector ID: " + collector.getCollectorId(), e);
        }
    }
}