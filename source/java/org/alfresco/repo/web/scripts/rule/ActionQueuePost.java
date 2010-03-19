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
package org.alfresco.repo.web.scripts.rule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 *
 */
public class ActionQueuePost extends AbstractRuleWebScript
{
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(ActionQueuePost.class);

    public static final String STATUS = "actionExecStatus";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";
    public static final String STATUS_QUEUED = "queued";

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        // get request parameters
        boolean async = Boolean.parseBoolean(req.getParameter("async"));

        ActionImpl action = null;
        JSONObject json = null;

        try
        {
            // read request json
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));

            // parse request json
            action = parseJsonAction(json);
            NodeRef actionedUponNode = action.getNodeRef();

            // clear nodeRef for action
            action.setNodeRef(null);
            json.remove("actionedUponNode");

            if (async)
            {
                model.put(STATUS, STATUS_QUEUED);
            }
            else
            {
                model.put(STATUS, STATUS_SUCCESS);
            }

            // Execute action
            actionService.executeAction(action, actionedUponNode, true, async);

            // Prepair model
            model.put("actionedUponNode", actionedUponNode.toString());
            model.put("action", json);
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from req.", je);
        }

        return model;
    }
}
