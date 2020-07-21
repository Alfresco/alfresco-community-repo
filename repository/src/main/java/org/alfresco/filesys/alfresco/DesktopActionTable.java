/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.filesys.alfresco;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Desktop Action Table Class
 * 
 * <p>Contains a list of desktop actions indexed by action name.
 *
 * @author gkspencer
*/
public class DesktopActionTable {

	// Table of actions, indexed by action name and pseudo file name
	
	private Hashtable<String, DesktopAction> m_actions;
	private Hashtable<String, DesktopAction> m_actionsPseudo;
	
	/**
	 * Default constructor
	 */
	public DesktopActionTable()
	{
		m_actions = new Hashtable<String, DesktopAction>();
		m_actionsPseudo = new Hashtable<String, DesktopAction>();
	}
	
	/**
	 * Find a named action
	 * 
	 * @param name String
	 * @return DesktopAction
	 */
	public final DesktopAction getAction(String name)
	{
		return m_actions.get(name);
	}

	/**
	 * Find an action via the pseudo file name
	 * 
	 * @param pseudoName String
	 * @return DesktopAction
	 */
	public final DesktopAction getActionViaPseudoName(String pseudoName)
	{
		return m_actionsPseudo.get(pseudoName.toUpperCase());
	}
	
	/**
	 * Return the count of actions
	 * 
	 * @return int
	 */
	public final int numberOfActions()
	{
		return m_actions.size();
	}
	
	/**
	 * Add an action
	 * 
	 * @param action DesktopAction
	 * @return boolean
	 */
	public final boolean addAction(DesktopAction action)
	{
		if ( m_actions.get( action.getName()) == null)
		{
			m_actions.put(action.getName(), action);
			m_actionsPseudo.put(action.getPseudoFile().getFileName().toUpperCase(), action);
			return true;
		}
		return false;
	}
	
	/**
	 * Enumerate the action names
	 * 
	 */
	public final Enumeration<String> enumerateActionNames()
	{
		return m_actions.keys();
	}
	
	/**
	 * Remove an action
	 * 
	 * @param name String
	 * @return DesktopAction
	 */
	public final DesktopAction removeAction(String name)
	{
		DesktopAction action = m_actions.remove(name);
		if ( action != null)
			m_actionsPseudo.remove(action.getPseudoFile().getFileName().toUpperCase());
		return action;
	}
}
