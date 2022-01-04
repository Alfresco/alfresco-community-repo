/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * Base class for all audit administration webscripts.
 * 
 * @author Gavin Cornwell
 */
public class BaseAuditAdminWebScript extends DeclarativeWebScript
{
	/** Records management audit service */
    protected RecordsManagementAuditService rmAuditService;
    
    /** File plan service */
    protected FilePlanService filePlanService;
    
    /**
     * Sets the RecordsManagementAuditService instance
     * 
     * @param rmAuditService The RecordsManagementAuditService instance
     */
    public void setRecordsManagementAuditService(RecordsManagementAuditService rmAuditService)
    {
        this.rmAuditService = rmAuditService;
    }
    
    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService) 
    {
		this.filePlanService = filePlanService;
	}
    
    /**
     * Creates a model to represent the current status of the RM audit log.
     * 
     * @return Map of RM audit log status
     */
    protected Map<String, Object> createAuditStatusModel()
    {
        Map<String, Object> auditStatus = new HashMap<>(3);
        
        auditStatus.put("started", ISO8601DateFormat.format(rmAuditService.getDateAuditLogLastStarted(getDefaultFilePlan())));
        auditStatus.put("stopped", ISO8601DateFormat.format(rmAuditService.getDateAuditLogLastStopped(getDefaultFilePlan())));
        auditStatus.put("enabled", Boolean.valueOf(rmAuditService.isAuditLogEnabled(getDefaultFilePlan())));
        
        return auditStatus;
    }
    
    /**
     * Helper method to get default file plan.
     * 
     * @return	NodeRef	default file plan
     */
    protected NodeRef getDefaultFilePlan()
    {
    	NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
    	if (filePlan == null)
    	{
    		throw new AlfrescoRuntimeException("Default file plan not found.");
    	}
    	return filePlan;
    }
}
