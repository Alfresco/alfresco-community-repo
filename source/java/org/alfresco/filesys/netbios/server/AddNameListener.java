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

/**
 * NetBIOS add name listener interface.
 */
public interface AddNameListener
{
    /**
     * Signal that a NetBIOS name has been added, or an error occurred whilst trying to add a new
     * NetBIOS name.
     * 
     * @param evt NetBIOSNameEvent
     */
    public void netbiosNameAdded(NetBIOSNameEvent evt);
}