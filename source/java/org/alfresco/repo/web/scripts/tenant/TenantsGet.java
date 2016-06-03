package org.alfresco.repo.web.scripts.tenant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.tenant.Tenant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST API - get tenants
 * 
 * TODO filter params - eg. enabled/ disabled and name startsWith
 * 
 * @author janv
 * @since 4.2
 */
public class TenantsGet extends AbstractTenantAdminWebScript
{
    protected static final Log logger = LogFactory.getLog(TenantsGet.class);
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        List<Tenant> tenants = tenantAdminService.getAllTenants();
        
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("tenants", tenants);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        
        return model;
    }
}
