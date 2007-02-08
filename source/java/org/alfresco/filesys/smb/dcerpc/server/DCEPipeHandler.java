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
package org.alfresco.filesys.smb.dcerpc.server;

/**
 * DCE Pipe Handler Class
 * <p>
 * Contains a list of the available DCE pipe handlers.
 */
public class DCEPipeHandler
{

    // DCE/RPC pipe request handlers

    private static DCEHandler[] _handlers = {
            new SrvsvcDCEHandler(),
            null, // samr
            null, // winreg
            new WkssvcDCEHandler(),
            null, // NETLOGON
            null, // lsarpc
            null, // spoolss
            null, // netdfs
            null, // service control
            null, // eventlog
            null // netlogon1
    };

    /**
     * Return the DCE/RPC request handler for the pipe type
     * 
     * @param typ int
     * @return DCEHandler
     */
    public final static DCEHandler getHandlerForType(int typ)
    {
        if (typ >= 0 && typ < _handlers.length)
            return _handlers[typ];
        return null;
    }
}
