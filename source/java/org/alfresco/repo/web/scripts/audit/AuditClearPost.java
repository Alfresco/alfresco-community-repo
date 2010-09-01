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
public class AuditClearPost extends AbstractAuditWebScript
{
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(7);
        
        String appName = getParamAppName(req);
        if (appName == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "audit.err.app.notProvided");
        }
        AuditApplication app = auditService.getAuditApplications().get(appName);
        if (app == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "audit.err.app.notFound", appName);
        }
        // Get from/to times
        Long fromTime = getParamFromTime(req);           // might be null
        Long toTime = getParamToTime(req);               // might be null
        
        // Clear
        int cleared = auditService.clearAudit(appName, fromTime, toTime);
        
        model.put(JSON_KEY_CLEARED, cleared);
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        return model;
    }
}