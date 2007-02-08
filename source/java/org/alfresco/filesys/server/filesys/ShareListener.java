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
package org.alfresco.filesys.server.filesys;

import org.alfresco.filesys.server.SrvSession;

/**
 * <p>
 * The share listener interface provides a hook into the server so that an application is notified
 * when a session connects/disconnects from a particular share.
 */
public interface ShareListener
{

    /**
     * Called when a session connects to a share
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     */
    public void shareConnect(SrvSession sess, TreeConnection tree);

    /**
     * Called when a session disconnects from a share
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     */
    public void shareDisconnect(SrvSession sess, TreeConnection tree);
}
