package org.alfresco.repo.web.scripts.admin;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsage.UsageType;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * GET the repository {@link RepoUsage usage}.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class RepoUsagePost extends AbstractAdminWebScript
{
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(7);
        
        boolean updated = repoAdminService.updateUsage(UsageType.USAGE_ALL);
        RepoUsage repoUsage = repoAdminService.getUsage();
        
        putUsageInModel(model, repoUsage, updated);
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        return model;
    }
}