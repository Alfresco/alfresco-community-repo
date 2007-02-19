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
package org.alfresco.filesys.smb.server;

import java.io.IOException;

import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.smb.PacketType;
import org.alfresco.filesys.smb.TransactBuffer;
import org.alfresco.filesys.util.DataBuffer;
import org.alfresco.filesys.util.DataPacker;

/**
 * SMB server transact packet class
 */
class SMBSrvTransPacket extends SMBTransPacket
{

    // Define the number of standard parameters for a server response

    private static final int StandardParamsResponse = 10;

    // Offset to the setup response paramaters

    protected static final int SetupOffsetResponse = PARAMWORDS + (StandardParamsResponse * 2);

    /**
     * Construct an SMB transaction packet
     * 
     * @param buf Buffer that contains the SMB transaction packet.
     */

    public SMBSrvTransPacket(byte[] buf)
    {
        super(buf);
    }

    /**
     * Construct an SMB transaction packet
     * 
     * @param siz Size of packet to allocate.
     */

    public SMBSrvTransPacket(int siz)
    {
        super(siz);

        // Set the multiplex id for this transaction

        setMultiplexId(getNextMultiplexId());
    }

    /**
     * Initialize the transact reply parameters.
     * 
     * @param pkt Reply SMB packet.
     * @param prmCnt Count of returned parameter bytes.
     * @param prmPos Starting offset to the parameter block.
     * @param dataCnt Count of returned data bytes.
     * @param dataPos Starting offset to the data block.
     */
    public final static void initTransactReply(SMBSrvPacket pkt, int prmCnt, int prmPos, int dataCnt, int dataPos)
    {

        // Set the total parameter words

        pkt.setParameterCount(10);

        // Set the total parameter/data bytes

        pkt.setParameter(0, prmCnt);
        pkt.setParameter(1, dataCnt);

        // Clear the reserved parameter

        pkt.setParameter(2, 0);

        // Set the parameter byte count/offset for this packet

        pkt.setParameter(3, prmCnt);
        pkt.setParameter(4, prmPos - RFCNetBIOSProtocol.HEADER_LEN);

        // Set the parameter displacement

        pkt.setParameter(5, 0);

        // Set the data byte count/offset for this packet

        pkt.setParameter(6, dataCnt);
        pkt.setParameter(7, dataPos - RFCNetBIOSProtocol.HEADER_LEN);

        // Set the data displacement

        pkt.setParameter(8, 0);

        // Set up word count

        pkt.setParameter(9, 0);
    }

    /**
     * Calculate the data item size from the data descriptor string.
     * 
     * @param desc java.lang.String
     * @return int
     */
    protected final static int CalculateDataItemSize(String desc)
    {

        // Scan the data descriptor string and calculate the data item size

        int len = 0;
        int pos = 0;

        while (pos < desc.length())
        {

            // Get the current data item type

            char dtype = desc.charAt(pos++);
            int dlen = 1;

            // Check if a data length has been specified

            if (pos < desc.length() && Character.isDigit(desc.charAt(pos)))
            {

                // Convert the data length string

                int numlen = 1;
                int numpos = pos + 1;
                while (numpos < desc.length() && Character.isDigit(desc.charAt(numpos++)))
                    numlen++;

                // Set the data length

                dlen = Integer.parseInt(desc.substring(pos, pos + numlen));

                // Update the descriptor string position

                pos = numpos - 1;
            }

            // Convert the current data item

            switch (dtype)
            {

            // Word (16 bit) data type

            case 'W':
                len += 2;
                break;

            // Integer (32 bit) data type

            case 'D':
                len += 4;
                break;

            // Byte data type, may be multiple bytes if 'B<n>'

            case 'B':
                len += dlen;
                break;

            // Null terminated string data type, offset into buffer only

            case 'z':
                len += 4;
                break;

            // Skip 'n' bytes in the buffer

            case '.':
                len += dlen;
                break;

            // Integer (32 bit) data type converted to a date/time value

            case 'T':
                len += 4;
                break;

            } // end switch data type

        } // end while descriptor string

        // Return the data length of each item

        return len;
    }

    /**
     * Return the offset to the data block within the SMB packet. The data block is word aligned
     * within the byte buffer area of the SMB packet. This method must be called after the parameter
     * count has been set.
     * 
     * @param prmLen Parameter block length, in bytes.
     * @return int Offset to the data block area.
     */
    public final int getDataBlockOffset(int prmLen)
    {

        // Get the position of the parameter block

        int pos = getParameterBlockOffset() + prmLen;
        if ((pos & 0x01) != 0)
            pos++;
        return pos;
    }

