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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.smb.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Protocol Packet Handler Interface
 */
public abstract class PacketHandler
{

    // Protocol type and name

    private int m_protoType;
    private String m_protoName;
    private String m_shortName;

    // Socket that this session is using.

    private Socket m_socket;

    // Input/output streams for receiving/sending SMB requests.

    private DataInputStream m_in;
    private DataOutputStream m_out;

    // Client caller name

    private String m_clientName;

    /**
     * Class constructor
     * 
     * @param sock Socket
     * @param typ int
     * @param name String
     * @param shortName String
     * @exception IOException If a network error occurs
     */
    public PacketHandler(Socket sock, int typ, String name, String shortName) throws IOException
    {
        m_socket = sock;
        m_protoType = typ;
        m_protoName = name;
        m_shortName = shortName;

        // Set socket options

        sock.setTcpNoDelay(true);

        // Open the input/output streams

        m_in = new DataInputStream(m_socket.getInputStream());
        m_out = new DataOutputStream(m_socket.getOutputStream());
    }

    /**
     * Class constructor
     * 
     * @param typ int
     * @param name String
     * @param shortName String
     */
    public PacketHandler(int typ, String name, String shortName, String clientName)
    {
        m_protoType = typ;
        m_protoName = name;
        m_shortName = shortName;

        m_clientName = clientName;
    }

    /**
     * Return the protocol type
     * 
     * @return int
     */
    public final int isProtocol()
    {
        return m_protoType;
    }

    /**
     * Return the protocol name
     * 
     * @return String
     */
    public final String isProtocolName()
    {
        return m_protoName;
    }

    /**
     * Return the short protocol name
     * 
     * @return String
     */
    public final String getShortName()
    {
        return m_shortName;
    }

    /**
     * Check if there is a remote address available
     * 
     * @return boolean
     */
    public final boolean hasRemoteAddress()
    {
        return m_socket != null ? true : false;
    }

    /**
     * Return the remote address for the socket connection
     * 
     * @return InetAddress
     */
    public final InetAddress getRemoteAddress()
    {
        return m_socket != null ? m_socket.getInetAddress() : null;
    }

    /**
     * Determine if the client name is available
     * 
     * @return boolean
     */
    public final boolean hasClientName()
    {
        return m_clientName != null ? true : false;
    }

    /**
     * Return the client name
     * 
     * @return
     */
    public final String getClientName()
    {
        return m_clientName;
    }

    /**
     * Return the count of available bytes in the receive input stream
     * 
     * @return int
     * @exception IOException If a network error occurs.
     */
    public final int availableBytes() throws IOException
    {
        if (m_in != null)
            return m_in.available();
        return 0;
    }

    /**
     * Read a packet
     * 
     * @param pkt byte[]
     * @param off int
     * @param len int
     * @return int
     * @exception IOException If a network error occurs.
     */
    public final int readPacket(byte[] pkt, int off, int len) throws IOException
    {

        // Read a packet of data

        if (m_in != null)
            return m_in.read(pkt, off, len);
        return 0;
    }

    /**
     * Receive an SMB request packet
     * 
     * @param pkt SMBSrvPacket
     * @return int
     * @exception IOException If a network error occurs.
     */
    public abstract int readPacket(SMBSrvPacket pkt) throws IOException;

    /**
     * Send an SMB request packet
     * 
     * @param pkt byte[]
     * @param off int
     * @param len int
     * @exception IOException If a network error occurs.
     */
    public final void writePacket(byte[] pkt, int off, int len) throws IOException
    {

        // Output the raw packet

        if (m_out != null)
            m_out.write(pkt, off, len);
    }

    /**
     * Send an SMB response packet
     * 
     * @param pkt SMBSrvPacket
     * @param len int
     * @exception IOException If a network error occurs.
     */
    public abstract void writePacket(SMBSrvPacket pkt, int len) throws IOException;

    /**
     * Send an SMB response packet
     * 
     * @param pkt SMBSrvPacket
     * @exception IOException If a network error occurs.
     */
    public final void writePacket(SMBSrvPacket pkt) throws IOException
    {
        writePacket(pkt, pkt.getLength());
    }

    /**
     * Flush the output socket
     * 
     * @exception IOException If a network error occurs
     */
    public final void flushPacket() throws IOException
    {
        if (m_out != null)
            m_out.flush();
    }

    /**
     * Close the protocol handler
     */
    public void closeHandler()
    {

        // Close the input stream

        if (m_in != null)
        {
            try
            {
                m_in.close();
            }
            catch (Exception ex)
            {
            }
            m_in = null;
        }

        // Close the output stream

        if (m_out != null)
        {
            try
            {
                m_out.close();
            }
            catch (Exception ex)
            {
            }
            m_out = null;
        }

        // Close the socket

        if (m_socket != null)
        {
            try
            {
                m_socket.close();
            }
            catch (Exception ex)
            {
            }
            m_socket = null;
        }
    }
}
