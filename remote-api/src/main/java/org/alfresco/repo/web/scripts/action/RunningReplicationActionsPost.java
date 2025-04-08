/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts.action;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;

/**
 * @author Nick Burch
 * @since 3.4
 */
public class RunningReplicationActionsPost extends AbstractExecuteActionWebscript
{
    private ReplicationService replicationService;

    @Override
    protected Action identifyAction(WebScriptRequest req, Status status,
            Cache cache)
    {
        // Which action did they ask for?
        String name = req.getParameter("name");
        if (name == null)
        {
            try
            {
                JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
                if (!json.has("name"))
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not find required 'name' parameter");
                }
                name = json.getString("name");
            }
            catch (IOException iox)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from request.", iox);
            }
            catch (JSONException je)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from request.", je);
            }
        }

        // Load the specified replication definition
        ReplicationDefinition replicationDefinition = replicationService.loadReplicationDefinition(name);
        return replicationDefinition;
    }

    public void setReplicationService(ReplicationService replicationService)
    {
        this.replicationService = replicationService;
    }
}
