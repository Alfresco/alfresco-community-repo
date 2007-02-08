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
package org.alfresco.filesys.smb.dcerpc;

/**
 * DCE/RPC Writeable List Interface
 * <p>
 * A class that implements the DCEWriteableList interface can write a list of DCEWriteable objects
 * to a DCE/RPC buffer.
 */
public interface DCEWriteableList
{

    /**
     * Write the object state to DCE/RPC buffers.
     * 
     * @param buf DCEBuffer
     * @exception DCEBufferException
     */
    public void writeObject(DCEBuffer buf) throws DCEBufferException;
}
