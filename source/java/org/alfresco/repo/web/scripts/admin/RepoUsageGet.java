package org.alfresco.repo.web.scripts.admin;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsageStatus;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * GET the repository {@link RepoUsage usage}.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class RepoUsageGet extends AbstractAdminWebScript
{
    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache)
    {
        // Runas system to obtain the info
        RunAsWork<Map<String, Object>> runAs = new RunAsWork<Map<String,Object>>()
        {
            @Override
            public Map<String, Object> doWork() throws Exception
            {
                Map<String, Object> model = new HashMap<String, Object>(7);
                
                RepoUsageStatus usageStatus = repoAdminService.getUsageStatus();
                RepoUsage usage = usageStatus.getUsage();
                
                putUsageInModel(
                        model,
                        usage,
                        false);
              
                // Add usage messages
                model.put(JSON_KEY_LEVEL, usageStatus.getLevel().ordinal());
                model.put(JSON_KEY_WARNINGS, usageStatus.getWarnings());
                model.put(JSON_KEY_ERRORS, usageStatus.getErrors());
              
                // Done
                if (logger.isDebugEnabled())
                {
                    logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
                }
                return model;
            }
        };
        return AuthenticationUtil.runAs(runAs, AuthenticationUtil.getSystemUserName());
    }
}