/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
	 * Get the action definition for the action
	 * 
	 * @return  the action definition
	 */
	public ActionDefinition getActionDefinition();
	
    /**
     * Execute the action executer
     * 
     * @param action				the action
     * @param actionedUponNodeRef	the actioned upon node reference
     */
    public void execute(
			Action action,
            NodeRef actionedUponNodeRef);
    
	/**
	 * Get the queueName that will execute this action
	 */
	String getQueueName();

}
