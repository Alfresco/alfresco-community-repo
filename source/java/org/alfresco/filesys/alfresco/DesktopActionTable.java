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
