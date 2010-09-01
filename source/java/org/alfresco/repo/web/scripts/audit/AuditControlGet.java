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
package org.alfresco.repo.web.scripts.audit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.audit.AuditService.AuditApplication;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Derek Hulley
 * @since 3.4
 */
public class AuditControlGet extends AbstractAuditWebScript
{
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(7);
        
        String appName = getParamAppName(req);
        String path = getParamPath(req);
        boolean enabledGlobal = auditService.isAuditEnabled();
        Map<String, AuditApplication> appsByName = auditService.getAuditApplications();
        
        // Check that the application exists
        if (appName != null)
        {
            if (path == null)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "audit.err.path.notProvided");
            }
            
            AuditApplication app = appsByName.get(appName);
            if (app == null)
            {
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "audit.err.app.notFound", appName);
            }
            // Discard all the other applications
            appsByName = Collections.singletonMap(appName, app);
        }
        
        model.put(JSON_KEY_ENABLED, enabledGlobal);
        model.put(JSON_KEY_APPLICATIONS, appsByName.values());
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        return model;
    }
}