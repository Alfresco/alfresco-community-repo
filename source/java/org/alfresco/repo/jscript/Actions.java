/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
