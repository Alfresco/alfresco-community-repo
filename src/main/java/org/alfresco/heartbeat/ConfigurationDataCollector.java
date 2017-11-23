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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.traitextender.SpringExtensionBundle;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * This class collects authorities data for HeartBeat.
 * <br>
 * <b>Collector ID:</b> acs.repository.configuration
 * <br>
 * <b>Data points:</b> smartFoldersEnabled
 *
 * @author mpopa
 */
public class ConfigurationDataCollector extends HBBaseDataCollector implements InitializingBean
{
    /** DAO for current repository descriptor. */
    private DescriptorDAO currentRepoDescriptorDAO;

    /** The logger. */
    private static final Log logger = LogFactory.getLog(ConfigurationDataCollector.class);

    private SpringExtensionBundle smartFoldersBundle;

    public ConfigurationDataCollector(String collectorId, String collectorVersion, String cronExpression)
    {
        super(collectorId, collectorVersion, cronExpression);
    }
    
    public void setCurrentRepoDescriptorDAO(DescriptorDAO currentRepoDescriptorDAO)
    {
        this.currentRepoDescriptorDAO = currentRepoDescriptorDAO;
    }

    public void setSmartFoldersBundle(SpringExtensionBundle smartFoldersBundle)
    {
        this.smartFoldersBundle = smartFoldersBundle;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "smartFoldersBundle", smartFoldersBundle);
        PropertyCheck.mandatory(this, "currentRepoDescriptorDAO", currentRepoDescriptorDAO);
    }

    @Override
    public List<HBData> collectData()
    {
        // Collect repository configuration data
        logger.debug("Preparing repository configuration data...");
        Map<String, Object> configurationValues = new HashMap<>();
        configurationValues.put("smartFoldersEnabled", smartFoldersBundle.isEnabled());
        HBData configurationData = new HBData(
                this.currentRepoDescriptorDAO.getDescriptor().getId(),
                this.getCollectorId(),
                this.getCollectorVersion(),
                new Date(),
                configurationValues);
        List<HBData> collectedData = new LinkedList<>();
        collectedData.add(configurationData);
        return collectedData;
    }

}
