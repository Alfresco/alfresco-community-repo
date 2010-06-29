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

import java.io.Serializable;

import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * The implementation of the AuditService for application auditing.
 * 
 * @author Andy Hind
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
     * @since 3.2
     */
    public boolean isAuditEnabled(String applicationName, String path)
    {
        // Get the root path for the application
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
    public void clearAudit(String applicationName)
    {
        Long now = Long.valueOf(System.currentTimeMillis());
        auditComponent.deleteAuditEntries(applicationName, null, now);
    }

    /**
     * {@inheritDoc}
     * @since 3.3
     */
    public void auditQuery(AuditQueryCallback callback, AuditQueryParameters parameters, int maxResults)
    {
        auditComponent.auditQuery(callback, parameters, maxResults);
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public void auditQuery(
            AuditQueryCallback callback,
            boolean forward,
            String applicationName, String user, Long from, Long to,
            int maxResults)

    {
        ParameterCheck.mandatory("callback", callback);
        
        AuditQueryParameters params = new AuditQueryParameters();
        params.setForward(true);
        params.setApplicationName(applicationName);
        params.setUser(user);
        params.setFromTime(from);
        params.setToTime(to);
        
        auditComponent.auditQuery(callback, params, maxResults);
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     */
    public void auditQuery(
            AuditQueryCallback callback,
            boolean forward,
            String applicationName, String user, Long from, Long to,
            String searchKey, Serializable searchValue,
            int maxResults)

    {
        ParameterCheck.mandatory("callback", callback);
        
        AuditQueryParameters params = new AuditQueryParameters();
        params.setForward(true);
        params.setApplicationName(applicationName);
        params.setUser(user);
        params.setFromTime(from);
        params.setToTime(to);
        if (searchKey != null || searchValue != null)
        {
            params.addSearchKey(searchKey, searchValue);
        }
        
        auditComponent.auditQuery(callback, params, maxResults);
    }
}