package org.alfresco.repo.web.scripts.tenant;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST API - delete tenant
 * 
 * @author janv
 * @since 4.2
 */
public class TenantDelete extends AbstractTenantAdminWebScript
{
    protected static final Log logger = LogFactory.getLog(TenantPost.class);
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // get request parameters
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String tenantDomain = templateVars.get("tenantDomain");
        
        tenantAdminService.deleteTenant(tenantDomain);
        
        Map<String, Object> model = new HashMap<String, Object>(0);
        return model;
    }
}
