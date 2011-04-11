/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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