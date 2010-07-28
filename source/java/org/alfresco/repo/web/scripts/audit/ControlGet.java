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
import java.io.StringWriter;

import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.json.JSONUtils;
import org.springframework.extensions.webscripts.json.JSONWriter;

/**
 * @author Derek Hulley
 * @since 3.4
 */
public class ControlGet extends AbstractAuditWebScript
{
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        String app = getApp(req, false);
        String path = getPath(req);
        
        boolean enabled = false;
        if (app == null)
        {
            enabled = auditService.isAuditEnabled();
        }
        else
        {
            enabled = auditService.isAuditEnabled(app, path);
        }
        
        // return the unique transfer id (the lock id)
        StringWriter stringWriter = new StringWriter(300);
        JSONWriter jsonWriter = new JSONWriter(stringWriter);
        jsonWriter.startObject();
        jsonWriter.writeValue("app", app);
        jsonWriter.writeValue("path", path);
        jsonWriter.writeValue("enabled", enabled);
        jsonWriter.endObject();
        String response = stringWriter.toString();
        
        res.setContentType("application/json");
        res.setContentEncoding("UTF-8");
        int length = response.getBytes("UTF-8").length;
        res.addHeader("Content-Length", "" + length);
        res.setStatus(Status.STATUS_OK);
        res.getWriter().write(response);
    }
}