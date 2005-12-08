/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
