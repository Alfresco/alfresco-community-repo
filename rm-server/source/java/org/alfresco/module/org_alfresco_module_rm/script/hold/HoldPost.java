/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.script.hold;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to add an item to the given hold in the hold container.
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class HoldPost extends DeclarativeWebScript
{
    /** Hold Service */
    private HoldService holdService;

    /**
     * Set the hold service
     *
     * @param holdService the hold service
     */
    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        NodeRef nodeRef = null;
        List<NodeRef> holds = new ArrayList<NodeRef>();
        try
        {
            String content = req.getContent().getContent();
            JSONObject json = new JSONObject(new JSONTokener(content));
            nodeRef = getItemNodeRef(json);
            holds.addAll(getHolds(json));
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

        holdService.addToHoldContainers(holds, nodeRef);

        return new HashMap<String, Object>();
    }

    /**
     * Helper method to get the {@link NodeRef} for the item (record / record folder) which will be added to the hold(s)
     *
     * @param json The request content as JSON object
     * @return The {@link NodeRef} of the item which will be added to the hold(s)
     * @throws JSONException If there is no string value for the key
     */
    private NodeRef getItemNodeRef(JSONObject json) throws JSONException
    {
        String nodeRef = json.getString("nodeRef");
        return new NodeRef(nodeRef);
    }

    /**
     * Helper method to get the list of {@link NodeRef}(s) for the hold(s) which will contain the item (record / record folder)
     *
     * @param json The request content as JSON object
     * @return List of {@link NodeRef}(s) of the hold(s)
     * @throws JSONException If there is no string value for the key
     */
    private List<NodeRef> getHolds(JSONObject json) throws JSONException
    {
        JSONArray holdsArray = json.getJSONArray("holds");
        List<NodeRef> holds = new ArrayList<NodeRef>();
        for (int i = 0; i < holdsArray.length(); i++)
        {
            String nodeRef = holdsArray.getString(i);
            holds.add(new NodeRef(nodeRef));
        }
        return holds;
    }
}