    /**
     * Return the data block offset.
     * 
     * @return int Offset to data block within packet.
     */
    public final int getRxDataBlock()
    {
        return getParameter(12) + RFCNetBIOSProtocol.HEADER_LEN;
    }

    /**
     * Return the received transaction data block length.
     * 
     * @return int
     */
    public final int getRxDataBlockLength()
    {
        return getParameter(11);
    }

    /**
     * Get the required transact parameter word (16 bit).
     * 
     * @param prmIdx int
     * @return int
     */
    public final int getRxParameter(int prmIdx)
    {

        // Get the parameter block offset

        int pos = getRxParameterBlock();

        // Get the required transact parameter word.

        pos += prmIdx * 2; // 16 bit words
        return DataPacker.getIntelShort(getBuffer(), pos);
    }

    /**
     * Return the position of the parameter block within the received packet.
     * 
     * @param prmblk Array to unpack the parameter block words into.
     */

    public final int getRxParameterBlock()
    {

        // Get the offset to the parameter words, add the NetBIOS header length
        // to the offset.

        return getParameter(10) + RFCNetBIOSProtocol.HEADER_LEN;
    }

    /**
     * Return the received transaction parameter block length.
     * 
     * @return int
     */
    public final int getRxParameterBlockLength()
    {
        return getParameter(9);
    }

    /**
     * Return the received transaction setup parameter count.
     * 
     * @return int
     */
    public final int getRxParameterCount()
    {
        return getParameterCount() - STD_PARAMS;
    }

    /**
     * Get the required transact parameter int value (32-bit).
     * 
     * @param prmIdx int
     * @return int
     */
    public final int getRxParameterInt(int prmIdx)
    {

        // Get the parameter block offset

        int pos = getRxParameterBlock();

        // Get the required transact parameter word.

        pos += prmIdx * 2; // 16 bit words
        return DataPacker.getIntelInt(getBuffer(), pos);
    }

    /**
     * Get the required transact parameter string.
     * 
     * @param pos Offset to the string within the parameter block.
     * @param uni Unicode if true, else ASCII
     * @return int
     */
    public final String getRxParameterString(int pos, boolean uni)
    {

        // Get the parameter block offset

        pos += getRxParameterBlock();

        // Get the transact parameter string

        byte[] buf = getBuffer();
        int len = (buf[pos++] & 0x00FF);
        return DataPacker.getString(buf, pos, len, uni);
    }

    /**
     * Get the required transact parameter string.
     * 
     * @param pos Offset to the string within the parameter block.
     * @param len Length of the string.
     * @param uni Unicode if true, else ASCII
     * @return int
     */
    public final String getRxParameterString(int pos, int len, boolean uni)
    {

        // Get the parameter block offset

        pos += getRxParameterBlock();

        // Get the transact parameter string

        byte[] buf = getBuffer();
        return DataPacker.getString(buf, pos, len, uni);
    }

    /**
     * Return the received transaction name.
     * 
     * @return java.lang.String
     */
    public final String getRxTransactName()
    {

        // Check if the transaction has a name

        if (getCommand() == PacketType.Transaction2)
            return "";

        // Unpack the transaction name string

        int pos = getByteOffset();
        return DataPacker.getString(getBuffer(), pos, getByteCount());
    }

    /**
     * Return the setup parameter count
     * 
     * @return int
     */
    public final int getSetupCount()
    {
        return getParameter(13) & 0xFF;
    }

    /**
     * Return the buffer offset to the setup parameters
     * 
     * @return int
     */
    public final int getSetupOffset()
    {
        return WORDCNT + 29; // 14 setup words + word count byte
    }

    /**
     * Return the specified transaction setup parameter.
     * 
     * @param idx Setup parameter index.
     * @return int
     */

    public final int getSetupParameter(int idx)
    {

        // Check if the setup parameter index is valid

        if (idx >= getRxParameterCount())
            throw new java.lang.ArrayIndexOutOfBoundsException();

        // Get the setup parameter

        return getParameter(idx + STD_PARAMS);
    }

    /**
     * Return the maximum return paramater byte count
     * 
     * @return int
     */
    public final int getMaximumReturnParameterCount()
    {
        return getParameter(2);
    }

