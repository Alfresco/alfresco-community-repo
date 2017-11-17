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

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class UsageSystemDataCollector extends HBBaseDataCollector
{
    /** The logger. */
    private static final Log logger = LogFactory.getLog(UsageSystemDataCollector.class);

    /** DAO for current repository descriptor. */
    private DescriptorDAO currentRepoDescriptorDAO;

    public UsageSystemDataCollector(String collectorId)
    {
        super(collectorId);
    }

    public void setCurrentRepoDescriptorDAO(DescriptorDAO currentRepoDescriptorDAO)
    {
        this.currentRepoDescriptorDAO = currentRepoDescriptorDAO;
    }

    @Override
    public List<HBData> collectData()
    {
        if(currentRepoDescriptorDAO == null)
        {
            logger.debug("Couldn't collect data because repository descriptor is null");
            return null;
        }
        logger.debug("Preparing repository usage (system) data...");

        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemUsageValues = new HashMap<>();
        systemUsageValues.put("memFree", runtime.freeMemory());
        systemUsageValues.put("memMax", runtime.maxMemory());
        systemUsageValues.put("memTotal", runtime.totalMemory());
        HBData systemUsageData = new HBData(
                this.currentRepoDescriptorDAO.getDescriptor().getId(),
                this.getCollectorId(),
                this.getCollectorVersion(),
                new Date(),
                systemUsageValues);

        List<HBData> collectedData = new LinkedList<>();
        collectedData.add(systemUsageData);

        return collectedData;
    }
}
