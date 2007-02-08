/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.action;

import java.util.Set;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Asynchronous action execution queue
 * 
 * @author Roy Wetherall
 */
public interface AsynchronousActionExecutionQueue
{
	/**
	 * 
	 * @param actionedUponNodeRef
	 * @param action
	 * @param checkConditions 
	 */
	void executeAction(
			RuntimeActionService actionService,
			Action action,
			NodeRef actionedUponNodeRef, 			 
			boolean checkConditions,
            Set<String> actionChain);
	
}
