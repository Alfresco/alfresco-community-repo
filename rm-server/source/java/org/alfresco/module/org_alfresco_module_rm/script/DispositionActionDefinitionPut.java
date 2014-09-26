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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to update an existing dispositon
 * action definition.
 *
 * @author Gavin Cornwell
 */
public class DispositionActionDefinitionPut extends DispositionAbstractBase
{
    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // parse the request to retrieve the schedule object
        DispositionSchedule schedule = parseRequestForSchedule(req);

        // parse the request to retrieve the action definition object
        DispositionActionDefinition actionDef = parseRequestForActionDefinition(req, schedule);

        // retrieve the rest of the post body and update the action definition
        JSONObject json = null;
        try
        {
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            actionDef = updateActionDefinition(actionDef, json);
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

        // create model object with just the action data
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("action", createActionDefModel(actionDef, req.getURL()));
        return model;
    }

    /**
     * Updates a dispositionActionDefinition node in the repo.
     *
     * @param actionDef The action definition to update
     * @param json The JSON to use to create the action definition
     * @param schedule The DispositionSchedule the action definition belongs to
     * @return The updated DispositionActionDefinition
     */
    protected DispositionActionDefinition updateActionDefinition(DispositionActionDefinition actionDef,
              JSONObject json) throws JSONException
    {
        // create the properties for the action definition
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(8);

        if (json.has("name"))
        {
            props.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, json.getString("name"));
        }

        if (json.has("description"))
        {
            props.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, json.getString("description"));
        }

        if (json.has("period"))
        {
            props.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, json.getString("period"));
        }

        if (json.has("periodProperty"))
        {
            QName periodProperty = QName.createQName(json.getString("periodProperty"), getNamespaceService());
            props.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD_PROPERTY, periodProperty);
        }

        if (json.has("eligibleOnFirstCompleteEvent"))
        {
            props.put(RecordsManagementModel.PROP_DISPOSITION_EVENT_COMBINATION,
                        json.getBoolean("eligibleOnFirstCompleteEvent") ? "or" : "and");
        }

        if (json.has("location"))
        {
            props.put(RecordsManagementModel.PROP_DISPOSITION_LOCATION,
                      json.getString("location"));
        }

        if (json.has("events"))
        {
            JSONArray events = json.getJSONArray("events");
            List<String> eventsList = new ArrayList<String>(events.length());
            for (int x = 0; x < events.length(); x++)
            {
                eventsList.add(events.getString(x));
            }
            props.put(RecordsManagementModel.PROP_DISPOSITION_EVENT, (Serializable)eventsList);
        }

        if (json.has("name") && "destroy".equals(json.getString("name")))
        {
            if (json.has("ghostOnDestroy"))
            {
                props.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_GHOST_ON_DESTROY, "ghost");
            }
            else
            {
                props.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_GHOST_ON_DESTROY, "delete");
            }
        }

        // update the action definition
        return getDispositionService().updateDispositionActionDefinition(actionDef, props);
    }
}