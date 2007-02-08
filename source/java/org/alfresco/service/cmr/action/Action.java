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
package org.alfresco.service.cmr.action;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * The rule action interface
 * 
 * @author Roy Wetherall
 */
public interface Action extends ParameterizedItem
{
    /**
     * Gets the node ref that represents the saved action node.
     * Returns null id unsaved.
     * 
     * @return  the action node reference
     */
    NodeRef getNodeRef();
    
	/**
	 * Get the name of the action definition that relates to this action
	 * 
	 * @return	the action defintion name
	 */
	String getActionDefinitionName();
	
	/**
	 * Get the title of the action
	 * 
	 * @return  the title of the action
	 */
	String getTitle();
	
	/**
	 * Set the title of the action
	 * 
	 * @param title	the title of the action
	 */
	void setTitle(String title);
	
	/**
	 * Get the description of the action
	 * 
	 * @return	the description of the action
	 */
	String getDescription();
	
	/**
	 * Set the description of the action
	 * 
	 * @param description  the description of the action
	 */
	void setDescription(String description);
	
	/**
	 * Gets a value indicating whether the action should be executed asychronously or not.
	 * <p>
	 * The default is to execute the action synchronously.
	 * 
	 * @return	true if the action is executed asychronously, false otherwise.  
	 */
	boolean getExecuteAsychronously();
	
	/**
	 * Set the value that indicates whether the action should be executed asychronously or not.
	 * 
	 * @param executeAsynchronously		true if the action is to be executed asychronously, false otherwise.
	 */
	void setExecuteAsynchronously(boolean executeAsynchronously);
	
	/**
	 * Get the compensating action.
	 * <p>
	 * This action is executed if the failure behaviour is to compensate and the action being executed 
	 * fails.
	 * 
	 * @return	the compensating action
	 */
	Action getCompensatingAction();
	
	/**
	 * Set the compensating action.
	 * 
	 * @param action	the compensating action
	 */
	void setCompensatingAction(Action action);
	
	/**
	 * Get the date the action was created
	 * 
	 * @return	action creation date
	 */
	Date getCreatedDate();
	
	/**
	 * Get the name of the user that created the action
	 * 
	 * @return	user name
	 */
	String getCreator();
	
	/**
	 * Get the date that the action was last modified
	 * 
	 * @return	aciton modification date
	 */
	Date getModifiedDate();
	
	/**
	 * Get the name of the user that last modified the action
	 * 
	 * @return	user name
	 */
	String getModifier();
	
	/**
	 * Indicates whether the action has any conditions specified
	 * 
	 * @return  true if the action has any conditions specified, flase otherwise
	 */
	boolean hasActionConditions();
	
	/**
	 * Gets the index of an action condition
	 * 
	 * @param actionCondition	the action condition
	 * @return					the index
	 */
	int indexOfActionCondition(ActionCondition actionCondition);
	
	/**
	 * Gets a list of the action conditions for this action
	 * 
	 * @return  list of action conditions
	 */
	List<ActionCondition> getActionConditions();
	
	/**
	 * Get the action condition at a given index
	 * 
	 * @param index  the index
	 * @return		 the action condition
	 */
	ActionCondition getActionCondition(int index);
	
	/**
	 * Add an action condition to the action
	 * 
	 * @param actionCondition  an action condition
	 */
	void addActionCondition(ActionCondition actionCondition);
	
	/**
	 * Add an action condition at the given index
	 * 
	 * @param index				the index
	 * @param actionCondition	the action condition
	 */
	void addActionCondition(int index, ActionCondition actionCondition);
	
	/**
	 * Replaces the current action condition at the given index with the 
	 * action condition provided.
	 * 
	 * @param index				the index
	 * @param actionCondition	the action condition
	 */
	void setActionCondition(int index, ActionCondition actionCondition);
	
	/**
	 * Removes an action condition
	 * 
	 * @param actionCondition  an action condition
	 */
	void removeActionCondition(ActionCondition actionCondition);
	
	/**
	 * Removes all action conditions 
	 */
	void removeAllActionConditions();
}
