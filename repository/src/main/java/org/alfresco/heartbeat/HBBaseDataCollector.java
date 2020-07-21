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
package org.alfresco.heartbeat;

import java.util.List;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.jobs.HeartBeatJobScheduler;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.alfresco.util.PropertyCheck;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 *
 * <p>
 * This class is to be extended by HeartBeat data collectors.
 * Every new collector needs to provide details of the data it collects. As an example use one
 * of the existing collectors {@link AuthoritiesDataCollector}, {@link ConfigurationDataCollector},
 * {@link InfoDataCollector}, {@link ModelUsageDataCollector}, {@link SystemUsageDataCollector}.
 * </p>
 * <p>
 * Each collector provides a reference to a {@link HeartBeatJobScheduler} which
 * is then used by the {@link HBDataCollectorService} to schedule and unschedule jobs for this collector.
 * </p>
 *
 * Example Javadoc for implementations:
 *
 * <ul>
 *  <li>Collector ID: <b>a.collector.id</b></li>
 *  <li>Data:
 *      <ul>
 *          <li><b>dataP1:</b> data type - description</li>
 *          <li><b>dataP2:</b> data type - description</li>
 *          ...
 *      </ul>
 *  </li>
 * </ul>
 *
 * @author eknizat
 */
public abstract class HBBaseDataCollector
{
    private final String collectorId;
    private final String collectorVersion;
    private final String cronExpression;

    /** The collector service managing this collector. */
    private HBDataCollectorService hbDataCollectorService;

    /** The job scheduler used to schedule a job for this collector */
    private HeartBeatJobScheduler hbJobScheduler;

    /**
     *
     * @param collectorId Unique collector ID e.g.: acs.repository.info
     * @param collectorVersion Version of the collector e.g.: 1.0
     * @param cronExpression Cron expression that will be used to schedule jobs for this collector. e.g.: "0 0 0 ? * SUN" (Weekly)
     * @param hbJobScheduler Scheduler that will be used to schedule jobs for this collector.
     */
    public HBBaseDataCollector(String collectorId, String collectorVersion, String cronExpression,
                               HeartBeatJobScheduler hbJobScheduler)
    {
        PropertyCheck.mandatory(this, "collectorId", collectorId);
        PropertyCheck.mandatory(this, "collectorVersion", collectorVersion);
        PropertyCheck.mandatory(this, "cronExpression", cronExpression);
        PropertyCheck.mandatory(this, "hbJobScheduler", hbJobScheduler);

        this.collectorId = collectorId;
        this.collectorVersion = collectorVersion;
        this.cronExpression = cronExpression;
        this.hbJobScheduler = hbJobScheduler;
    }

    public String getCollectorId()
    {
        return collectorId;
    }

    public String getCollectorVersion()
    {
        return this.collectorVersion;
    }

    public String getCronExpression()
    {
        return this.cronExpression;
    }

    public void setHbDataCollectorService(HBDataCollectorService hbDataCollectorService)
    {
        this.hbDataCollectorService = hbDataCollectorService;
    }

    /**
     *
     * @param hbJobScheduler The scheduler which will be used to schedule jobs for this collector.
     */
    public void setHbJobScheduler(HeartBeatJobScheduler hbJobScheduler)
    {
        ParameterCheck.mandatory("hbJobScheduler", hbJobScheduler);
        this.hbJobScheduler = hbJobScheduler;
    }

    /**
     *
     * @return JobScheduler used to schedule jobs for this collector.
     */
    public HeartBeatJobScheduler getHbJobScheduler()
    {
        return this.hbJobScheduler;
    }

    /**
     * This method is called by Spring at initialisation and will register this collector with the provided {@link HBDataCollectorService}
     */
    public void register()
    {
        if (hbDataCollectorService == null)
        {
            throw new IllegalStateException("HbDataCollectorService needs to be set before calling this method.");
        }
        hbDataCollectorService.registerCollector(this);
    }

    /**
     * This method is called by Spring at initialisation and will deregister this collector with the provided {@link HBDataCollectorService}
     */
    public void deregister()
    {
        if (hbDataCollectorService == null)
        {
            throw new IllegalStateException("HbDataCollectorService needs to be set before calling this method.");
        }
        hbDataCollectorService.deregisterCollector(this);
    }

    /**
     * This method returns data to be collected.
     * @return List of {@link HBData}
     */
    public abstract List<HBData> collectData();
}
