 
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
     * @param auditService The RecordsManagementAuditService instance
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
        Map<String, Object> auditStatus = new HashMap<String, Object>(3);
        
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