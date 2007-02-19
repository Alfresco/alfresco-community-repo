/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.action;

import java.util.List;

/**
 * Composite action
 * 
 * @author Roy Wetherall
 */
public interface CompositeAction extends Action
{
	/**
	 * Indicates whether there are any actions
	 * 
	 * @return  true if there are actions, false otherwise
	 */
	boolean hasActions();
	
	/**
	 * Add an action to the end of the list
	 * 
	 * @param action  the action
	 */
	void addAction(Action action);
	
	/**
	 * Add an action to the list at the index specified
	 * 
	 * @param index		the index
	 * @param action	the action
	 */
	void addAction(int index, Action action);
	
	/**
	 * Replace the action at the specfied index with the passed action.
	 * 
	 * @param index		the index
	 * @param action	the action
	 */
	void setAction(int index, Action action);
	
	/**
	 * Gets the index of an action
	 * 
	 * @param action	the action
	 * @return			the index
	 */
	int indexOfAction(Action action);
	
	/**
	 * Get list containing the actions in their current order
	 * 
	 * @return  the list of actions
	 */
	List<Action> getActions();
	
	/**
	 * Get an action at a given index
	 * 
	 * @param index		the index
	 * @return			the action
	 */
	Action getAction(int index);
	
	/**
	 * Remove an action from the list
	 * 
	 * @param action  the action
	 */
	void removeAction(Action action);
	
	/**
	 * Remove all actions from the list
	 */
	void removeAllActions();
}
