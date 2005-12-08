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
package org.alfresco.filesys.smb.server;

import java.io.IOException;
import java.net.Socket;

import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.util.DataPacker;

/**
 * Tcpip SMB Packet Handler Class
 */
public class TcpipSMBPacketHandler extends PacketHandler
{

    /**
     * Class constructor
     * 
     * @param sock Socket
     * @exception IOException If a network error occurs
     */
    public TcpipSMBPacketHandler(Socket sock) throws IOException
    {
        super(sock, SMBSrvPacket.PROTOCOL_TCPIP, "TCP-SMB", "T");
    }

    /**
     * Read a packet from the input stream
     * 
     * @param pkt SMBSrvPacket
     * @return int
     * @exception IOexception If a network error occurs
     */
    public int readPacket(SMBSrvPacket pkt) throws IOException
    {

        // Read the packet header

        byte[] buf = pkt.getBuffer();
        int len = 0;

        while (len < RFCNetBIOSProtocol.HEADER_LEN && len != -1)
            len = readPacket(buf, len, RFCNetBIOSProtocol.HEADER_LEN - len);

        // Check if the connection has been closed, read length equals -1

        if (len == -1)
            return len;

        // Check if we received a valid header

        if (len < RFCNetBIOSProtocol.HEADER_LEN)
            throw new IOException("Invalid header, len=" + len);

        // Get the packet type from the header

        int typ = (int) (buf[0] & 0xFF);
        int dlen = (int) DataPacker.getShort(buf, 2);

        // Check for a large packet, add to the data length

        if (buf[1] != 0)
        {
            int llen = (int) buf[1];
            dlen += (llen << 16);
        }

        // Check if the packet buffer is large enough to hold the data + header

        if (buf.length < (dlen + RFCNetBIOSProtocol.HEADER_LEN))
        {

            // Allocate a new buffer to hold the data and copy the existing header

            byte[] newBuf = new byte[dlen + RFCNetBIOSProtocol.HEADER_LEN];
            for (int i = 0; i < 4; i++)
                newBuf[i] = buf[i];

            // Attach the new buffer to the SMB packet

            pkt.setBuffer(newBuf);
            buf = newBuf;
        }

        // Read the data part of the packet into the users buffer, this may take
        // several reads

        int offset = RFCNetBIOSProtocol.HEADER_LEN;
        int totlen = offset;

        while (dlen > 0)
        {

            // Read the data

            len = readPacket(buf, offset, dlen);

            // Check if the connection has been closed

            if (len == -1)
                return -1;

            // Update the received length and remaining data length

            totlen += len;
            dlen -= len;

            // Update the user buffer offset as more reads will be required
            // to complete the data read

            offset += len;

        } // end while reading data

        // Return the received packet length

        return totlen;
    }

    /**
     * Send a packet to the output stream
     * 
     * @param pkt SMBSrvPacket
     * @param len int
     * @exception IOexception If a network error occurs
     */
    public void writePacket(SMBSrvPacket pkt, int len) throws IOException
    {

        // Fill in the TCP SMB message header, this is already allocated as
        // part of the users buffer.

        byte[] buf = pkt.getBuffer();
        DataPacker.putInt(len, buf, 0);

        // Output the data packet

        int bufSiz = len + RFCNetBIOSProtocol.HEADER_LEN;
        writePacket(buf, 0, bufSiz);
    }
}
