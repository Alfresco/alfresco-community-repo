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
package org.alfresco.filesys.smb.server.win32;

import java.io.IOException;

import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.netbios.win32.NetBIOSSocket;
import org.alfresco.filesys.netbios.win32.WinsockError;
import org.alfresco.filesys.netbios.win32.WinsockNetBIOSException;
import org.alfresco.filesys.smb.server.PacketHandler;
import org.alfresco.filesys.smb.server.SMBSrvPacket;

/**
 * Winsock NetBIOS Packet Handler Class
 * 
 * <p>Uses a Windows Winsock NetBIOS socket to provide the low level session layer for better integration
 * with Windows.
 *
 * @author GKSpencer
 */
public class WinsockNetBIOSPacketHandler extends PacketHandler
{
    // Constants
    //
    // Receive error indicating a receive buffer error

    private static final int ReceiveBufferSizeError  = 0x80000000;
    
    // Network LAN adapter to use

    private int m_lana;

    // NetBIOS session socket

    private NetBIOSSocket m_sessSock;
    
    /**
     * Class constructor
     * 
     * @param lana int
     * @param sock NetBIOSSocket
     */
    public WinsockNetBIOSPacketHandler(int lana, NetBIOSSocket sock)
    {
        super(SMBSrvPacket.PROTOCOL_WIN32NETBIOS, "WinsockNB", "WSNB", sock.getName().getName());

        m_lana     = lana;
        m_sessSock = sock;
    }
    
    /**
     * Return the LANA number
     * 
     * @return int
     */
    public final int getLANA()
    {
        return m_lana;
    }

    /**
     * Return the NetBIOS socket
     * 
     * @return NetBIOSSocket
     */
    public final NetBIOSSocket getSocket()
    {
        return m_sessSock;
    }

    /**
     * Read a packet from the client
     * 
     * @param pkt SMBSrvPacket
     * @return int
     * @throws IOException
     */
    public int readPacket(SMBSrvPacket pkt) throws IOException
    {
        // Receive an SMB/CIFS request packet via the Winsock NetBIOS socket
        
        int rxlen = 0;
        
        try {
            
            // Read a pakcet of data
            
            rxlen = m_sessSock.read(pkt.getBuffer(), 4, pkt.getBufferLength() - 4);
            
            // Check if the buffer is not big enough to receive the entire packet, extend the buffer
            // and read the remaining part of the packet
            
            if (rxlen == ReceiveBufferSizeError)
            {
                
                // Check if the packet buffer is already at the maximum size (we assume the maximum
                // size is the maximum that RFC NetBIOS can send which is 17bits)

                if (pkt.getBuffer().length < RFCNetBIOSProtocol.MaxPacketSize)
                {
                    // Set the initial receive size, assume a full read
                    
                    rxlen = pkt.getBufferLength() - 4;
                    
                    // Allocate a new buffer, copy the existing data to the new buffer

                    byte[] newbuf = new byte[RFCNetBIOSProtocol.MaxPacketSize];
                    System.arraycopy(pkt.getBuffer(), 4, newbuf, 4, rxlen);
                    pkt.setBuffer( newbuf);

                    // Receive the packet

                    int rxlen2 = m_sessSock.read(pkt.getBuffer(), rxlen + 4, pkt.getBufferLength() - (rxlen + 4));

                    System.out.println("Winsock rx2 len=" + rxlen2);
                    
                    if ( rxlen2 == ReceiveBufferSizeError)
                        throw new WinsockNetBIOSException(WinsockError.WsaEMsgSize);

                    rxlen += rxlen2;
                }
                else
                    throw new WinsockNetBIOSException(WinsockError.WsaEMsgSize);
            }
        }
        catch ( WinsockNetBIOSException ex)
        {
            // Check if the remote client has closed the socket
            
            if ( ex.getErrorCode() == WinsockError.WsaEConnReset ||
                     ex.getErrorCode() == WinsockError.WsaEDiscon)
            {
                // Indicate that the socket has been closed
                
                rxlen = -1;
            }
            else
            {
                // Rethrow the exception
                
                throw ex;
            }
        }
        
        // Return the received packet length
        
        return rxlen;
    }

    /**
     * Write a packet to the client
     * 
     * @param pkt SMBSrvPacket
     * @param len int
     * @throws IOException
     */
    public void writePacket(SMBSrvPacket pkt, int len) throws IOException
    {
        // Output the packet via the Winsock NetBIOS socket
        //
        // As Windows is handling the NetBIOS session layer we do not send the 4 byte header that is
        // used by the NetBIOS over TCP/IP and native SMB packet handlers.

        int txlen = m_sessSock.write(pkt.getBuffer(), 4, len);

        // Do not check the status, if the session has been closed the next receive will fail
    }
    
    /**
     * Close the Winsock NetBIOS packet handler.
     */
    public void closeHandler()
    {
        super.closeHandler();

        // Close the session socket

        if ( m_sessSock != null)
            m_sessSock.closeSocket();
    }
}
