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

package org.alfresco.service.cmr.action;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This is the interface for over the transport of ActionService.
 * It's job is to tunnel an authentication token for each call.
 * @author britt
 */
public interface ActionServiceTransport
{
    /**
     * Get a named action definition
     * 
     * @param name  the name of the action definition
     * @return      the action definition
     */
    ActionDefinition getActionDefinition(String ticket, String name);
    
    /**
     * Get all the action definitions
     * 
     * @return  the list action definitions
     */
    List<ActionDefinition> getActionDefinitions(String ticket);
    
    /**
     * Get all the action definitions that are applicable for the given node, based on
     * its type and aspects.
     * 
     * @param nodeRef   the node reference
     * @return          a list of applicable action definitions
     */
    List<ActionDefinition> getActionDefinitions(String ticket, NodeRef nodeRef);
    
    /**
     * Get a named action condition definition
     * 
     * @param name  the name of the action condition definition
     * @return      the action condition definition
     */
    ActionConditionDefinition getActionConditionDefinition(String ticket, String name);
    
    /**
     * Get all the action condition definitions
     * 
     * @return  the list of aciton condition definitions
     */
    List<ActionConditionDefinition> getActionConditionDefinitions(String ticket);
    
    /**
     * Create a new action
     * 
     * @param name  the action definition name
     * @return      the action
     */
    Action createAction(String ticket, String name);
    
    /**
     * Create a new action specifying the initial set of parameter values
     * 
     * @param name      the action definition name
     * @param params    the parameter values
     * @return          the action
     */
    Action createAction(String ticket, String name, Map<String, Serializable> params);
    
    /**
     * Create a composite action 
     * 
     * @return  the composite action
     */
    CompositeAction createCompositeAction(String ticket);
    
    /**
     * Create an action condition
     * 
     * @param name  the action condition definition name
     * @return      the action condition
     */
    ActionCondition createActionCondition(String ticket, String name);
    
    /**
     * Create an action condition specifying the initial set of parameter values
     * 
     * @param name      the action condition definition name
     * @param params    the parameter values
     * @return          the action condition
     */
    ActionCondition createActionCondition(String ticket, String name, Map<String, Serializable> params);
    
    /**
     * The actions conditions are always checked.
     * 
     * @see ActionService#executeAction(Action, NodeRef, boolean)
     *  
     * @param action                the action
     * @param actionedUponNodeRef   the actioned upon node reference
     */
    void executeAction(String ticket, Action action, NodeRef actionedUponNodeRef);
    
    /**
     * The action is executed based on the asynchronous attribute of the action.
     * 
     * @see ActionService#executeAction(Action, NodeRef, boolean, boolean)
     * 
     * @param action                the action
     * @param actionedUponNodeRef   the actioned upon node reference
     * @param checkConditions       indicates whether the conditions should be checked
     */
    void executeAction(String ticket, Action action, NodeRef actionedUponNodeRef, boolean checkConditions);
    
    /**
     * Executes the specified action upon the node reference provided.
     * <p>
     * If specified that the conditions should be checked then any conditions
     * set on the action are evaluated.
     * <p>
     * If the conditions fail then the action is not executed.
     * <p>
     * If an action has no conditions then the action will always be executed.
     * <p>
     * If the conditions are not checked then the action will always be executed.
     * 
     * @param action                the action
     * @param actionedUponNodeRef   the actioned upon node reference
     * @param checkConditions       indicates whether the conditions should be checked before
     *                              executing the action
     * @param executeAsynchronously indicates whether the action should be executed asychronously or not, this value overrides
     *                              the value set on the action its self
     */
    void executeAction(String ticket, Action action, NodeRef actionedUponNodeRef, boolean checkConditions, boolean executeAsynchronously);
    
    /**
     * Evaluted the conditions set on an action.
     * <p>
     * Returns true if the action has no conditions.
     * <p>
     * If the action has more than one condition their results are combined using the 'AND' 
     * logical operator.
     * 
     * @param action                the action
     * @param actionedUponNodeRef   the actioned upon node reference
     * @return                      true if the condition succeeds, false otherwise
     */
    boolean evaluateAction(String ticket, Action action, NodeRef actionedUponNodeRef);
    
    /**
     * Evaluate an action condition.
     * 
     * @param condition             the action condition
     * @param actionedUponNodeRef   the actioned upon node reference
     * @return                      true if the condition succeeds, false otherwise
     */
    boolean evaluateActionCondition(String ticket, ActionCondition condition, NodeRef actionedUponNodeRef);
    
    /**
     * Save an action against a node reference.
     * <p>
     * The node will be made configurable if it is not already.
     * <p>
     * If the action already exists then its details will be updated.
     * 
     * @param nodeRef   the node reference
     * @param action    the action
     */
    void saveAction(String ticket, NodeRef nodeRef, Action action);
    
    /**
     * Gets all the actions currently saved on the given node reference.
     * 
     * @param nodeRef   the node reference
     * @return          the list of actions
     */
    List<Action> getActions(String ticket, NodeRef nodeRef);
    
    /**
     * Gets an action stored against a given node reference.
     * <p>
     * Returns null if the action can not be found.
     * 
     * @param nodeRef   the node reference
     * @param actionId  the action id
     * @return          the action
     */
    Action getAction(String ticket, NodeRef nodeRef, String actionId);
    
    /**
     * Removes an action associated with a node reference.
     * 
     * @param nodeRef       the node reference
     * @param action        the action
     */
    void removeAction(String ticket, NodeRef nodeRef, Action action);
    
    /**
     * Removes all actions associated with a node reference
     * 
     * @param nodeRef   the node reference
     */
    void removeAllActions(String ticket, NodeRef nodeRef);
}
