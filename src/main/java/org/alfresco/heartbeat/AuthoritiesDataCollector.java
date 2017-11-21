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
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * This class collects authorities data for HBDataCollectorService.
 * <br>
 * <b>Collector ID:</b> acs.repository.usage.authorities
 * <br>
 * <b>Data points:</b> numUsers, numGroups
 *
 * @author eknizat
 */
public class AuthoritiesDataCollector extends HBBaseDataCollector implements InitializingBean
{

    /** The logger. */
    private static final Log logger = LogFactory.getLog(AuthoritiesDataCollector.class);

    /** DAO for current repository descriptor. */
    private DescriptorDAO currentRepoDescriptorDAO;

    /** The authority service. */
    private AuthorityService authorityService;

    public AuthoritiesDataCollector(String collectorId, String collectorVersion, String cronExpression)
    {
        super(collectorId, collectorVersion, cronExpression);
    }

    public void setCurrentRepoDescriptorDAO(DescriptorDAO currentRepoDescriptorDAO)
    {
        this.currentRepoDescriptorDAO = currentRepoDescriptorDAO;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "authorityService", authorityService);
        PropertyCheck.mandatory(this, "currentRepoDescriptorDAO", currentRepoDescriptorDAO);
    }

    @Override
    public List<HBData> collectData()
    {
        this.logger.debug("Preparing repository usage (authorities) data...");

        Map<String, Object> authoritiesUsageValues = new HashMap<>();
        authoritiesUsageValues.put("numUsers", new Integer(this.authorityService.getAllAuthoritiesInZone(
                AuthorityService.ZONE_APP_DEFAULT, AuthorityType.USER).size()));
        authoritiesUsageValues.put("numGroups", new Integer(this.authorityService.getAllAuthoritiesInZone(
                AuthorityService.ZONE_APP_DEFAULT, AuthorityType.GROUP).size()));
        HBData authoritiesUsageData = new HBData(
                this.currentRepoDescriptorDAO.getDescriptor().getId(),
                this.getCollectorId(),
                this.getCollectorVersion(),
                new Date(),
                authoritiesUsageValues);

        return Arrays.asList(authoritiesUsageData);
    }
}
