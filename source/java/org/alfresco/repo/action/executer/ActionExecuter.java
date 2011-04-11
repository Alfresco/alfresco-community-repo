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
package org.alfresco.repo.action.executer;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Action executer interface
 * 
 * @author Roy Wetherall
 */
public interface ActionExecuter
{
	/** Standard action result parameter name */
	public static String PARAM_RESULT = "result";
	
	/**
	 * Get the queueName that will execute this action
	 */
	String getQueueName();
	
	/**
	 * Indicates whether a lock on the actioned upon node reference should be ignored or 
	 * not.  If true any lock is ignored and execution continues reguardless, otherwise the
	 * lock is checked and the action is not executed (ignored) if the actioned upon node reference
	 * is locked in any way.  By default locks will be ignored.
	 * 
	 * @since 3.3.4
	 * @return	boolean	true if ignore lock, false otherwise.
	 */
	boolean getIgnoreLock();
	
    /**
     * Get whether the basic action definition supports action tracking
     * or not.  This can be overridden for each {@link Action#getTrackStatus() action}
     * but if not, this value is used.  Defaults to <tt>false</tt>.
     * 
     * @return      <tt>true</tt> to track action execution status or <tt>false</tt> (default)
     *              to do no action tracking
     * 
     * @since 3.4.1
     */
	boolean getTrackStatus();
	
	/**
	 * Get the action definition for the action
	 * 
	 * @return  the action definition
	 */
	ActionDefinition getActionDefinition();
	
    /**
     * Execute the action executer
     * 
     * @param action				the action
     * @param actionedUponNodeRef	the actioned upon node reference
     */
    void execute(Action action, NodeRef actionedUponNodeRef);
}
