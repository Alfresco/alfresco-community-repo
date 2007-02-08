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
package org.alfresco.filesys.netbios.server;

import java.net.InetAddress;

/**
 * NetBIOS name query listener interface.
 */
public interface QueryNameListener
{

    /**
     * Signal that a NetBIOS name query has been received, for the specified local NetBIOS name.
     * 
     * @param evt Local NetBIOS name details.
     * @param addr IP address of the remote node that sent the name query request.
     */
    public void netbiosNameQuery(NetBIOSNameEvent evt, InetAddress addr);
}