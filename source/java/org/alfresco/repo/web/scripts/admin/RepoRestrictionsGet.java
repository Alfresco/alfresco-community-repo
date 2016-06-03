package org.alfresco.repo.web.scripts.admin;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.admin.RepoUsage;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * GET the repository {@link RepoUsage restrictions}.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class RepoRestrictionsGet extends AbstractAdminWebScript
{
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(7);
        
        RepoUsage restrictions = repoAdminService.getRestrictions();
        putUsageInModel(model, restrictions, false);
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        return model;
    }
}