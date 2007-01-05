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
package org.alfresco.filesys.server;

import java.util.*;

/**
 * Session Handler List Class
 * 
 * @author GKSpencer
 */
public class SessionHandlerList {

  //	List of session handlers

	private Vector<SessionHandlerInterface> m_handlers;

	/**
	 * Default constructor
	 */
	public SessionHandlerList()
	{
		m_handlers = new Vector<SessionHandlerInterface>();
	}

	/**
	 * Add a handler to the list
	 * 
	 * @param handler SessionHandlerInterface
	 */
	public final void addHandler(SessionHandlerInterface handler)
	{
		m_handlers.addElement(handler);
	}

	/**
	 * Return the number of handlers in the list
	 * 
	 * @return int
	 */
	public final int numberOfHandlers()
	{
		return m_handlers.size();
	}

	/**
	 * Return the specified handler
	 * 
	 * @param idx int
	 * @return SessionHandlerInterface
	 */
	public final SessionHandlerInterface getHandlerAt(int idx)
	{

		//	Range check the index

		if (idx < 0 || idx >= m_handlers.size())
			return null;
		return (SessionHandlerInterface) m_handlers.elementAt(idx);
	}

	/**
	 * Find the required handler by name
	 * 
	 * @param name String
	 * @return SessionHandlerInterface
	 */
	public final SessionHandlerInterface findHandler(String name)
	{

		//	Search for the required handler

		for (int i = 0; i < m_handlers.size(); i++)
		{

			//	Get the current handler

			SessionHandlerInterface handler = (SessionHandlerInterface) m_handlers.elementAt(i);

			if (handler.getHandlerName().equals(name))
				return handler;
		}

		//	Handler not found

		return null;
	}

	/**
	 * Remove a handler from the list
	 * 
	 * @param idx int
	 * @return SessionHandlerInterface
	 */
	public final SessionHandlerInterface remoteHandler(int idx)
	{

		//	Range check the index

		if (idx < 0 || idx >= m_handlers.size())
			return null;

		//	Remove the handler, and return it

		SessionHandlerInterface handler = (SessionHandlerInterface) m_handlers.elementAt(idx);
		m_handlers.removeElementAt(idx);
		return handler;
	}

	/**
	 * Remove a handler from the list
	 * 
	 * @param name String
	 * @return SessionHandlerInterface
	 */
	public final SessionHandlerInterface remoteHandler(String name)
	{

		//	Search for the required handler

		for (int i = 0; i < m_handlers.size(); i++)
		{

			//	Get the current handler

			SessionHandlerInterface handler = (SessionHandlerInterface) m_handlers.elementAt(i);

			if (handler.getHandlerName().equals(name))
			{

				//	Remove the handler from the list

				m_handlers.removeElementAt(i);
				return handler;
			}
		}

		//	Handler not found

		return null;
	}

	/**
	 * Remove all handlers from the list
	 */
	public final void removeAllHandlers()
	{
		m_handlers.removeAllElements();
	}
}
