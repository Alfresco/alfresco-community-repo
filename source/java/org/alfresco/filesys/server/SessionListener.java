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
package org.alfresco.filesys.server;

/**
 * <p>
 * The session listener interface provides a hook into the server so that an application is notified
 * when a new session is created and closed by a network server.
 */
public interface SessionListener
{

    /**
     * Called when a network session is closed.
     * 
     * @param sess Network session details.
     */
    public void sessionClosed(SrvSession sess);

    /**
     * Called when a new network session is created by a network server.
     * 
     * @param sess Network session that has been created for the new connection.
     */
    public void sessionCreated(SrvSession sess);

    /**
     * Called when a user logs on to a network server
     * 
     * @param sess Network session that has been logged on.
     */
    public void sessionLoggedOn(SrvSession sess);
}