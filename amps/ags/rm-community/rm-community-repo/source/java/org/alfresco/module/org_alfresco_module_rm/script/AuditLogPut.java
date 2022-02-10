/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to start
 * and stop Records Management auditing.
 * 
 * @author Gavin Cornwell
 */
public class AuditLogPut extends BaseAuditAdminWebScript
{
    protected static final String PARAM_ENABLED = "enabled";
    
    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        try
        {
            // determine whether to start or stop auditing
            JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            
            // check the enabled property present
            if (!json.has(PARAM_ENABLED))
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Mandatory 'enabled' parameter was not provided in request body");
            }
            
            boolean enabled = json.getBoolean(PARAM_ENABLED);
            if (enabled)
            {
                this.rmAuditService.startAuditLog(getDefaultFilePlan());
            }
            else
            {
                this.rmAuditService.stopAuditLog(getDefaultFilePlan());
            }
            
            // create model object with the audit status model
            Map<String, Object> model = new HashMap<>(1);
            model.put("auditstatus", createAuditStatusModel());
            return model;
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Could not parse JSON from req.", je);
        }
    }
}
