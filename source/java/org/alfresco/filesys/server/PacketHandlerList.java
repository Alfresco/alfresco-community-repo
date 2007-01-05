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
 * Packet Handler List Class
 * 
 * @author GKSpencer
 */
public class PacketHandlerList {

	//	List of session handlers

	private Vector<PacketHandlerInterface> m_handlers;

	/**
	 * Default constructor
	 */
	public PacketHandlerList() {
		m_handlers = new Vector<PacketHandlerInterface>();
	}

	/**
	 * Add a handler to the list
	 * 
	 * @param handler PacketHandlerInterface
	 */
	public final void addHandler(PacketHandlerInterface handler) {
		m_handlers.addElement(handler);
	}

	/**
	 * Return the number of handlers in the list
	 * 
	 * @return int
	 */
	public final int numberOfHandlers() {
		return m_handlers.size();
	}

	/**
	 * Return the specified handler
	 * 
	 * @param idx int
	 * @return PacketHandlerInterface
	 */
	public final PacketHandlerInterface getHandlerAt(int idx) {

		//	Range check the index

		if (idx < 0 || idx >= m_handlers.size())
			return null;
		return (PacketHandlerInterface) m_handlers.elementAt(idx);
	}

	/**
	 * Remove a handler from the list
	 * 
	 * @param idx int
	 * @return PacketHandlerInterface
	 */
	public final PacketHandlerInterface remoteHandler(int idx) {

		//	Range check the index

		if (idx < 0 || idx >= m_handlers.size())
			return null;

		//	Remove the handler, and return it

		PacketHandlerInterface handler = (PacketHandlerInterface) m_handlers.elementAt(idx);
		m_handlers.removeElementAt(idx);
		return handler;
	}

	/**
	 * Remove all handlers from the list
	 */
	public final void removeAllHandlers() {
		m_handlers.removeAllElements();
	}
}