    /**
     * Return the maximum return data byte count
     * 
     * @return int
     */
    public final int getMaximumReturnDataCount()
    {
        return getParameter(3);
    }

    /**
     * Return the maximum return setup count
     * 
     * @return int
     */
    public final int getMaximumReturnSetupCount()
    {
        return getParameter(4);
    }

    /**
     * Return the specified transaction setup parameter 32bit value.
     * 
     * @param idx Setup parameter index.
     * @return int
     */

    public final int getSetupParameterInt(int idx)
    {

        // Check if the setup parameter index is valid

        if (idx >= getRxParameterCount())
            throw new java.lang.ArrayIndexOutOfBoundsException();

        // Get the setup parameter

        return getParameterLong(idx + STD_PARAMS);
    }

    /**
     * Set the total parameter block length, in bytes
     * 
     * @param cnt int
     */
    public final void setTotalParameterCount(int cnt)
    {
        setParameter(0, cnt);
    }

    /**
     * Set the total data block length, in bytes
     * 
     * @param cnt int
     */
    public final void setTotalDataCount(int cnt)
    {
        setParameter(1, cnt);
    }

    /**
     * Set the parameter block count for this packet
     * 
     * @param len int
     */
    public final void setParameterBlockCount(int len)
    {
        setParameter(3, len);
    }

    /**
     * Set the parameter block offset
     * 
     * @param off int
     */
    public final void setParameterBlockOffset(int off)
    {
        setParameter(4, off != 0 ? off - RFCNetBIOSProtocol.HEADER_LEN : 0);
    }

    /**
     * Set the parameter block displacement within the total parameter block
     * 
     * @param disp int
     */
    public final void setParameterBlockDisplacement(int disp)
    {
        setParameter(5, disp);
    }

    /**
     * Set the data block count for this packet
     * 
     * @param len int
     */
    public final void setDataBlockCount(int len)
    {
        setParameter(6, len);
    }

    /**
     * Set the data block offset, from the start of the packet
     * 
     * @param off int
     */
    public final void setDataBlockOffset(int off)
    {
        setParameter(7, off != 0 ? off - RFCNetBIOSProtocol.HEADER_LEN : 0);
    }

    /**
     * Set the data block displacement within the total data block
     * 
     * @param disp int
     */
    public final void setDataBlockDisplacement(int disp)
    {
        setParameter(8, disp);
    }

