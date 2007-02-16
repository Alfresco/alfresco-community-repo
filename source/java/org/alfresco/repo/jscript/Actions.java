/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.jscript;

import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.mozilla.javascript.Scriptable;

/**
 * Scripted Action service for describing and executing actions against Nodes.
 * 
 * @author davidc
 */
public final class Actions extends BaseScriptImplementation implements Scopeable
{
    /** Repository Service Registry */
    private ServiceRegistry services;

    /** Root scope for this object */
    private Scriptable scope;

    /**
     * Set the service registry
     * 
     * @param serviceRegistry	the service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.services = serviceRegistry;
    }

    /**
     * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
     */
    public void setScope(Scriptable scope)
    {
        this.scope = scope;
    }
    
    /**
     * Gets the list of registered action names
     * 
     * @return the registered action names
     */
    public String[] getRegistered()
    {
        ActionService actionService = services.getActionService();
        List<ActionDefinition> defs = actionService.getActionDefinitions();
        String[] registered = new String[defs.size()];
        int i = 0;
        for (ActionDefinition def : defs)
        {
            registered[i++] = def.getName();
        }
        return registered;
    }

    public String[] jsGet_registered()
    {
        return getRegistered();
    }

    /**
     * Create an Action
     * 
     * @param actionName
     *            the action name
     * @return the action
     */
    public ScriptAction create(String actionName)
    {
        ScriptAction scriptAction = null;
        ActionService actionService = services.getActionService();
        ActionDefinition actionDef = actionService.getActionDefinition(actionName);
        if (actionDef != null)
        {
            Action action = actionService.createAction(actionName);
            scriptAction = new ScriptAction(this.services, action, actionDef);
            scriptAction.setScope(scope);
        }
        return scriptAction;
    } 

}
