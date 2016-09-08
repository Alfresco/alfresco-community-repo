/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * Base class for all audit administration webscripts.
 * 
 * @author Gavin Cornwell
 */
public class BaseAuditAdminWebScript extends DeclarativeWebScript
{
    protected RecordsManagementAuditService rmAuditService;
    
    /**
     * Sets the RecordsManagementAuditService instance
     * 
     * @param auditService The RecordsManagementAuditService instance
     */
    public void setRecordsManagementAuditService(RecordsManagementAuditService rmAuditService)
    {
        this.rmAuditService = rmAuditService;
    }
    
    /**
     * Creates a model to represent the current status of the RM audit log.
     * 
     * @return Map of RM audit log status
     */
    protected Map<String, Object> createAuditStatusModel()
    {
        Map<String, Object> auditStatus = new HashMap<String, Object>(3);
        
        auditStatus.put("started", ISO8601DateFormat.format(rmAuditService.getDateLastStarted()));
        auditStatus.put("stopped", ISO8601DateFormat.format(rmAuditService.getDateLastStopped()));
        auditStatus.put("enabled", Boolean.valueOf(rmAuditService.isEnabled()));
        
        return auditStatus;
    }
}