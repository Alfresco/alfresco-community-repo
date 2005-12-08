/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.filesys.netbios;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * NetBIOS Datagram Socket Class
 * <p>
 * Singleton class that allows multiple users of the socket.
 */
public class NetBIOSDatagramSocket
{
    // Global NetBIOS datagram socket instance

    private static NetBIOSDatagramSocket m_nbSocket;

    // Default port and bind address

    private static int m_defPort = RFCNetBIOSProtocol.DATAGRAM;
    private static InetAddress m_defBindAddr;

    // Datagram socket

    private DatagramSocket m_socket;

    // Broadcast address

    private InetAddress m_broadcastAddr;

    /**
     * Class constructor
     * 
     * @exception SocketException
     * @exception UnknownHostException
     */
    private NetBIOSDatagramSocket() throws SocketException, UnknownHostException
    {

        // Create the datagram socket

        if (m_defBindAddr == null)
            m_socket = new DatagramSocket(m_defPort);
        else
            m_socket = new DatagramSocket(m_defPort, m_defBindAddr);

        // Generate the broadcast mask

        if (m_defBindAddr == null)
            m_broadcastAddr = InetAddress.getByName(NetworkSettings.GenerateBroadcastMask(null));
        else
            m_broadcastAddr = InetAddress.getByName(NetworkSettings.GenerateBroadcastMask(m_defBindAddr
                    .getHostAddress()));
    }

    /**
     * Return the global NetBIOS datagram instance
     * 
     * @return NetBIOSDatagramSocket
     * @exception SocketException
     * @exception UnknownHostException
     */
    public final static synchronized NetBIOSDatagramSocket getInstance() throws SocketException, UnknownHostException
    {

        // Check if the datagram socket has been created

        if (m_nbSocket == null)
            m_nbSocket = new NetBIOSDatagramSocket();

        // Return the global NetBIOS datagram socket instance

        return m_nbSocket;
    }

    /**
     * Set the default port to use
     * 
     * @param port int
     */
    public final static void setDefaultPort(int port)
    {
        m_defPort = port;
    }

    /**
     * Set the address to bind the datagram socket to
     * 
     * @param bindAddr InetAddress
     */
    public final static void setBindAddress(InetAddress bindAddr)
    {
        m_defBindAddr = bindAddr;
    }

    /**
     * Receive a NetBIOS datagram
     * 
     * @param dgram NetBIOSDatagram
     * @return int
     * @exception IOException
     */
    public final int receiveDatagram(NetBIOSDatagram dgram) throws IOException
    {

        // Create a datagram packet using the NetBIOS datagram buffer

        DatagramPacket pkt = new DatagramPacket(dgram.getBuffer(), dgram.getBuffer().length);

        // Receive a datagram

        m_socket.receive(pkt);
        return pkt.getLength();
    }

    /**
     * Send a NetBIOS datagram
     * 
     * @param dgram NetBIOSDatagram
     * @param destAddr InetAddress
     * @param destPort int
     * @exception IOException
     */
    public final void sendDatagram(NetBIOSDatagram dgram, InetAddress destAddr, int destPort) throws IOException
    {

        // Create a datagram packet using the NetBIOS datagram buffer

        DatagramPacket pkt = new DatagramPacket(dgram.getBuffer(), dgram.getLength(), destAddr, destPort);

        // Send the NetBIOS datagram

        m_socket.send(pkt);
    }

    /**
     * Send a broadcast NetBIOS datagram
     * 
     * @param dgram NetBIOSDatagram
     * @exception IOException
     */
    public final void sendBroadcastDatagram(NetBIOSDatagram dgram) throws IOException
    {

        // Create a datagram packet using the NetBIOS datagram buffer

        DatagramPacket pkt = new DatagramPacket(dgram.getBuffer(), dgram.getLength(), m_broadcastAddr, m_defPort);

        // Send the NetBIOS datagram

        m_socket.send(pkt);
    }
}
