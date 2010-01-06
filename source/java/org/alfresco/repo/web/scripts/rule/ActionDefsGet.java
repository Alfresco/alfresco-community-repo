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
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Web Script to GET the action definition collection
 * This can optionally be scoped by by either
 *     - a node reference in the form /api/node/{store_type}/{store_id}/{id} on the URL  
 *     - a node path in the form /api/path/{store_type}/{store_id}/{id} on the URL
 * 
 * @author glen johnson at alfresco dot com
 */
public class ActionDefsGet extends DeclarativeWebScript
{
    // private constants
    private static final String REQ_URL_TEMPL_VAR_STORE_TYPE = "store_type";
    private static final String REQ_URL_TEMPL_VAR_STORE_ID = "store_id";
    private static final String REQ_URL_TEMPL_VAR_ID = "id";
    
    // model property keys
    private static final String MODEL_PROP_KEY_ACTION_DEFS = "actiondefs";
    
    // properties for services & dependencies
    private ActionService actionService;
    private RulesHelper rulesHelper;

    /**
     * Set the actionService property.
     * 
     * @param actionService The action service instance to set
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }
    
    /**
     * Set the rules helper
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
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,
            Status status)
    {
        // initialise model to pass on for template to render
        Map<String, Object> model = new HashMap<String, Object>();
        
        // get the template variables for store_type, store_id and id
        String storeType = req.getServiceMatch().getTemplateVars().get(REQ_URL_TEMPL_VAR_STORE_TYPE);
        String storeId = req.getServiceMatch().getTemplateVars().get(REQ_URL_TEMPL_VAR_STORE_ID);
        String id = req.getServiceMatch().getTemplateVars().get(REQ_URL_TEMPL_VAR_ID);
        
        // work out which of storeType, storeId, id template variables have been given
        boolean storeTypeGiven = (storeType != null) && (storeType.length() > 0);
        boolean storeIdGiven = (storeId != null) && (storeId.length() > 0);
        boolean idGiven = (id != null) && (id.length() > 0);

        List<ActionDefinition> actionDefs = null;
        
        //
        // if either a node reference or path are provided to scope the action 
        // definition collection by, then obtain a reference to that node
        // 
        if ((storeTypeGiven && storeIdGiven && idGiven))
        {
            // get the node reference to scope the action definitions by
            NodeRef scopeByNodeRef = this.rulesHelper.getNodeRefFromWebScriptUrl(req, storeType, storeId, id);
            
            // get all the action definitions that are applicable for the node reference
            actionDefs = this.actionService.getActionDefinitions(scopeByNodeRef);
        }
        // else not scoped by noderef, so get all the condition definitions
        else
        {
            actionDefs = this.actionService.getActionDefinitions();
        }
        
        // add objects to model for the template to render
        model.put(MODEL_PROP_KEY_ACTION_DEFS, actionDefs);
        
        return model;
    }        
}
