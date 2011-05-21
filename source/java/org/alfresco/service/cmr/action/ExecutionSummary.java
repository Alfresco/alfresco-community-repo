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

/**
 * Holds core key details of an Action that is 
 *  currently executing.
 * This information is normally the limit
 *  of what the {@link ActionTrackingService}
 *  can use when filtering lists of actions.
 *  
 * @author Nick Burch
 */
public class ExecutionSummary {
    private final String actionType;
    private final String actionId;
    private final int executionInstance;
    
    public ExecutionSummary(final String actionType, final String actionId,
         final int executionInstance) {
       this.actionType = actionType;
       this.actionId = actionId;
       this.executionInstance = executionInstance;
    }

    /**
     * What kind of action is this? 
     * @return The action type, typically an executor bean name
     */
    public String getActionType() {
       return actionType;
    }

    /**
     * What is the id of the action?
     * @return The action ID
     */
    public String getActionId() {
       return actionId;
    }

    /**
     * Which instance of the action is this?
     * Every time you start an action, it gets
     *  a new instance ID, and this lets you
     *  tell the difference between two copies
     *  running in parallel.
     * @return The instance ID
     */
    public int getExecutionInstance() {
       return executionInstance;
    }
    
    public String toString() {
       return "Execution of " + actionType + " as " + executionInstance + " : " + actionId; 
    }
 }