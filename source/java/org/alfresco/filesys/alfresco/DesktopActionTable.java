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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */

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
	 * @return Enumeration<String>
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
