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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Holds all the details available to the 
 *  {@link ActionTrackingService} on a currently
 *  executing Action.
 *  
 * @author Nick Burch
 */
public class ExecutionDetails implements Serializable {
   private static final long serialVersionUID = 8002491363996364589L;
   
   /* 
     * Transient as all the info is held in the key,
     *  we don't need to also hold a 2nd copy of it 
     */
    private transient ExecutionSummary executionSummary;
    private NodeRef persistedActionRef;
    private String runningOn;
    private Date startedAt;
    private boolean cancelRequested;
    
    public ExecutionDetails() {}

    public ExecutionDetails(ExecutionSummary executionSummary,
          NodeRef persistedActionRef, String runningOn, Date startedAt,
          boolean cancelRequested) {
       this.executionSummary = executionSummary;
       this.persistedActionRef = persistedActionRef;
       this.runningOn = runningOn;
       this.startedAt = startedAt;
       this.cancelRequested = cancelRequested;
    }

    public ExecutionSummary getExecutionSummary() {
       return executionSummary;
    }
    public void setExecutionSummary(ExecutionSummary executionSummary) {
       this.executionSummary = executionSummary;
    }
    
    /**
     * What kind of action is this? 
     * @return The action type, typically an executor bean name
     */
    public String getActionType() {
       return executionSummary.getActionType();
    }

    /**
     * What is the id of the action?
     * @return The action ID
     */
    public String getActionId() {
       return executionSummary.getActionId();
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
       return executionSummary.getExecutionInstance();
    }
    
    public NodeRef getPersistedActionRef() {
       return persistedActionRef;
    }

    public String getRunningOn() {
       return runningOn;
    }

    /**
     * Returns when this action started executing, or
     *  null if it is still pending
     */
    public Date getStartedAt() {
       return startedAt;
    }

    public boolean isCancelRequested() {
       return cancelRequested;
    }
    public void requestCancel() {
       cancelRequested = true;
    }
 }