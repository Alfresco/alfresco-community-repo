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

import org.alfresco.service.PublicService;

/**
 * Service interface for tracking when actions
 *  begin to run, complete or fail.
 * 
 * @author Nick Burch
 */
@PublicService
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
}
