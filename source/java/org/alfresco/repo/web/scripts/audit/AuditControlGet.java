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
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;

import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.json.JSONWriter;

/**
 * @author Derek Hulley
 * @since 3.4
 */
public class AuditControlGet extends AbstractAuditWebScript
{
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        // return the unique transfer id (the lock id)
        JSONWriter json = new JSONWriter(res.getWriter());

        String app = getApp(req, false);
        String path = getPath(req);
        Set<String> apps = auditService.getAuditApplications();
        
        // Check that the application exists
        if (app != null)
        {
            if (apps.contains(app))
            {
                apps = Collections.singleton(app);
            }
            else
            {
                apps = Collections.emptySet();
            }
        }
        
        boolean enabledGlobal = auditService.isAuditEnabled();
        json.startObject();
        {
            json.writeValue(JSON_KEY_ENABLED, enabledGlobal);
            json.startValue(JSON_KEY_APPLICATIONS);
            {
                json.startArray();
                {
                    for (String appName : apps)
                    {
                        boolean enabled = auditService.isAuditEnabled(appName, path);
                        json.startObject();
                        {
                            json.writeValue(JSON_KEY_NAME, appName);
                            json.writeValue(JSON_KEY_PATH, path);
                            json.writeValue(JSON_KEY_ENABLED, enabled);
                        }
                        json.endObject();
                    }
                }
                json.endArray();
            }
            json.endValue();
        }
        json.endObject();
        
        // Close off
        res.getWriter().close();

        res.setContentType("application/json");
        res.setContentEncoding(Charset.defaultCharset().displayName());     // TODO: Should be settable on JSONWriter
        //        res.addHeader("Content-Length", "" + length);             // TODO: Do we need this?
        res.setStatus(Status.STATUS_OK);
    }
}