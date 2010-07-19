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
    private String actionType;
    private String actionId;
    private int executionInstance;
    
    public ExecutionSummary(String actionType, String actionId,
         int executionInstance) {
       this.actionType = actionType;
       this.actionId = actionId;
       this.executionInstance = executionInstance;
    }

    public String getActionType() {
       return actionType;
    }

    public String getActionId() {
       return actionId;
    }

    public int getExecutionInstance() {
       return executionInstance;
    }
 }