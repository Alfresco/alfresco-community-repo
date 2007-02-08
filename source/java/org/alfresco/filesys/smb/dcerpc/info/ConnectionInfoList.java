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
package org.alfresco.filesys.smb.dcerpc.info;

import org.alfresco.filesys.smb.dcerpc.DCEBuffer;
import org.alfresco.filesys.smb.dcerpc.DCEBufferException;
import org.alfresco.filesys.smb.dcerpc.DCEList;
import org.alfresco.filesys.smb.dcerpc.DCEReadable;

/**
 * Connection Information List Class
 */
public class ConnectionInfoList extends DCEList
{

    /**
     * Default constructor
     */
    public ConnectionInfoList()
    {
        super();
    }

    /**
     * Class constructor
     * 
     * @param buf DCEBuffer
     * @exception DCEBufferException
     */
    public ConnectionInfoList(DCEBuffer buf) throws DCEBufferException
    {
        super(buf);
    }

    /**
     * Create a new connection information object
     * 
     * @return DCEReadable
     */
    protected DCEReadable getNewObject()
    {
        return new ConnectionInfo(getInformationLevel());
    }
}
