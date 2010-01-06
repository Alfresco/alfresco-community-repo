/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.rule;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Web Script to POST an Action Queue Item onto the Action Queue
 * 
 * @author glen johnson at alfresco dot com
 */
public class ActionQueuePost extends DeclarativeWebScript
{
    // model property keys
    private static final String MODEL_PROP_KEY_ACTION_Q_ITEM_STATUS = "actionQItemStatus";
    
    // properties for services
    private ActionService actionService;

    // properties for dependencies
    private RulesHelper rulesHelper;

    /**
     * @param actionService the actionService to set
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }        
    
    /**
     * Set the rules helper property
     * 
     * @param rulesHelper the rulesHelper to set
     */
    public void setRulesHelper(RulesHelper rulesHelper)
    {
        this.rulesHelper = rulesHelper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(
     *      org.alfresco.web.scripts.WebScriptRequest,
     *      org.alfresco.web.scripts.WebScriptResponse)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,
            Status status)
    {
        // initialise model to pass on to template for rendering
        Map<String, Object> model = new HashMap<String, Object>();
        
        // get the posted action queue item JSON object by parsing request content
        Object contentObj = req.parseContent();
        if (contentObj == null || !(contentObj instanceof JSONObject))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Web Script request content must be a JSON Object. "
                    + "Request content is either a JSON Array or not of MIME-type application/json");
        }
        
        JSONObject actionJson = null;
        Action action = null;
        boolean checkConditions = true;
        boolean executeAsynchronously = false;
        NodeRef actionedUponNodeRef = null;
        try
        {
            // get action queue item JSON object cast from contentObj
            JSONObject actionQueueItemJson = (JSONObject)contentObj;
            
            // get action JSON object from actionQueueItem JSON
            actionJson = actionQueueItemJson.getJSONObject("action");
            
            // get the action from the action JSON object
            action = this.rulesHelper.getActionFromJson(actionJson, null);
                
            // Get 'checkConditions' and 'executeAsynchronously' properties off action queue item
            checkConditions = actionQueueItemJson.optBoolean("checkConditions", true);
            executeAsynchronously = actionQueueItemJson.optBoolean(
                    "executeAsync", action.getExecuteAsychronously());
            
            // get the actioned-upon node reference 
            String nodeRefStr = actionQueueItemJson.getString("nodeRef"); 
            actionedUponNodeRef = new NodeRef(nodeRefStr);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Problem retrieving properties from ActionQueueItem Details sent in the request content.", je); 
        }
        
        // apply rule to actionable node
        this.actionService.executeAction(action, actionedUponNodeRef, checkConditions, executeAsynchronously);
        
        // create the action queue item status bean
        // to be rendered by the template
        ActionQueueItemStatus actionQItemStatus = new ActionQueueItemStatus();
        
        //
        // set the action queue item id on the action queue item status bean
        //
        
        String actionedUponNodeId = actionedUponNodeRef.getId();
        String actionId = action.getId();
        String actionQItemId = actionId + ":" + actionedUponNodeId;
        actionQItemStatus.setActionQueueItemId(actionQItemId);
        
        // set the action id on the action queue item status bean
        actionQItemStatus.setActionId(actionId);
        
        // set the status on the action queue item status bean
        if (executeAsynchronously == true)
        {
            actionQItemStatus.setStatus(RulesHelper.ACTION_QUEUE_ITEM_STATUS_PENDING);
        }
        else
        {
            actionQItemStatus.setStatus(RulesHelper.ACTION_QUEUE_ITEM_STATUS_COMPLETE);
        }
        
        // add objects to model for the template to render
        model.put(MODEL_PROP_KEY_ACTION_Q_ITEM_STATUS, actionQItemStatus);
        
        return model;
    }
}
