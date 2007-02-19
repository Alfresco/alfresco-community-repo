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
import org.alfresco.filesys.netbios.win32.NetBIOS;
import org.alfresco.filesys.netbios.win32.Win32NetBIOS;
import org.alfresco.filesys.smb.server.PacketHandler;
import org.alfresco.filesys.smb.server.SMBSrvPacket;

/**
 * Win32 NetBIOS Packet Handler Class
 * 
 * <p>Uses the Win32 Netbios() call to provide the low level session layer for better integration with
 * Windows.
 * 
 * @author GKSpencer
 */
public class Win32NetBIOSPacketHandler extends PacketHandler
{

    // Constants
    //
    // Receive error encoding and length masks

    private static final int ReceiveErrorMask = 0xFF000000;
    private static final int ReceiveLengthMask = 0x0000FFFF;

    // Network LAN adapter to use

    private int m_lana;

    // NetBIOS session id

    private int m_lsn;

    /**
     * Class constructor
     * 
     * @param lana int
     * @param lsn int
     * @param callerName String
     */
    public Win32NetBIOSPacketHandler(int lana, int lsn, String callerName)
    {
        super(SMBSrvPacket.PROTOCOL_WIN32NETBIOS, "Win32NB", "WNB", callerName);

        m_lana = lana;
        m_lsn = lsn;
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
     * Return the NetBIOS session id
     * 
     * @return int
     */
    public final int getLSN()
    {
        return m_lsn;
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

        // Wait for a packet on the Win32 NetBIOS session
        //
        // As Windows is handling the NetBIOS session layer we only receive the SMB packet. In order
        // to be compatible with the other packet handlers we allow for the 4 byte header.

        int pktLen = pkt.getBuffer().length;
        if (pktLen > NetBIOS.MaxReceiveSize)
            pktLen = NetBIOS.MaxReceiveSize;

        int rxLen = Win32NetBIOS.Receive(m_lana, m_lsn, pkt.getBuffer(), 4, pktLen - 4);

        if ((rxLen & ReceiveErrorMask) != 0)
        {

            // Check for an incomplete message status code

            int sts = (rxLen & ReceiveErrorMask) >> 24;

            if (sts == NetBIOS.NRC_Incomp)
            {

                // Check if the packet buffer is already at the maximum size (we assume the maximum
                // size is the maximum that RFC NetBIOS can send which is 17bits)

                if (pkt.getBuffer().length < RFCNetBIOSProtocol.MaxPacketSize)
                {

                    // Allocate a new buffer

                    byte[] newbuf = new byte[RFCNetBIOSProtocol.MaxPacketSize];

                    // Copy the first part of the received data to the new buffer

                    System.arraycopy(pkt.getBuffer(), 4, newbuf, 4, pktLen - 4);

                    // Move the new buffer in as the main packet buffer

                    pkt.setBuffer(newbuf);

                    // DEBUG

                    // Debug.println("readPacket() extended buffer to " + pkt.getBuffer().length);
                }

                // Set the original receive size

                rxLen = (rxLen & ReceiveLengthMask);

                // Receive the remaining data
                //
                // Note: If the second read request is issued with a size of 64K or 64K-4 it returns
                // with another incomplete status and returns no data.

                int rxLen2 = Win32NetBIOS.Receive(m_lana, m_lsn, pkt.getBuffer(), rxLen + 4, 32768);

                if ((rxLen2 & ReceiveErrorMask) != 0)
                {
                    sts = (rxLen2 & ReceiveErrorMask) >> 24;
                    throw new IOException("Win32 NetBIOS multi-part receive failed, sts=0x" + sts + ", err="
                            + NetBIOS.getErrorString(sts));
                }

                // Set the total received data length

                rxLen += rxLen2;
            }
            else
            {

                // Indicate that the session has closed

                return -1;
            }
        }

        // Return the received data length

        return rxLen;
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

        // Output the packet on the Win32 NetBIOS session
        //
        // As Windows is handling the NetBIOS session layer we do not send the 4 byte header that is
        // used by the NetBIOS over TCP/IP and native SMB packet handlers.

        int sts = Win32NetBIOS.Send(m_lana, m_lsn, pkt.getBuffer(), 4, len);

        // Do not check the status, if the session has been closed the next receive will fail
    }

    /**
     * Close the Win32 NetBIOS packet handler. Hangup the NetBIOS session
     */
    public void closeHandler()
    {
        super.closeHandler();

        // Hangup the Win32 NetBIOS session

        Win32NetBIOS.Hangup(m_lana, m_lsn);
    }
}
