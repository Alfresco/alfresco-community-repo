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

import org.alfresco.rest.api.Audit;
import org.alfresco.rest.api.model.AuditApp;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.audit.AuditService;

import java.util.Map;

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
        if (! auditService.isAuditEnabled())
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
}
