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

package org.alfresco.repo.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionStatus;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Action implementation
 * 
 * @author Roy Wetherall
 */
public class ActionImpl extends ParameterizedItemImpl implements Action
{
    private static final long serialVersionUID = 3258135760426186548L;

    private NodeRef nodeRef;
    private String title;
    private String description;
    private Boolean trackStatus = null;
    private boolean executeAsynchronously = false;
    private Action compensatingAction;
    private Date createdDate;
    private String creator;
    private Date modifiedDate;
    private String modifier;
    private String actionDefinitionName;
    private String runAsUserName;
    private Set<String> actionChain;
    private List<ActionCondition> actionConditions = new ArrayList<ActionCondition>();
    
    /**
     * When there is more than one instance of the
     *  action executing, both with the same ID, 
     *  which one is this?
     * This crops up most often with persisted
     *  actions, with two copies running, one on
     *  each of two different target nodes.
     */
    private int executionInstance = -1;
    
    /**
     * When the action started executing, or null if it hasn't yet.
     */
    private Date executionStartDate;
    
    /**
     * When the action finished executing, or null if it hasn't yet.
     */
    private Date executionEndDate;
    
    /**
     * The status of the action's execution
     */
    private ActionStatus executionStatus = ActionStatus.New;
    
    /**
     * Why the action failed to execute fully,
     *  if exists.
     */
    private String executionFailureMessage;

    /**
     * Constructor
     * 
     * @param nodeRef the action node reference (null if not saved)
     * @param id the action id
     * @param actionDefinitionName the name of the action definition
     */
    public ActionImpl(NodeRef nodeRef, String id, String actionDefinitionName)
    {
        this(nodeRef, id, actionDefinitionName, null);
    }

    /**
     * Constructor
     * 
     * @param nodeRef the action node reference (null if not saved)
     * @param id the action id
     * @param actionDefinitionName the action definition name
     * @param parameterValues the parameter values
     */
    public ActionImpl(NodeRef nodeRef, String id, String actionDefinitionName, Map<String, Serializable> parameterValues)
    {
        super(id, parameterValues);
        this.nodeRef = nodeRef;
        this.actionDefinitionName = actionDefinitionName;
    }

    public ActionImpl(Action action, String actionDefinitionName)
    {
        super(action.getId(), action.getParameterValues());
        this.actionDefinitionName = actionDefinitionName;
        this.actionConditions = action.getActionConditions();
        this.compensatingAction = action.getCompensatingAction();
        this.createdDate = action.getCreatedDate();
        this.creator = action.getCreator();
        this.description = action.getDescription();
        this.trackStatus = action.getTrackStatus();
        this.executeAsynchronously = action.getExecuteAsychronously();
        this.modifiedDate = action.getModifiedDate();
        this.modifier = action.getModifier();
        this.nodeRef = action.getNodeRef();
        this.title = action.getTitle();
        this.executionStartDate = action.getExecutionStartDate();
        this.executionEndDate = action.getExecutionEndDate();
        this.executionStatus = action.getExecutionStatus();
        this.executionFailureMessage = action.getExecutionFailureMessage();
        if (action instanceof ActionImpl)
        {
            ActionImpl actionImpl = (ActionImpl) action;
            this.executionInstance = actionImpl.getExecutionInstance();
            this.runAsUserName = actionImpl.getRunAsUser();
            this.actionChain = actionImpl.actionChain;
        }
    }

    public ActionImpl(Action action)
    {
        this(action, action.getActionDefinitionName());
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Action").append("[ id=").append(getId()).append(", node=").append(nodeRef).append(" ]");
        return sb.toString();
    }

    @Override
    public String getTitle()
    {
        return this.title;
    }

