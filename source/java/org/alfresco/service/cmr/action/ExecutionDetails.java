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
    
    private final NodeRef persistedActionRef;
    private final NodeRef actionedUponNodeRef;
    private final String runningOn;
    private final Date startedAt;
    private final boolean cancelRequested;
    
    public ExecutionDetails() {
       persistedActionRef = null;
       actionedUponNodeRef = null;
       runningOn = null;
       startedAt = null;
       cancelRequested = false;
    }

    public ExecutionDetails(ExecutionSummary executionSummary,
          NodeRef persistedActionRef, String runningOn, Date startedAt,
          boolean cancelRequested) {
       this.executionSummary = executionSummary;
       this.persistedActionRef = persistedActionRef;
       this.actionedUponNodeRef = null;
       this.runningOn = runningOn;
       this.startedAt = startedAt;
       this.cancelRequested = cancelRequested;
    }
    
    public ExecutionDetails(ExecutionSummary executionSummary,
            NodeRef persistedActionRef, NodeRef actionedUponNodeRef, 
            String runningOn, Date startedAt,
            boolean cancelRequested) {
         this.executionSummary = executionSummary;
         this.persistedActionRef = persistedActionRef;
         this.actionedUponNodeRef = actionedUponNodeRef;
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
    
    /**
     * Gets the <code>NodeRef</code> where the action is persisted.
     * 
     * @return <code>NodeRef</code> for the persisted action
     */
    public NodeRef getPersistedActionRef() {
       return persistedActionRef;
    }

    /**
     * Gets the <code>NodeRef</code> the action is acting on.
     * 
     * @return <code>NodeRef</code> the action is acting on
     */
    public NodeRef getActionedUponNodeRef()
    {
        return actionedUponNodeRef;
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
 }