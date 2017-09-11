/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

import java.util.LinkedList;
import java.util.List;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.datasender.HBDataSenderService;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HBDataCollectorServiceImpl implements HBDataCollectorService
{
    /** The logger. */
    private static final Log logger = LogFactory.getLog(HBDataCollectorServiceImpl.class);

    /** List of collectors registered with this service */
    private List<HBBaseDataCollector> collectors = new LinkedList<>();

    /** The service responsible for sending the collected data */
    private HBDataSenderService hbDataSenderService;

    /** Current enabled state */
    private boolean enabled = false;

    /** The default enable state */
    private final boolean defaultHbState;

    /**
     *
     * @param defaultHeartBeatState the default enabled state of heartbeat
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

    /**
     *
     * Register data collector with this service.
     * The registered collectors will be called to provide heartbeat data.
     *
     * @param collector collector to register
     */
    @Override
    public void registerCollector(HBBaseDataCollector collector)
    {
        this.collectors.add(collector);
    }

    /**
     *  Collects and sends data for all registered collectors using the provided sender service.
     */
    @Override
    public void collectAndSendData()
    {
        for (HBBaseDataCollector collector : collectors)
        {
            List<HBData> data = collector.collectData();
            try
            {
                hbDataSenderService.sendData(data);
            }
            catch (Exception e)
            {
                // Log exception
                logger.error(e);
            }
        }
    }

    @Override
    public boolean getDefaultHbState()
    {
        return defaultHbState;
    }

    @Override
    public synchronized boolean isHbEnabled()
    {
        return enabled;
    }

    @Override
    public synchronized void setHbEnabled(boolean enabled)
    {
        this.enabled = enabled;
        
        this.hbDataSenderService.enable(enabled);
    }

}