    @Override
    public void setTitle(String title)
    {
        this.title = title;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public Boolean getTrackStatus()
    {
        return trackStatus;
    }

    @Override
    public void setTrackStatus(Boolean trackStatus)
    {
        this.trackStatus = trackStatus;
    }

    @Override
    public boolean getExecuteAsychronously()
    {
        return this.executeAsynchronously;
    }

    @Override
    public void setExecuteAsynchronously(boolean executeAsynchronously)
    {
        this.executeAsynchronously = executeAsynchronously;
    }

    @Override
    public Action getCompensatingAction()
    {
        return this.compensatingAction;
    }

    @Override
    public void setCompensatingAction(Action action)
    {
        this.compensatingAction = action;
    }

    @Override
    public Date getCreatedDate()
    {
        return this.createdDate;
    }

    /**
     * Set the created date
     * 
     * @param createdDate the created date
     */
    public void setCreatedDate(Date createdDate)
    {
        this.createdDate = createdDate;
    }

    @Override
    public String getCreator()
    {
        return this.creator;
    }

    /**
     * Set the creator
     */
    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    @Override
    public Date getModifiedDate()
    {
        return this.modifiedDate;
    }

    /**
     * Set the modified date
     */
    public void setModifiedDate(Date modifiedDate)
    {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String getModifier()
    {
        return this.modifier;
    }

    /**
     * Set the modifier
     */
    public void setModifier(String modifier)
    {
        this.modifier = modifier;
    }

    @Override
    public String getActionDefinitionName()
    {
        return this.actionDefinitionName;
    }

    @Override
    public boolean hasActionConditions()
    {
        return (this.actionConditions.isEmpty() == false);
    }

    @Override
    public int indexOfActionCondition(ActionCondition actionCondition)
    {
        return this.actionConditions.indexOf(actionCondition);
    }

    @Override
    public List<ActionCondition> getActionConditions()
    {
        return this.actionConditions;
    }

    @Override
    public ActionCondition getActionCondition(int index)
    {
        return this.actionConditions.get(index);
    }

    @Override
    public void addActionCondition(ActionCondition actionCondition)
    {
        this.actionConditions.add(actionCondition);
    }

    @Override
    public void addActionCondition(int index, ActionCondition actionCondition)
    {
        this.actionConditions.add(index, actionCondition);
    }

    @Override
    public void setActionCondition(int index, ActionCondition actionCondition)
    {
        this.actionConditions.set(index, actionCondition);
    }

    @Override
    public void removeActionCondition(ActionCondition actionCondition)
    {
        this.actionConditions.remove(actionCondition);
    }

    @Override
    public void removeAllActionConditions()
    {
        this.actionConditions.clear();
    }

    /**
     * Set the action chain
     * 
     * @param actionChain the list of actions that lead to this action
     */
    public void setActionChain(Set<String> actionChain)
    {
        this.actionChain = actionChain;
    }

    /**
     * Get the action chain
     * 
     * @return the list of actions that lead to this action
     */
    public Set<String> getActionChain()
    {
        return actionChain;
    }

    public String getRunAsUser()
    {
        return this.runAsUserName;
    }

    public void setRunAsUser(String runAsUserName)
    {
        this.runAsUserName = runAsUserName;
    }

    @Override
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }

    /**
     * Set the node reference
     * 
     * @param nodeRef the node reference
     */
    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }
    
    @Override
    public void addParameterValues(Map<String, Serializable> values)
    {
        getParameterValues().putAll(values);
    }
    
    /**
     * When there is more than one instance of the
     *  action executing, both with the same ID, 
     *  which one is this?
     * This crops up most often with persisted
     *  actions, with two copies running, one on
     *  each of two different target nodes.
     */
    public int getExecutionInstance() 
    {
       return executionInstance;
    }
    
    /**
     * Called by the ActionService when the action  begins running. 
     */
    public void setExecutionInstance(int instance) {
       executionInstance = instance;
    }
    
    public Date getExecutionStartDate() {
       return executionStartDate;
    }

    /**
     * Records the date when the action began execution,
     *  normally called by the {@link ActionService} when
     *  it starts running the action.
     */
    public void setExecutionStartDate(Date startDate) {
       this.executionStartDate = startDate;
    }
    
    public Date getExecutionEndDate() {
       return executionEndDate;
    }

    /**
     * Records the date when the action finished execution,
     *  normally called by the {@link ActionService} when
     *  the action completes or fails.
     */
    public void setExecutionEndDate(Date endDate) {
       this.executionEndDate = endDate;
    }
    
    public ActionStatus getExecutionStatus() {
       return executionStatus;
    }

    /**
     * Updates the current execution status. This is 
     *  normally called by the {@link ActionService} as
     *  it progresses the Action's execution.
     */
    public void setExecutionStatus(ActionStatus status) {
       this.executionStatus = status;
    }
    
    public String getExecutionFailureMessage() {
       return executionFailureMessage;
    }

    /**
     * Records the message of the exception which caused the 
     *  Action to fail, if any.
     */
    public void setExecutionFailureMessage(String message) {
       this.executionFailureMessage = message;
    }
}
