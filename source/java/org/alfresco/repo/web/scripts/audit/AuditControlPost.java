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

import java.io.IOException;

import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * @author Derek Hulley
 * @since 3.4
 */
public class AuditControlPost extends AbstractAuditWebScript
{
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        String app = getApp(req, false);
        String path = getPath(req);
        
        boolean enable = getEnableDisable(req);
        
        if (app == null)
        {
            // Global operation
            auditService.setAuditEnabled(enable);
        }
        else
        {
            // Apply to a specific application
            auditService.enableAudit(app, path);
        }

//        res.setContentType("application/json");
//        res.setContentEncoding(Charset.defaultCharset().displayName());     // TODO: Should be settable on JSONWriter
        //        res.addHeader("Content-Length", "" + length);             // TODO: Do we need this?
        res.setStatus(Status.STATUS_OK);
    }
}