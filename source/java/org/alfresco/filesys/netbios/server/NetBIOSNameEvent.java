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

import org.alfresco.filesys.netbios.NetBIOSName;

/**
 * NetBIOS name server event class.
 */
public class NetBIOSNameEvent
{
    /*
     * NetBIOS name event status codes
     */

    public static final int ADD_SUCCESS = 0; // local name added successfully
    public static final int ADD_FAILED = 1; // local name add failure
    public static final int ADD_DUPLICATE = 2; // local name already in use
    public static final int ADD_IOERROR = 3; // I/O error during add name broadcast
    public static final int QUERY_NAME = 4; // query for local name
    public static final int REGISTER_NAME = 5; // remote name registered
    public static final int REFRESH_NAME = 6; // name refresh
    public static final int REFRESH_IOERROR = 7; // refresh name I/O error

    /**
     * NetBIOS name details
     */

    private NetBIOSName m_name;

    /**
     * Name status
     */

    private int m_status;

    /**
     * Create a NetBIOS name event.
     * 
     * @param name NetBIOSName
     * @param sts int
     */
    protected NetBIOSNameEvent(NetBIOSName name, int sts)
    {
        m_name = name;
        m_status = sts;
    }

    /**
     * Return the NetBIOS name details.
     * 
     * @return NetBIOSName
     */
    public final NetBIOSName getNetBIOSName()
    {
        return m_name;
    }

    /**
     * Return the NetBIOS name status.
     * 
     * @return int
     */
    public final int getStatus()
    {
        return m_status;
    }
}