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

import java.util.List;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.service.cmr.repository.HBDataCollectorService;

/**
 *
 * This class is to be extended by HeartBeat data collectors.
  *
 * @author eknizat
 */
public abstract class HBBaseDataCollector {
    private String collectorId;
    private String collectorVersion;
    private String cronExpression;

    /**
     * The collector service managing this collector.
     */
    private HBDataCollectorService hbDataCollectorService;

    public HBBaseDataCollector(String collectorId)
    {
        this.collectorId = collectorId;
    }

    public String getCollectorId()
    {
        return collectorId;
    }

    public void setCollectorVersion(String collectorVersion)
    {
        this.collectorVersion = collectorVersion;
    }

    public String getCollectorVersion()
    {
        return this.collectorVersion;
    }

    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    public String getCronExpression()
    {
        return this.cronExpression;
    }

    /**
     * This method is called by Spring at initialisation and will register this collector with the provided {@link HBDataCollectorService}
     */
    public void register()
    {
        hbDataCollectorService.registerCollector(this);
    }
    
    public void setHbDataCollectorService(HBDataCollectorService hbDataCollectorService)
    {
        this.hbDataCollectorService = hbDataCollectorService;
    }

    /**
     * This method returns data to be collected.
     * @return List of {@link HBData}
     */
    public abstract List<HBData> collectData();
}
