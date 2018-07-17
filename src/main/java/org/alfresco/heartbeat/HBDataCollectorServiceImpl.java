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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.heartbeat.datasender.HBDataSenderService;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.service.license.LicenseService.LicenseChangeHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This service lets implementations of {@link HBBaseDataCollector} register. <br>
 * Registered collectors have jobs scheduled or unscheduled based on the enabled state of Heartbeat. <br>
 * This service listens to events from {@link LicenseChangeHandler} and enables or disables Heartbeat accordingly.
 *
 */
public class HBDataCollectorServiceImpl implements HBDataCollectorService, LicenseChangeHandler
{
    /** The logger. */
    private static final Log logger = LogFactory.getLog(HBDataCollectorServiceImpl.class);

    /** List of collectors registered with this service */
    private Map<String, HBBaseDataCollector> collectors = new HashMap<>();

    /** The service responsible for sending the collected data */
    private HBDataSenderService hbDataSenderService;

    /** The default enable state */
    private final boolean defaultHbState;

    /** Current enabled state */
    private boolean enabled = false;

    /**
     *
     * @param defaultHeartBeatState
     *          the default enabled state of heartbeat
     *
     */
    public HBDataCollectorServiceImpl (boolean defaultHeartBeatState)
    {
        this.defaultHbState = defaultHeartBeatState;
        this.enabled = defaultHeartBeatState;
    }

    public void setHbDataSenderService(HBDataSenderService hbDataSenderService)
    {
        this.hbDataSenderService = hbDataSenderService;
    }

    public synchronized boolean isEnabled()
    {
        return this.enabled;
    }

    /**
     *
     * Register data collector with this service, a job will be scheduled for the collector if Heartbeat is enabled.
     * The registered collector will be called to provide heartbeat data at the scheduled interval.
     * Each collector registered via this method must have a unique collector id.
     *
     * @param collector collector to register
     */
    @Override
    public synchronized void registerCollector(final HBBaseDataCollector collector)
    {
        // Check collector with the same ID does't already exist
        if(collectors.containsKey(collector.getCollectorId()))
        {
            throw new IllegalArgumentException("HeartBeat did not registered collector because a collector with ID: \n"
                    + collector.getCollectorId() + " already exists. Collectors must have unique collector IDs" );
        }
        // Schedule collector job
        scheduleCollector(collector);
        // Add collector to list of registered collectors
        collectors.put(collector.getCollectorId(), collector);

        if (logger.isDebugEnabled())
        {
            logger.debug("HeartBeat registered collector: " + collector.getCollectorId());
        }
    }

    /**
     *
     *
     * @param collector - Deregister data collector. Removed collector and unscheduled associated job.
     */
    @Override
    public synchronized void deregisterCollector(final HBBaseDataCollector collector)
    {
        if (collectors.remove(collector.getCollectorId(), collector))
        {
            collector.getHbJobScheduler().unscheduleJob(collector);

            if (logger.isDebugEnabled())
            {
                logger.debug("HeartBeat unscheduled job and deregistered collector: " + collector.getCollectorId());
            }
        }
    }

    @Override
    public boolean isEnabledByDefault()
    {
        return defaultHbState;
    }

    private void scheduleCollector(final HBBaseDataCollector collector)
    {
        if (this.enabled)
        {
            collector.getHbJobScheduler().scheduleJob(collector);
        }
        else
        {
            collector.getHbJobScheduler().unscheduleJob(collector);
        }
    }

    /**
     * Listens for license changes.  If a license is change or removed, the heartbeat job is rescheduled.
     */
    @Override
    public synchronized void onLicenseChange(final LicenseDescriptor licenseDescriptor)
    {
        final boolean newEnabled = !licenseDescriptor.isHeartBeatDisabled();

        if (newEnabled != this.enabled)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("HeartBeat enabled state change. Enabled=" + newEnabled);
            }
            setEnable(newEnabled);
            restartAllCollectorSchedules();
        }
    }

    /**
     * License load failure resets the heartbeat back to the default state
     */
    @Override
    public synchronized void onLicenseFail()
    {
        final boolean newEnabled = isEnabledByDefault();

        if (newEnabled != this.enabled)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("HeartBeat enabled state change. Enabled=" + newEnabled);
            }
            setEnable(newEnabled);
            restartAllCollectorSchedules();
        }
    }

    private void restartAllCollectorSchedules()
    {
        for( HBBaseDataCollector collector : collectors.values() )
        {
            try
            {
                scheduleCollector(collector);
            }
            catch (Exception e)
            {
                // Log and ignore
                logger.error("HeartBeat failed to restart collector: " + collector.getCollectorId() ,e);
            }
        }
    }

    private void setEnable(boolean enable)
    {
        this.enabled = enable;
        if (hbDataSenderService != null)
        {
            hbDataSenderService.enable(enable);
        }
    }
}
