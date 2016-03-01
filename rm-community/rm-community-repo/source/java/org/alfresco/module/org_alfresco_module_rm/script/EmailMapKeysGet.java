 
package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.email.CustomEmailMappingService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to return email mapping keys
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class EmailMapKeysGet extends DeclarativeWebScript
{
    /** Custom email mapping service */
    private CustomEmailMappingService customEmailMappingService;

    /**
     * Custom email mapping service
     *
     * @param customEmailMappingService the custom email mapping service
     */
    public void setCustomEmailMappingService(CustomEmailMappingService customEmailMappingService)
    {
        this.customEmailMappingService = customEmailMappingService;
    }

    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // Create model object with the lists of email mapping keys
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("emailmapkeys", customEmailMappingService.getEmailMappingKeys());
        return model;
    }
}
