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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.action;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionServiceTransport;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;

/**
 * Server side implementation for transport of ActionService.
 * @author britt
 */
public class ActionServiceTransportImpl implements ActionServiceTransport
{
    private ActionService fActionService;
    
    private AuthenticationService fAuthenticationService;
    
    public void setActionService(ActionService service)
    {
        fActionService = service;
    }
    
    public void setAuthenticationService(AuthenticationService service)
    {
        fAuthenticationService = service;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#createAction(java.lang.String, java.lang.String)
     */
    public Action createAction(String ticket, String name)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.createAction(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#createAction(java.lang.String, java.lang.String, java.util.Map)
     */
    public Action createAction(String ticket, String name,
            Map<String, Serializable> params)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.createAction(name, params);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#createActionCondition(java.lang.String, java.lang.String)
     */
    public ActionCondition createActionCondition(String ticket, String name)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.createActionCondition(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#createActionCondition(java.lang.String, java.lang.String, java.util.Map)
     */
    public ActionCondition createActionCondition(String ticket, String name,
            Map<String, Serializable> params)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.createActionCondition(name, params);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#createCompositeAction(java.lang.String)
     */
    public CompositeAction createCompositeAction(String ticket)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.createCompositeAction();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#evaluateAction(java.lang.String, org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateAction(String ticket, Action action,
            NodeRef actionedUponNodeRef)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.evaluateAction(action, actionedUponNodeRef);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#evaluateActionCondition(java.lang.String, org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateActionCondition(String ticket,
            ActionCondition condition, NodeRef actionedUponNodeRef)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.evaluateActionCondition(condition, actionedUponNodeRef);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#executeAction(java.lang.String, org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void executeAction(String ticket, Action action,
            NodeRef actionedUponNodeRef)
    {
        fAuthenticationService.validate(ticket);
        fActionService.executeAction(action, actionedUponNodeRef);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#executeAction(java.lang.String, org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public void executeAction(String ticket, Action action,
            NodeRef actionedUponNodeRef, boolean checkConditions)
    {
        fAuthenticationService.validate(ticket);
        fActionService.executeAction(action, actionedUponNodeRef, checkConditions);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#executeAction(java.lang.String, org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef, boolean, boolean)
     */
    public void executeAction(String ticket, Action action,
            NodeRef actionedUponNodeRef, boolean checkConditions,
            boolean executeAsynchronously)
    {
        fAuthenticationService.validate(ticket);
        fActionService.executeAction(action, actionedUponNodeRef, checkConditions, executeAsynchronously);   
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#getAction(java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public Action getAction(String ticket, NodeRef nodeRef, String actionId)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.getAction(nodeRef, actionId);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#getActionConditionDefinition(java.lang.String, java.lang.String)
     */
    public ActionConditionDefinition getActionConditionDefinition(
            String ticket, String name)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.getActionConditionDefinition(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#getActionConditionDefinitions(java.lang.String)
     */
    public List<ActionConditionDefinition> getActionConditionDefinitions(
            String ticket)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.getActionConditionDefinitions();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#getActionDefinition(java.lang.String, java.lang.String)
     */
    public ActionDefinition getActionDefinition(String ticket, String name)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.getActionDefinition(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#getActionDefinitions(java.lang.String)
     */
    public List<ActionDefinition> getActionDefinitions(String ticket)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.getActionDefinitions();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#getActionDefinitions(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<ActionDefinition> getActionDefinitions(String ticket,
            NodeRef nodeRef)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.getActionDefinitions(nodeRef);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#getActions(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<Action> getActions(String ticket, NodeRef nodeRef)
    {
        fAuthenticationService.validate(ticket);
        return fActionService.getActions(nodeRef);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#removeAction(java.lang.String, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.action.Action)
     */
    public void removeAction(String ticket, NodeRef nodeRef, Action action)
    {
        fAuthenticationService.validate(ticket);
        fActionService.removeAction(nodeRef, action);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#removeAllActions(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void removeAllActions(String ticket, NodeRef nodeRef)
    {
        fAuthenticationService.validate(ticket);
        fActionService.removeAllActions(nodeRef);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionServiceTransport#saveAction(java.lang.String, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.action.Action)
     */
    public void saveAction(String ticket, NodeRef nodeRef, Action action)
    {
        fAuthenticationService.validate(ticket);
        fActionService.saveAction(nodeRef, action);
    }
}
