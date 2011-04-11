/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.service.cmr.action;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * The rule action interface
 * 
 * @author Roy Wetherall
 */
public interface Action extends ParameterizedItem
{
    /**
     * Gets the node ref that represents the saved action node.
     * Returns null id unsaved.
     * 
     * @return  the action node reference
     */
    NodeRef getNodeRef();
    
    /**
     * Get the name of the action definition that relates to this action
     * 
     * @return    the action defintion name
     */
    String getActionDefinitionName();
    
    /**
     * Get the title of the action
     * 
     * @return  the title of the action
     */
    String getTitle();
    
    /**
     * Set the title of the action
     * 
     * @param title    the title of the action
     */
    void setTitle(String title);
    
    /**
     * Get the description of the action
     * 
     * @return    the description of the action
     */
    String getDescription();
    
    /**
     * Set the description of the action
     * 
     * @param description  the description of the action
     */
    void setDescription(String description);
    
    /**
     * @return                  <tt>true</tt> if the action must be tracked by the
     *                          {@link ActionTrackingService}, <tt>false</tt> if it must NOT be
     *                          tracked or <tt>null</tt> to use the action definition's default.
     */
    Boolean getTrackStatus();
    
    /**
     * Set whether the action should be tracked or not.
     * <p/>
     * The option of tracking can be <tt>null</tt> i.e. unset, indicating that the
     * defaults of the action definition should be used.  This is to allow manual overriding
     * of the property when it becomes supported by the UI.
     * 
     * @param trackStatus       <tt>true</tt> if the action must be tracked by the
     *                          {@link ActionTrackingService}, <tt>false</tt> if it must NOT be
     *                          tracked or <tt>null</tt> to use the action definition's default.
     */
    void setTrackStatus(Boolean trackStatus);
    
    /**
     * Gets a value indicating whether the action should be executed asychronously or not.
     * <p>
     * The default is to execute the action synchronously.
     * 
     * @return    true if the action is executed asychronously, false otherwise.  
     */
    boolean getExecuteAsychronously();
    
    /**
     * Set the value that indicates whether the action should be executed asychronously or not.
     * 
     * @param executeAsynchronously        true if the action is to be executed asychronously, false otherwise.
     */
    void setExecuteAsynchronously(boolean executeAsynchronously);
    
    /**
     * Get the compensating action.
     * <p>
     * This action is executed if the failure behaviour is to compensate and the action being executed 
     * fails.
     * 
     * @return    the compensating action
     */
    Action getCompensatingAction();
    
    /**
     * Set the compensating action.
     * 
     * @param action    the compensating action
     */
    void setCompensatingAction(Action action);
    
    /**
     * Get the date the action was created
     * 
     * @return    action creation date
     */
    Date getCreatedDate();
    
    /**
     * Get the name of the user that created the action
     * 
     * @return    user name
     */
    String getCreator();
    
    /**
     * Get the date that the action was last modified
     * 
     * @return    aciton modification date
     */
    Date getModifiedDate();
    
    /**
     * Get the name of the user that last modified the action
     * 
     * @return    user name
     */
    String getModifier();
    
    /**
     * Indicates whether the action has any conditions specified
     * 
     * @return  true if the action has any conditions specified, flase otherwise
     */
    boolean hasActionConditions();
    
    /**
     * Gets the index of an action condition
     * 
     * @param actionCondition    the action condition
     * @return                    the index
     */
    int indexOfActionCondition(ActionCondition actionCondition);
    
    /**
     * Gets a list of the action conditions for this action
     * 
     * @return  list of action conditions
     */
    List<ActionCondition> getActionConditions();
    
    /**
     * Get the action condition at a given index
     * 
     * @param index  the index
     * @return         the action condition
     */
    ActionCondition getActionCondition(int index);
    
    /**
     * Add an action condition to the action
     * 
     * @param actionCondition  an action condition
     */
    void addActionCondition(ActionCondition actionCondition);
    
    /**
     * Add an action condition at the given index
     * 
     * @param index                the index
     * @param actionCondition    the action condition
     */
    void addActionCondition(int index, ActionCondition actionCondition);
    
    /**
     * Replaces the current action condition at the given index with the 
     * action condition provided.
     * 
     * @param index                the index
     * @param actionCondition    the action condition
     */
    void setActionCondition(int index, ActionCondition actionCondition);
    
    /**
     * Removes an action condition
     * 
     * @param actionCondition  an action condition
     */
    void removeActionCondition(ActionCondition actionCondition);
    
    /**
     * Removes all action conditions 
     */
    void removeAllActionConditions();
    
    /**
     * Adds a {@link Map} of parameter values to the {@link Action}
     * @param values A map of values to be added
     */
    void addParameterValues(Map<String, Serializable> values);
 
    /**
     * Gets the date that the action (last) began executing at.
     * Null if the action has not yet been run.
     * For a saved action, this will be the last time at ran.
     * @return The date the action (last) began executing at, or null.
     */
    Date getExecutionStartDate();
    
    /**
     * Gets the date that the action (last) finished
     *  execution at.
     * Null if the action has not yet been run, or is
     *  currently running.
     * For a saved action, this will normally be the last
     *  time it finished running.
     * @return The date the action last finished exeucting at, or null.
     */
    Date getExecutionEndDate();
    
    /**
     * Gets the current execution status of the action,
     *  such as Running or Completed.
     * @return The current execution status
     */
    ActionStatus getExecutionStatus();
    
    /**
     * Gets the message of the exception which caused the 
     *  Action execution failure, or null if the Action
     *  hasn't failed / has been retried.
     * @return The exception message, if the action has failed
     */
    String getExecutionFailureMessage();
}