    /**
     * Send one or more transaction response SMBs to the client
     * 
     * @param sess SMBSrvSession
     * @param tbuf TransactBuffer
     * @exception java.io.IOException If an I/O error occurs.
     */
    protected final void doTransactionResponse(SMBSrvSession sess, TransactBuffer tbuf) throws IOException
    {

        // Initialize the transaction response packet

        setCommand(tbuf.isType());

        // Get the individual buffers from the transact buffer

        tbuf.setEndOfBuffer();

        DataBuffer setupBuf = tbuf.getSetupBuffer();
        DataBuffer paramBuf = tbuf.getParameterBuffer();
        DataBuffer dataBuf = tbuf.getDataBuffer();

        // Set the parameter count

        if (tbuf.hasSetupBuffer())
            setParameterCount(StandardParamsResponse + setupBuf.getLengthInWords());
        else
            setParameterCount(StandardParamsResponse);

        // Clear the parameters

        for (int i = 0; i < getParameterCount(); i++)
            setParameter(i, 0);

        // Get the total parameter/data block lengths

        int totParamLen = paramBuf != null ? paramBuf.getLength() : 0;
        int totDataLen = dataBuf != null ? dataBuf.getLength() : 0;

        // Initialize the parameters

        setTotalParameterCount(totParamLen);
        setTotalDataCount(totDataLen);

        // Get the available data space within the packet

        int availBuf = getAvailableLength();
        int clientLen = getAvailableLength(sess.getClientMaximumBufferSize());
        if (availBuf > clientLen)
            availBuf = clientLen;

        // Check if the transaction parameter block and data block will fit within a single request
        // packet

        int plen = totParamLen;
        int dlen = totDataLen;

        if ((plen + dlen) > availBuf)
        {

            // Calculate the parameter/data block sizes to send in the first request packet

            if (plen > 0)
            {

                // Check if the parameter block can fit into the packet

                if (plen <= availBuf)
                {

                    // Pack all of the parameter block and fill the remaining buffer with the data
                    // block

                    if (dlen > 0)
                        dlen = availBuf - plen;
                }
                else
                {

                    // Split the parameter/data space in the packet

                    plen = availBuf / 2;
                    dlen = plen;
                }
            }
            else if (dlen > availBuf)
            {

                // Fill the packet with the first section of the data block

                dlen = availBuf;
            }
        }

        // Set the parameter/data block counts for this packet

        setParameterBlockCount(plen);
        setDataBlockCount(dlen);

        // Pack the setup bytes

        if (setupBuf != null)
            setupBuf.copyData(getBuffer(), SetupOffsetResponse);

        // Pack the parameter block

        int pos = DataPacker.wordAlign(getByteOffset());
        setPosition(pos);

        // Set the parameter block offset, from the start of the SMB packet

        setParameterBlockCount(plen);
        setParameterBlockOffset(pos);

        int packLen = -1;

        if (paramBuf != null)
        {

            // Pack the parameter block

            packLen = paramBuf.copyData(getBuffer(), pos, plen);

            // Update the buffer position for the data block

            pos = DataPacker.longwordAlign(pos + packLen);
            setPosition(pos);
        }

        // Set the data block offset

        setDataBlockCount(dlen);
        setDataBlockOffset(pos);

        // Pack the data block

        if (dataBuf != null)
        {

            // Pack the data block

            packLen = dataBuf.copyData(getBuffer(), pos, dlen);

            // Update the end of buffer position

            setPosition(pos + packLen);
        }

        // Set the byte count for the SMB packet

        setByteCount();

        // Send the start of the transaction request

        sess.sendResponseSMB(this);

        // Get the available parameter/data block buffer space for the secondary packet

        availBuf = getAvailableLength();
        if (availBuf > clientLen)
            availBuf = clientLen;

        // Loop until all parameter/data block data has been sent to the server

        TransactBuffer rxBuf = null;

        while ((paramBuf != null && paramBuf.getAvailableLength() > 0)
                || (dataBuf != null && dataBuf.getAvailableLength() > 0))
        {

            // Setup the NT transaction secondary packet to send the remaining parameter/data blocks

            setCommand(tbuf.isType());

            // Get the remaining parameter/data block lengths

            plen = paramBuf != null ? paramBuf.getAvailableLength() : 0;
            dlen = dataBuf != null ? dataBuf.getAvailableLength() : 0;

            if ((plen + dlen) > availBuf)
            {

                // Calculate the parameter/data block sizes to send in the first request packet

                if (plen > 0)
                {

                    // Check if the remaining parameter block can fit into the packet

                    if (plen <= availBuf)
                    {

                        // Pack all of the parameter block and fill the remaining buffer with the
                        // data block

                        if (dlen > 0)
                            dlen = availBuf - plen;
                    }
                    else
                    {

                        // Split the parameter/data space in the packet

                        plen = availBuf / 2;
                        dlen = plen;
                    }
                }
                else if (dlen > availBuf)
                {

                    // Fill the packet with the first section of the data block

                    dlen = availBuf;
                }
            }

            // Pack the parameter block data, if any

            resetBytePointerAlign();

            packLen = -1;
            pos = getPosition();

            if (plen > 0 && paramBuf != null)
            {

                // Set the parameter block offset, from the start of the SMB packet

                setParameterBlockOffset(pos);
                setParameterBlockCount(plen);
                setParameterBlockDisplacement(paramBuf.getDisplacement());

                // Pack the parameter block

                packLen = paramBuf.copyData(getBuffer(), pos, plen);

                // Update the buffer position for the data block

                pos = DataPacker.wordAlign(pos + packLen);
                setPosition(pos);
            }
            else
            {

                // No parameter data, clear the count/offset

                setParameterBlockCount(0);
                setParameterBlockOffset(pos);
            }

            // Pack the data block, if any

            if (dlen > 0 && dataBuf != null)
            {

                // Set the data block offset

                setDataBlockOffset(pos);
                setDataBlockCount(dlen);
                setDataBlockDisplacement(dataBuf.getDisplacement());

                // Pack the data block

                packLen = dataBuf.copyData(getBuffer(), pos, dlen);

                // Update the end of buffer position

                setPosition(pos + packLen);
            }
            else
            {

                // No data, clear the count/offset

                setDataBlockCount(0);
                setDataBlockOffset(pos);
            }

            // Set the byte count for the SMB packet to set the overall length

            setByteCount();

            // Send the transaction response packet

            sess.sendResponseSMB(this);
        }
    }
}