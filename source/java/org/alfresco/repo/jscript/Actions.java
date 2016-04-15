/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.jscript;

import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;

/**
 * Scripted Action service for describing and executing actions against Nodes.
 * 
 * @author davidc
 */
public final class Actions extends BaseScopableProcessorExtension
{
    /** Repository Service Registry */
    private ServiceRegistry services;

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
            scriptAction.setScope(getScope());
        }
        return scriptAction;
    } 
}
