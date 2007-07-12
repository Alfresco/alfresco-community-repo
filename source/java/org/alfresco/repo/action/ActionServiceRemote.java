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

import org.alfresco.repo.remote.ClientTicketHolder;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionServiceTransport;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Client side implementation of ActionService for remote access.
 * @author britt
 */
public class ActionServiceRemote implements ActionService
{
    private ClientTicketHolder fHolder;
    
    private ActionServiceTransport fTransport;
    
    public void setClientTicketHolder(ClientTicketHolder holder)
    {
        fHolder = holder;
    }
    
    public void setActionServiceTransport(ActionServiceTransport transport)
    {
        fTransport = transport;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#createAction(java.lang.String)
     */
    public Action createAction(String name)
    {
        return fTransport.createAction(fHolder.getTicket(), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#createAction(java.lang.String, java.util.Map)
     */
    public Action createAction(String name, Map<String, Serializable> params)
    {
        return fTransport.createAction(fHolder.getTicket(), name, params);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#createActionCondition(java.lang.String)
     */
    public ActionCondition createActionCondition(String name)
    {
        return fTransport.createActionCondition(fHolder.getTicket(), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#createActionCondition(java.lang.String, java.util.Map)
     */
    public ActionCondition createActionCondition(String name,
            Map<String, Serializable> params)
    {
        return fTransport.createActionCondition(fHolder.getTicket(), name, params);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#createCompositeAction()
     */
    public CompositeAction createCompositeAction()
    {
        return fTransport.createCompositeAction(fHolder.getTicket());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#evaluateAction(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateAction(Action action, NodeRef actionedUponNodeRef)
    {
        return fTransport.evaluateAction(fHolder.getTicket(), action, actionedUponNodeRef);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#evaluateActionCondition(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateActionCondition(ActionCondition condition,
            NodeRef actionedUponNodeRef)
    {
        return fTransport.evaluateActionCondition(fHolder.getTicket(), condition, actionedUponNodeRef);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#executeAction(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void executeAction(Action action, NodeRef actionedUponNodeRef)
    {
        fTransport.executeAction(fHolder.getTicket(), action, actionedUponNodeRef);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#executeAction(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public void executeAction(Action action, NodeRef actionedUponNodeRef,
            boolean checkConditions)
    {
        fTransport.executeAction(fHolder.getTicket(), action, actionedUponNodeRef, checkConditions);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#executeAction(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef, boolean, boolean)
     */
    public void executeAction(Action action, NodeRef actionedUponNodeRef,
            boolean checkConditions, boolean executeAsynchronously)
    {
        fTransport.executeAction(fHolder.getTicket(), action, actionedUponNodeRef, checkConditions);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#getAction(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public Action getAction(NodeRef nodeRef, String actionId)
    {
        return fTransport.getAction(fHolder.getTicket(), nodeRef, actionId);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#getActionConditionDefinition(java.lang.String)
     */
    public ActionConditionDefinition getActionConditionDefinition(String name)
    {
        return fTransport.getActionConditionDefinition(fHolder.getTicket(), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#getActionConditionDefinitions()
     */
    public List<ActionConditionDefinition> getActionConditionDefinitions()
    {
        return fTransport.getActionConditionDefinitions(fHolder.getTicket());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#getActionDefinition(java.lang.String)
     */
    public ActionDefinition getActionDefinition(String name)
    {
        return fTransport.getActionDefinition(fHolder.getTicket(), name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#getActionDefinitions()
     */
    public List<ActionDefinition> getActionDefinitions()
    {
        return fTransport.getActionDefinitions(fHolder.getTicket());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#getActionDefinitions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<ActionDefinition> getActionDefinitions(NodeRef nodeRef)
    {
        return fTransport.getActionDefinitions(fHolder.getTicket(), nodeRef);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#getActions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<Action> getActions(NodeRef nodeRef)
    {
        return fTransport.getActions(fHolder.getTicket(), nodeRef);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#removeAction(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.action.Action)
     */
    public void removeAction(NodeRef nodeRef, Action action)
    {
        fTransport.removeAction(fHolder.getTicket(), nodeRef, action);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#removeAllActions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void removeAllActions(NodeRef nodeRef)
    {
        fTransport.removeAllActions(fHolder.getTicket(), nodeRef);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.action.ActionService#saveAction(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.action.Action)
     */
    public void saveAction(NodeRef nodeRef, Action action)
    {
        fTransport.saveAction(fHolder.getTicket(), nodeRef, action);
    }
}
