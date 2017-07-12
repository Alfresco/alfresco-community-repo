/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.rest.api.Audit;
import org.alfresco.rest.api.model.AuditApp;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditApplication;

/**
 * Handles audit (applications & entries)
 *
 * @author janv
 */
public class AuditImpl implements Audit
{
    private final static String DISABLED = "Audit is disabled system-wide";

    private AuditService auditService;

    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    private void checkEnabled()
    {
        if (!auditService.isAuditEnabled())
        {
            throw new DisabledServiceException(DISABLED);
        }
    }

    @Override
    public AuditApp getAuditApp(String auditAppId, Parameters parameters)
    {
        checkEnabled();

        AuditService.AuditApplication auditApplication = findAuditAppById(auditAppId);

        if (auditApplication == null)
        {
            throw new EntityNotFoundException(auditAppId);
        }

        return new AuditApp(auditApplication.getKey().substring(1), auditApplication.getName(), auditApplication.isEnabled());
    }

    private AuditService.AuditApplication findAuditAppById(String auditAppId)
    {
        AuditService.AuditApplication auditApp = null;
        Map<String, AuditService.AuditApplication> auditApplicationsByName = auditService.getAuditApplications();
        if (auditApplicationsByName != null)
        {
            for (AuditService.AuditApplication auditApplication : auditApplicationsByName.values())
            {
                if (auditApplication.getKey().equals("/" + auditAppId))
                {
                    auditApp = auditApplication;
                }
            }
        }
        return auditApp;
    }

    @Override
    public CollectionWithPagingInfo<AuditApp> getAuditApps(Paging paging)
    {
        checkEnabled();

        Map<String, AuditService.AuditApplication> auditApplicationsByName = auditService.getAuditApplications();

        Set<String> audAppsName = new TreeSet<String>(auditApplicationsByName.keySet());
        Iterator<String> audAppsNameIt = audAppsName.iterator();

        int skipCount = paging.getSkipCount();
        int maxItems = paging.getMaxItems();
        int totalItems = audAppsName.size();
        int end = skipCount + maxItems;

        if (skipCount >= totalItems)
        {
            List<AuditApp> empty = Collections.emptyList();
            return CollectionWithPagingInfo.asPaged(paging, empty, false, totalItems);
        }

        List<AuditApp> auditApps = new ArrayList<AuditApp>(totalItems);
        int count = 0;
        for (int i = 0; i < end && audAppsNameIt.hasNext(); i++)
        {
            String auditAppName = audAppsNameIt.next();
            if (i < skipCount)
            {
                continue;
            }
            count++;
            AuditApplication auditApplication = auditApplicationsByName.get(auditAppName);

            auditApps.add(new AuditApp(auditApplication.getKey().substring(1), auditApplication.getName(), auditApplication.isEnabled()));
        }

        boolean hasMoreItems = (skipCount + count < totalItems);

        return CollectionWithPagingInfo.asPaged(paging, auditApps, hasMoreItems, totalItems);
    }

}
