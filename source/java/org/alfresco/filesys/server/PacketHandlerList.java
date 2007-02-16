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
