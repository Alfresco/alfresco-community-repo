package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.caveat.RMCaveatConfigService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to return 
 * the values for an RM constraint.
 */
public class RMConstraintGet extends DeclarativeWebScript
{   
    
    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {     
        String extensionPath = req.getExtensionPath();
        
        String constraintName = extensionPath.replace('_', ':');
        
        List<String> values = caveatConfigService.getRMAllowedValues(constraintName);
        
        // create model object with the lists model
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("allowedValuesForCurrentUser", values);
        model.put("constraintName", extensionPath);

        return model;
    }
 
    public void setCaveatConfigService(RMCaveatConfigService caveatConfigService)
    {
        this.caveatConfigService = caveatConfigService;
    }

    public RMCaveatConfigService getCaveatConfigService()
    {
        return caveatConfigService;
    }

    private RMCaveatConfigService caveatConfigService;
     
}
