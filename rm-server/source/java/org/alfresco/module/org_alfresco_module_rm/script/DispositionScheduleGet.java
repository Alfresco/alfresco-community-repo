/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to return full details
 * about a disposition schedule.
 * 
 * @author Gavin Cornwell
 */
public class DispositionScheduleGet extends DispositionAbstractBase
{
    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // parse the request to retrieve the schedule object
        DispositionSchedule schedule = parseRequestForSchedule(req);

        // add all the schedule data to Map
        Map<String, Object> scheduleModel = new HashMap<String, Object>(8);
        
        // build url
        String serviceUrl = req.getServiceContextPath() + req.getPathInfo();
        scheduleModel.put("url", serviceUrl);
        String actionsUrl = serviceUrl + "/dispositionactiondefinitions";
        scheduleModel.put("actionsUrl", actionsUrl);
        scheduleModel.put("nodeRef", schedule.getNodeRef().toString());
        scheduleModel.put("recordLevelDisposition", schedule.isRecordLevelDisposition());
        scheduleModel.put("canStepsBeRemoved", 
                    !this.dispositionService.hasDisposableItems(schedule));
        
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
        
        List<Map<String, Object>> actions = new ArrayList<Map<String, Object>>();
        for (DispositionActionDefinition actionDef : schedule.getDispositionActionDefinitions())
        {
            NodeRef actionDefNodeRef = actionDef.getNodeRef();
            if (nodeService.hasAspect(actionDefNodeRef, RecordsManagementModel.ASPECT_UNPUBLISHED_UPDATE) == true)
            {
                unpublishedUpdates = true;
                publishInProgress = ((Boolean)nodeService.getProperty(actionDefNodeRef, RecordsManagementModel.PROP_PUBLISH_IN_PROGRESS)).booleanValue();
            }
            
            actions.add(createActionDefModel(actionDef, actionsUrl + "/" + actionDef.getId()));
        }
        scheduleModel.put("actions", actions);                
        scheduleModel.put("unpublishedUpdates", unpublishedUpdates);
        scheduleModel.put("publishInProgress", publishInProgress);
        
        // create model object with just the schedule data
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("schedule", scheduleModel);
        return model;
    }
}