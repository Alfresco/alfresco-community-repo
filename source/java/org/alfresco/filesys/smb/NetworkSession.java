/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.smb;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Network Session Interface
 * <p>
 * Base class for client network sessions.
 */
public interface NetworkSession
{

    /**
     * Return the protocol name
     * 
     * @return String
     */
    public String getProtocolName();

    /**
     * Open a connection to a remote host
     * 
     * @param toName Host name/address being called
     * @param fromName Local host name/address
     * @param toAddr Optional address of the remote host
     * @exception IOException
     * @exception UnknownHostException
     */
    public void Open(String toName, String fromName, String toAddr) throws IOException, UnknownHostException;

    /**
     * Determine if the session is connected to a remote host
     * 
     * @return boolean
     */
    public boolean isConnected();

    /**
     * Check if the network session has data available
     * 
     * @return boolean
     * @exception IOException
     */
    public boolean hasData() throws IOException;

    /**
     * Receive a data packet from the remote host.
     * 
     * @param buf Byte buffer to receive the data into.
     * @param tmo Receive timeout in milliseconds, or zero for no timeout
     * @return Length of the received data.
     * @exception java.io.IOException I/O error occurred.
     */
    public int Receive(byte[] buf, int tmo) throws IOException;

    /**
     * Send a data packet to the remote host.
     * 
     * @param data Byte array containing the data to be sent.
     * @param siz Length of the data to send.
     * @return true if the data was sent successfully, else false.
     * @exception java.io.IOException I/O error occurred.
     */
    public boolean Send(byte[] data, int siz) throws IOException;

    /**
     * Close the network session
     * 
     * @exception java.io.IOException I/O error occurred
     */
    public void Close() throws IOException;
}
