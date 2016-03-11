package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * GET audit log status
 * 
 * @author Roy Wetherall
 */
public class AuditLogStatusGet extends DeclarativeWebScript
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
     * @see org.alfresco.repo.web.scripts.content.StreamContent#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("enabled", Boolean.valueOf(rmAuditService.isAuditLogEnabled(getDefaultFilePlan())));
        return model;
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
