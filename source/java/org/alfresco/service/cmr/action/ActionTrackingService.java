/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.util.List;

import org.alfresco.service.PublicService;

/**
 * Service interface for tracking when actions
 *  begin to run, complete or fail.
 * 
 * @author Nick Burch
 */
public interface ActionTrackingService
{
    /**
     * Record that an action has been scheduled for
     *  asynchronous execution, and is pending
     *  being executed.
     * 
     * @param action  the action that has been scheduled
     */
    void recordActionPending(Action action);
    
    /**
     * Record that an action has begun execution.
     * 
     * @param action  the action that has begun execution
     */
    void recordActionExecuting(Action action);
    
    /**
     * Record that an action has completed execution
     *  without error.
     * 
     * @param action  the action that has been finished
     */
    void recordActionComplete(Action action);
    
    /**
     * Record that an action failed during execution
     * 
     * @param action  the action that has failed
     */
    void recordActionFailure(Action action, Throwable problem);
    
    /**
     * Requests that the specified Action cancel itself
     *  and aborts execution, as soon as possible.
     * Cancellable actions periodically check to see
     *  if a cancel has been requested, and will take
     *  note of the cancel request once seen.
     * 
     * @param action The action to request the cancel of
     */
    void requestActionCancellation(CancellableAction action);
    
    /**
     * Requests that the specified Action cancel itself
     *  and aborts execution, as soon as possible.
     * Cancellable actions periodically check to see
     *  if a cancel has been requested, and will take
     *  note of the cancel request once seen.
     * If the specified action is not a cancellable
     *  action, nothing will happen.
     * 
     * @param action The action to request the cancel of
     */
    void requestActionCancellation(ExecutionSummary executionSummary);
    
    /**
     * Has cancellation been requested for the given
     *  action?
     * This method is most commonly called by the
     *  action in question, to check to see if
     *  someone has called {@link #requestActionCancellation(CancellableAction)}
     *  for them.
     *  
     * @param action The action to check about
     * @return if cancellation has been requested or not
     */
    boolean isCancellationRequested(CancellableAction action);
    
    /**
     * Retrieves the execution details on the given
     *  executing action, such as when it started,
     *  and what machine it is executing on.
     */
    ExecutionDetails getExecutionDetails(ExecutionSummary executionSummary);
    
    /**
     * Retrieve summary details of all the actions
     *  currently executing.  
     */
    List<ExecutionSummary> getAllExecutingActions();
    
    /**
     * Retrieve summary details of all the actions
     *  of the given type that are currently executing.  
     */
    List<ExecutionSummary> getExecutingActions(String type);
    
    /**
     * Retrieve summary details of all instances of
     *  the specified action that are currently
     *  executing.
     */
    List<ExecutionSummary> getExecutingActions(Action action);
}
