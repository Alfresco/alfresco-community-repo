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

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_COMBINE_DISPOSITION_STEP_CONDITIONS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Abstract base class for all disposition related java backed webscripts.
 *
 * @author Gavin Cornwell
 */
public class DispositionAbstractBase extends AbstractRmWebScript
{

    public final static String COMBINE_DISPOSITION_STEP_CONDITIONS =  "combineDispositionStepConditions";
    /**
     * Parses the request and providing it's valid returns the DispositionSchedule object.
     *
     * @param req The webscript request
     * @return The DispositionSchedule object the request is aimed at
     */
    protected DispositionSchedule parseRequestForSchedule(WebScriptRequest req)
    {
        // get the NodeRef from the request
        NodeRef nodeRef = parseRequestForNodeRef(req);

        // Determine whether we are getting the inherited disposition schedule or not
        boolean inherited = true;
        String inheritedString = req.getParameter("inherited");
        if (inheritedString != null)
        {
            inherited = Boolean.parseBoolean(inheritedString);
        }

        // make sure the node passed in has a disposition schedule attached
        DispositionSchedule schedule = null;
        if (inherited)
        {
            schedule = getDispositionService().getDispositionSchedule(nodeRef);
        }
        else
        {
            schedule = getDispositionService().getAssociatedDispositionSchedule(nodeRef);
        }
        if (schedule == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Node " +
                        nodeRef.toString() + " does not have a disposition schedule");
        }

        return schedule;
    }

    /**
     * Parses the request and providing it's valid returns the DispositionActionDefinition object.
     *
     * @param req The webscript request
     * @param schedule The disposition schedule
     * @return The DispositionActionDefinition object the request is aimed at
     */
    protected DispositionActionDefinition parseRequestForActionDefinition(WebScriptRequest req,
              DispositionSchedule schedule)
    {
        // make sure the requested action definition exists
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String actionDefId = templateVars.get("action_def_id");
        DispositionActionDefinition actionDef = schedule.getDispositionActionDefinition(actionDefId);
        if (actionDef == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND,
                        "Requested disposition action definition (id:" + actionDefId + ") does not exist");
        }

        return actionDef;
    }

    /**
     * Helper to create a model to represent the given disposition action definition.
     *
     * @param actionDef The DispositionActionDefinition instance to generate model for
     * @param url The URL for the DispositionActionDefinition
     * @return Map representing the model
     */
    protected Map<String, Object> createActionDefModel(DispositionActionDefinition actionDef,
                String url)
    {
        Map<String, Object> model = new HashMap<>(8);

        model.put("id", actionDef.getId());
        model.put("index", actionDef.getIndex());
        model.put("url", url);
        model.put("name", actionDef.getName());
        model.put("label", actionDef.getLabel());
        model.put("eligibleOnFirstCompleteEvent", actionDef.eligibleOnFirstCompleteEvent());

        if (actionDef.getDescription() != null)
        {
            model.put("description", actionDef.getDescription());
        }

        if (actionDef.getPeriod() != null)
        {
            model.put("period", actionDef.getPeriod().toString());
        }

        if (actionDef.getPeriodProperty() != null)
        {
            model.put("periodProperty", actionDef.getPeriodProperty().toPrefixString(getNamespaceService()));
        }

        if (actionDef.getLocation() != null)
        {
            model.put("location", actionDef.getLocation());
        }

        if (actionDef.getGhostOnDestroy() != null)
        {
            model.put("ghostOnDestroy", actionDef.getGhostOnDestroy());
        }

        List<RecordsManagementEvent> events = actionDef.getEvents();
        if (events != null && events.size() > 0)
        {
            List<String> eventNames = new ArrayList<>(events.size());
            for (RecordsManagementEvent event : events)
            {
                eventNames.add(event.getName());
            }
            model.put("events", eventNames);
        }

        if(getNodeService().getProperty(actionDef.getNodeRef(), PROP_COMBINE_DISPOSITION_STEP_CONDITIONS) != null)
        {
            model.put("combineDispositionStepConditions", getNodeService().getProperty(actionDef.getNodeRef(), PROP_COMBINE_DISPOSITION_STEP_CONDITIONS));
        }

        return model;
    }

    /**
     * Helper method to parse the request and retrieve the disposition schedule model.
     *
     * @param req The webscript request
     * @return Map representing the model
     */
    protected Map<String, Object> getDispositionScheduleModel(WebScriptRequest req)
    {
        // parse the request to retrieve the schedule object
        DispositionSchedule schedule = parseRequestForSchedule(req);

        // add all the schedule data to Map
        Map<String, Object> scheduleModel = new HashMap<>(8);

        // build url
        String serviceUrl = req.getServiceContextPath() + req.getPathInfo();
        scheduleModel.put("url", serviceUrl);
        String actionsUrl = serviceUrl + "/dispositionactiondefinitions";
        scheduleModel.put("actionsUrl", actionsUrl);
        scheduleModel.put("nodeRef", schedule.getNodeRef().toString());
        scheduleModel.put("recordLevelDisposition", schedule.isRecordLevelDisposition());
        scheduleModel.put("canStepsBeRemoved",
                    !getDispositionService().hasDisposableItems(schedule));

        if (schedule.getDispositionAuthority() != null)
        {
            scheduleModel.put("authority", schedule.getDispositionAuthority());
        }

        if (schedule.getDispositionInstructions() != null)
        {
            scheduleModel.put("instructions", schedule.getDispositionInstructions());
        }

        boolean unpublishedUpdates = false;
        boolean publishInProgress = false;

        List<Map<String, Object>> actions = new ArrayList<>();
        for (DispositionActionDefinition actionDef : schedule.getDispositionActionDefinitions())
        {
            NodeRef actionDefNodeRef = actionDef.getNodeRef();
            if (getNodeService().hasAspect(actionDefNodeRef, RecordsManagementModel.ASPECT_UNPUBLISHED_UPDATE))
            {
                unpublishedUpdates = true;
                publishInProgress = ((Boolean) getNodeService().getProperty(actionDefNodeRef, RecordsManagementModel.PROP_PUBLISH_IN_PROGRESS)).booleanValue();
            }

            actions.add(createActionDefModel(actionDef, actionsUrl + "/" + actionDef.getId()));
        }
        scheduleModel.put("actions", actions);
        scheduleModel.put("unpublishedUpdates", unpublishedUpdates);
        scheduleModel.put("publishInProgress", publishInProgress);

        // create model object with just the schedule data
        Map<String, Object> model = new HashMap<>(1);
        model.put("schedule", scheduleModel);
        return model;
    }
}
