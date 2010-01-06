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

import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Web Script to GET an action definition given its name
 * 
 * @author glen johnson at alfresco dot com
 */
public class ActionDefGet extends DeclarativeWebScript
{
    // model property keys
    private static final String MODEL_PROP_KEY_ACTION_DEF = "actiondef";
    
    // private constants
    private static final String REQ_PARAM_ACTION_DEF_NAME = "actionDefinitionName";
    
    // properties for services
    private ActionService actionService;

    /**
     * Set the actionService property.
     * 
     * @param actionService The action service instance to set
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
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
        
        // get actionDefinitionName URL template variable 
        String actionDefName = req.getServiceMatch().getTemplateVars().get(REQ_PARAM_ACTION_DEF_NAME);
        if ((actionDefName == null) || (actionDefName.length() == 0))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "The 'actionDefinitionName' URL parameter has not been provided in URL");
        }                        
        
        // get an action definition with the given action definition name
        ActionDefinition actionDef = this.actionService.getActionDefinition(actionDefName);
        
        // add objects to model for the template to render
        model.put(MODEL_PROP_KEY_ACTION_DEF, actionDef);
        
        return model;
    }        
}
