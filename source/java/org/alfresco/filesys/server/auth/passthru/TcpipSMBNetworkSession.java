/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.server.auth.passthru;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.smb.NetworkSession;
import org.alfresco.filesys.smb.TcpipSMB;
import org.alfresco.filesys.util.DataPacker;

/**
 * Native TCP/IP SMB Network Session Class
 */
public class TcpipSMBNetworkSession implements NetworkSession
{

    // Default socket timeout value

    private static int _defTimeout = 30000; // 30 seconds, in milliseconds

    // Socket used to connect and read/write to remote host

    private Socket m_socket;

    // Input and output data streams, from the socket network connection

    private DataInputStream m_in;
    private DataOutputStream m_out;

    // Socket timeout

    private int m_tmo = _defTimeout;

    // Debug enable flag and debug output stream

    private static boolean m_debug = false;
    private static PrintStream m_dbg = System.out;

    /**
     * Default constructor
     */
    public TcpipSMBNetworkSession()
    {
    }

    /**
     * Class constructor
     * 
     * @param tmo Socket timeout, in milliseconds
     */
    public TcpipSMBNetworkSession(int tmo)
    {
        m_tmo = tmo;
    }

    /**
     * Return the protocol name
     * 
     * @return String
     */
    public String getProtocolName()
    {
        return "Native SMB (port 445)";
    }

    /**
     * Open a connection to a remote host
     * 
     * @param toName Host name/address being called
     * @param fromName Local host name/address
     * @param toAddr Optional address
     * @exception IOException
     */
    public void Open(String toName, String fromName, String toAddr) throws IOException, UnknownHostException
    {

        // Create the socket

        m_socket = new Socket(toName, TcpipSMB.PORT);

        // Enable the timeout on the socket, disable the Nagle algorithm

        m_socket.setSoTimeout(m_tmo);
        m_socket.setTcpNoDelay(true);

        // Attach input/output streams to the socket

        m_in = new DataInputStream(m_socket.getInputStream());
        m_out = new DataOutputStream(m_socket.getOutputStream());
    }

    /**
     * Determine if the session is connected to a remote host
     * 
     * @return boolean
     */
    public boolean isConnected()
    {
        return m_socket != null ? true : false;
    }

    /**
     * Check if there is data available on this network session
     * 
     * @return boolean
     * @exception IOException
     */
    public final boolean hasData() throws IOException
    {

        // Check if the connection is active

        if (m_socket == null || m_in == null)
            return false;

        // Check if there is data available

        return m_in.available() > 0 ? true : false;
    }

    /**
     * Receive a data packet from the remote host.
     * 
     * @param buf Byte buffer to receive the data into.
     * @param tmo Receive timeout in milliseconds, or zero for no timeout
     * @return Length of the received data.
     * @exception java.io.IOException I/O error occurred.
     */
    public int Receive(byte[] buf, int tmo) throws IOException
    {

        // Set the read timeout

        m_socket.setSoTimeout(tmo);

        // Read a data packet of data

        int rdlen = m_in.read(buf, 0, RFCNetBIOSProtocol.HEADER_LEN);

        // Check if a header was received

        if (rdlen < RFCNetBIOSProtocol.HEADER_LEN)
            throw new java.io.IOException("TCP/IP SMB Short Read");

        // Get the packet data length

        int pktlen = DataPacker.getInt(buf, 0);

        // Debug mode

        if (m_debug)
            m_dbg.println("TcpSMB: Rx " + pktlen + " bytes");

        // Read the data part of the packet into the users buffer, this may take
        // several reads

        int totlen = 0;
        int offset = RFCNetBIOSProtocol.HEADER_LEN;

        while (pktlen > 0)
        {

            // Read the data

            rdlen = m_in.read(buf, offset, pktlen);

            // Update the received length and remaining data length

            totlen += rdlen;
            pktlen -= rdlen;

            // Update the user buffer offset as more reads will be required
            // to complete the data read

            offset += rdlen;

        } // end while reading data

        // Return the received data length, not including the header

        return totlen;
    }

    /**
     * Send a data packet to the remote host.
     * 
     * @param data Byte array containing the data to be sent.
     * @param siz Length of the data to send.
     * @return true if the data was sent successfully, else false.
     * @exception java.io.IOException I/O error occurred.
     */
    public boolean Send(byte[] data, int siz) throws IOException
    {

        // Pack the data length as the first four bytes of the packet

        DataPacker.putInt(siz, data, 0);

        // Send the packet to the remote host

        int len = siz + RFCNetBIOSProtocol.HEADER_LEN;
        m_out.write(data, 0, len);
        return true;
    }

    /**
     * Close the network session
     * 
     * @exception java.io.IOException I/O error occurred
     */
    public void Close() throws IOException
    {

        // Close the input/output streams

        if (m_in != null)
        {
            m_in.close();
            m_in = null;
        }

        if (m_out != null)
        {
            m_out.close();
            m_out = null;
        }

        // Close the socket

        if (m_socket != null)
        {
            m_socket.close();
            m_socket = null;
        }
    }

    /**
     * Enable/disable session debugging output
     * 
     * @param dbg true to enable debugging, else false
     */
    public static void setDebug(boolean dbg)
    {
        m_debug = dbg;
    }

    /**
     * Return the default socket timeout value
     * 
     * @return int
     */
    public static final int getDefaultTimeout()
    {
        return _defTimeout;
    }

    /**
     * Set the default socket timeout for new sessions
     * 
     * @param tmo int
     */
    public static final void setDefaultTimeout(int tmo)
    {
        _defTimeout = tmo;
    }
}
