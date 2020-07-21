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

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * A collector of data related the <code>Runtime</code> data of the system. Every Java application has a single instance of class
 * <code>Runtime</code> that allows the application to interface with the environment in which the application is running.
 * <ul>
 *  <li>Collector ID: <b>acs.repository.usage.system</b></li>
 *  <li>Data:
 *      <ul>
 *          <li><b>memFree:</b> Long - The amount of free memory in the Java Virtual Machine. {@link Runtime#freeMemory()}</li>
 *          <li><b>memMax:</b> Long -T he maximum amount of memory that the Java virtual machine will
 * attempt to use. {@link Runtime#maxMemory()}</li>
 *          <li><b>memTotal:</b> Long - The total amount of memory in the Java virtual machine. {@link Runtime#totalMemory()}</li>
 *      </ul>
 *  </li>
 * </ul>
 *
 * @author eknizat
 */
public class SystemUsageDataCollector extends HBBaseDataCollector implements InitializingBean
{
    /** The logger. */
    private static final Log logger = LogFactory.getLog(SystemUsageDataCollector.class);

    /** DAO for current repository descriptor. */
    private DescriptorDAO currentRepoDescriptorDAO;

    public SystemUsageDataCollector(String collectorId, String collectorVersion, String cronExpression)
    {
        super(collectorId, collectorVersion, cronExpression);
    }

    public void setCurrentRepoDescriptorDAO(DescriptorDAO currentRepoDescriptorDAO)
    {
        this.currentRepoDescriptorDAO = currentRepoDescriptorDAO;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "currentRepoDescriptorDAO", currentRepoDescriptorDAO);
    }

    @Override
    public List<HBData> collectData()
    {
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

        return Arrays.asList(systemUsageData);
    }
}
