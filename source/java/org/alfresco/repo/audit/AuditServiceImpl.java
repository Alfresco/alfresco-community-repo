/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.audit;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;

/**
 * The implementation of the AuditService for application auditing.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditServiceImpl implements AuditService
{
    private AuditComponent auditComponent;

    public AuditServiceImpl()
    {
        super();
    }

    public void setAuditComponent(AuditComponent auditComponent)
    {
        this.auditComponent = auditComponent;
    }

    /**
     * {@inheritDoc}
     * @since 3.4
     */
    public boolean isAuditEnabled()
    {
        return auditComponent.isAuditEnabled();
    }

    /**
     * {@inheritDoc}
     * @since 3.4
     */
    @Override
    public void setAuditEnabled(boolean enable)
    {
        auditComponent.setAuditEnabled(enable);
    }

    /**
     * {@inheritDoc}
     * @since 3.4
     */
    @Override
    public Map<String, AuditApplication> getAuditApplications()
    {
        Map<String, org.alfresco.repo.audit.model.AuditApplication> apps = auditComponent.getAuditApplications();

        Map<String, AuditApplication> ret = new TreeMap<String, AuditApplication>();
        for (String app : apps.keySet())
        {
            String name = app;
            String key = org.alfresco.repo.audit.model.AuditApplication.AUDIT_PATH_SEPARATOR + apps.get(app).getApplicationKey();
            boolean enabled = auditComponent.isAuditPathEnabled(app, key);
            AuditApplication auditApplication = new AuditApplication(name, key, enabled);
            ret.put(name, auditApplication);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public boolean isAuditEnabled(String applicationName, String path)
    {
        return auditComponent.isAuditPathEnabled(applicationName, path);
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public void enableAudit(String applicationName, String path)
    {
        auditComponent.enableAudit(applicationName, path);
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public void disableAudit(String applicationName, String path)
    {
        auditComponent.disableAudit(applicationName, path);
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public int clearAudit(String applicationName)
    {
        Long now = Long.valueOf(System.currentTimeMillis());
        return auditComponent.deleteAuditEntries(applicationName, null, now);
    }

    /**
     * {@inheritDoc}
     * @since 3.4
     */
    @Override
    public int clearAudit(String applicationName, Long fromTime, Long toTime)
    {
        toTime = (toTime == null) ? Long.valueOf(System.currentTimeMillis()) : toTime;
        return auditComponent.deleteAuditEntries(applicationName, fromTime, toTime);
    }

    /**
     * {@inheritDoc}
     * @since 3.3
     */
    public void auditQuery(AuditQueryCallback callback, AuditQueryParameters parameters, int maxResults)
    {
        auditComponent.auditQuery(callback, parameters, maxResults);
    }
}