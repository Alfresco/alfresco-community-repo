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

import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.smb.PacketType;
import org.alfresco.filesys.util.DataPacker;

/**
 * NT Transaction Packet Class
 */
public class NTTransPacket extends SMBSrvPacket
{

    // Define the number of standard parameter words/bytes

    private static final int StandardParams = 19;
    private static final int ParameterBytes = 36; // 8 x 32bit params + max setup count byte +
                                                    // setup count byte + reserved word

    // Standard reply word count

    private static final int ReplyParams = 18;

    // Offset to start of NT parameters from start of packet

    private static final int NTMaxSetupCount =      SMBPacket.PARAMWORDS;
    private static final int NTParams =             SMBPacket.PARAMWORDS + 3;
    private static final int NTSetupCount =         NTParams + 32;
    private static final int NTFunction =           NTSetupCount + 1;

    // Default return parameter/data byte counts

    private static final int DefaultReturnParams = 4;
    private static final int DefaultReturnData = 1024;

    /**
     * Default constructor
     */
    public NTTransPacket()
    {
        super();
    }

    /**
     * Class constructor
     * 
     * @param buf byte[]
     */
    public NTTransPacket(byte[] buf)
    {
        super(buf);
    }

    /**
     * Copy constructor
     * 
     * @param pkt NTTransPacket
     */
    public NTTransPacket(NTTransPacket pkt)
    {
        super(pkt);
    }

    /**
     * Return the data block size
     * 
     * @return Data block size in bytes
     */
    public final int getDataLength()
    {
        return getNTParameter(6);
    }

    /**
     * Return the data block offset
     * 
     * @return Data block offset within the SMB packet.
     */
    public final int getDataOffset()
    {
        return getNTParameter(7) + RFCNetBIOSProtocol.HEADER_LEN;
    }

    /**
     * Unpack the parameter block
     * 
     * @return int[]
     */
    public final int[] getParameterBlock()
    {

        // Get the parameter count and allocate the parameter buffer

        int prmcnt = getParameterBlockCount() / 4; // convert to number of ints
        if (prmcnt <= 0)
            return null;
        int[] prmblk = new int[prmcnt];

        // Get the offset to the parameter words, add the NetBIOS header length
        // to the offset.

        int pos = getParameterBlockOffset();

        // Unpack the parameter ints

        setBytePointer(pos, getByteCount());

        for (int idx = 0; idx < prmcnt; idx++)
        {

            // Unpack the current parameter value

            prmblk[idx] = unpackInt();
        }

        // Return the parameter block

        return prmblk;
    }

    /**
     * Return the total parameter count
     * 
     * @return int
     */
    public final int getTotalParameterCount()
    {
        return getNTParameter(0);
    }

    /**
     * Return the total data count
     * 
     * @return int
     */
    public final int getTotalDataCount()
    {
        return getNTParameter(1);
    }

    /**
     * Return the maximum parameter block length to be returned
     * 
     * @return int
     */
    public final int getMaximumParameterReturn()
    {
        return getNTParameter(2);
    }

    /**
     * Return the maximum data block length to be returned
     * 
     * @return int
     */
    public final int getMaximumDataReturn()
    {
        return getNTParameter(3);
    }

    /**
     * Return the parameter block count
     * 
     * @return int
     */
    public final int getParameterBlockCount()
    {
        return getNTParameter(getCommand() == PacketType.NTTransact ? 4 : 2);
    }

    /**
     * Return the parameter block offset
     * 
     * @return int
     */
    public final int getParameterBlockOffset()
    {
        return getNTParameter(getCommand() == PacketType.NTTransact ? 5 : 3) + RFCNetBIOSProtocol.HEADER_LEN;
    }

    /**
     * Return the paramater block displacement
     * 
     * @return int
     */
    public final int getParameterBlockDisplacement()
    {
        return getNTParameter(4);
    }

    /**
     * Return the data block count
     * 
     * @return int
     */
    public final int getDataBlockCount()
    {
        return getNTParameter(getCommand() == PacketType.NTTransact ? 6 : 5);
    }

    /**
     * Return the data block offset
     * 
     * @return int
     */
    public final int getDataBlockOffset()
    {
        return getNTParameter(getCommand() == PacketType.NTTransact ? 7 : 6) + RFCNetBIOSProtocol.HEADER_LEN;
    }

    /**
     * Return the data block displacment
     * 
     * @return int
     */
    public final int getDataBlockDisplacement()
    {
        return getNTParameter(7);
    }

    /**
     * Get an NT parameter (32bit)
     * 
     * @param idx int
     * @return int
     */
    protected final int getNTParameter(int idx)
    {
        int pos = NTParams + (4 * idx);
        return DataPacker.getIntelInt(getBuffer(), pos);
    }

    /**
     * Get the setup parameter count
     * 
     * @return int
     */
    public final int getSetupCount()
    {
        byte[] buf = getBuffer();
        return (int) buf[NTSetupCount] & 0xFF;
    }

    /**
     * Return the offset to the setup words data
     * 
     * @return int
     */
    public final int getSetupOffset()
    {
        return NTFunction + 2;
    }

    /**
     * Get the NT transaction function code
     * 
     * @return int
     */
    public final int getNTFunction()
    {
        byte[] buf = getBuffer();
        return DataPacker.getIntelShort(buf, NTFunction);
    }

    /**
     * Initialize the transact SMB packet
     * 
     * @param func NT transaction function code
     * @param paramblk Parameter block data bytes
     * @param plen Parameter block data length
     * @param datablk Data block data bytes
     * @param dlen Data block data length
     * @param setupcnt Number of setup parameters
     */
    public final void initTransact(int func, byte[] paramblk, int plen, byte[] datablk, int dlen, int setupcnt)
    {
        initTransact(func, paramblk, plen, datablk, dlen, setupcnt, DefaultReturnParams, DefaultReturnData);
    }

    /**
     * Initialize the transact SMB packet
     * 
     * @param func NT transaction function code
     * @param paramblk Parameter block data bytes
     * @param plen Parameter block data length
     * @param datablk Data block data bytes
     * @param dlen Data block data length
     * @param setupcnt Number of setup parameters
     * @param maxPrm Maximum parameter bytes to return
     * @param maxData Maximum data bytes to return
     */
    public final void initTransact(int func, byte[] paramblk, int plen, byte[] datablk, int dlen, int setupcnt,
            int maxPrm, int maxData)
    {

        // Set the SMB command and parameter count

        setCommand(PacketType.NTTransact);
        setParameterCount(StandardParams + setupcnt);

        // Initialize the parameters

        setTotalParameterCount(plen);
        setTotalDataCount(dlen);
        setMaximumParameterReturn(maxPrm);
        setMaximumDataReturn(maxData);
        setParameterCount(plen);
        setParameterBlockOffset(0);
        setDataBlockCount(dlen);
        setDataBlockOffset(0);

        setSetupCount(setupcnt);
        setNTFunction(func);

        resetBytePointerAlign();

        // Pack the parameter block

        if (paramblk != null)
        {

            // Set the parameter block offset, from the start of the SMB packet

            setParameterBlockOffset(getPosition());

            // Pack the parameter block

            packBytes(paramblk, plen);
        }

        // Pack the data block

        if (datablk != null)
        {

            // Align the byte area offset and set the data block offset in the request

            alignBytePointer();
            setDataBlockOffset(getPosition());

            // Pack the data block

            packBytes(datablk, dlen);
        }

        // Set the byte count for the SMB packet

        setByteCount();
    }

    /**
     * Initialize the NT transaction reply
     * 
     * @param paramblk Parameter block data bytes
     * @param plen Parameter block data length
     * @param datablk Data block data bytes
     * @param dlen Data block data length
     */
    public final void initTransactReply(byte[] paramblk, int plen, byte[] datablk, int dlen)
    {

        // Set the parameter count

        setParameterCount(ReplyParams);
        setSetupCount(0);

        // Initialize the parameters

        setTotalParameterCount(plen);
        setTotalDataCount(dlen);

        setReplyParameterCount(plen);
        setReplyParameterOffset(0);
        setReplyParameterDisplacement(0);

        setReplyDataCount(dlen);
        setDataBlockOffset(0);
        setReplyDataDisplacement(0);

        setSetupCount(0);

        resetBytePointerAlign();

        // Pack the parameter block

        if (paramblk != null)
        {

            // Set the parameter block offset, from the start of the SMB packet

            setReplyParameterOffset(getPosition() - 4);

            // Pack the parameter block

            packBytes(paramblk, plen);
        }

        // Pack the data block

        if (datablk != null)
        {

            // Align the byte area offset and set the data block offset in the request

            alignBytePointer();
            setReplyDataOffset(getPosition() - 4);

            // Pack the data block

            packBytes(datablk, dlen);
        }

        // Set the byte count for the SMB packet

        setByteCount();
    }

    /**
     * Initialize the NT transaction reply
     * 
     * @param paramblk Parameter block data bytes
     * @param plen Parameter block data length
     * @param datablk Data block data bytes
     * @param dlen Data block data length
     * @param setupCnt Number of setup parameter
     */
    public final void initTransactReply(byte[] paramblk, int plen, byte[] datablk, int dlen, int setupCnt)
    {

        // Set the parameter count, add the setup parameter count

        setParameterCount(ReplyParams + setupCnt);
        setSetupCount(setupCnt);

        // Initialize the parameters

        setTotalParameterCount(plen);
        setTotalDataCount(dlen);

        setReplyParameterCount(plen);
        setReplyParameterOffset(0);
        setReplyParameterDisplacement(0);

        setReplyDataCount(dlen);
        setDataBlockOffset(0);
        setReplyDataDisplacement(0);

        setSetupCount(setupCnt);

        resetBytePointerAlign();

        // Pack the parameter block

        if (paramblk != null)
        {

            // Set the parameter block offset, from the start of the SMB packet

            setReplyParameterOffset(getPosition() - 4);

            // Pack the parameter block

            packBytes(paramblk, plen);
        }

        // Pack the data block

        if (datablk != null)
        {

            // Align the byte area offset and set the data block offset in the request

            alignBytePointer();
            setReplyDataOffset(getPosition() - 4);

            // Pack the data block

            packBytes(datablk, dlen);
        }

        // Set the byte count for the SMB packet

        setByteCount();
    }

    /**
     * Set the total parameter count
     * 
     * @param cnt int
     */
    public final void setTotalParameterCount(int cnt)
    {
        setNTParameter(0, cnt);
    }

    /**
     * Set the total data count
     * 
     * @param cnt int
     */
    public final void setTotalDataCount(int cnt)
    {
        setNTParameter(1, cnt);
    }

    /**
     * Set the maximum return parameter count
     * 
     * @param cnt int
     */
    public final void setMaximumParameterReturn(int cnt)
    {
        setNTParameter(2, cnt);
    }

    /**
     * Set the maximum return data count
     * 
     * @param cnt int
     */
    public final void setMaximumDataReturn(int cnt)
    {
        setNTParameter(3, cnt);
    }

    /**
     * Set the paramater block count
     * 
     * @param disp int
     */
    public final void setTransactParameterCount(int cnt)
    {
        setNTParameter(4, cnt);
    }

    /**
     * Set the reply parameter byte count
     * 
     * @param cnt int
     */
    public final void setReplyParameterCount(int cnt)
    {
        setNTParameter(2, cnt);
    }

    /**
     * Set the reply parameter offset
     * 
     * @param off int
     */
    public final void setReplyParameterOffset(int off)
    {
        setNTParameter(3, off);
    }

    /**
     * Set the reply parameter bytes displacement
     * 
     * @param disp int
     */
    public final void setReplyParameterDisplacement(int disp)
    {
        setNTParameter(4, disp);
    }

    /**
     * Set the reply data byte count
     * 
     * @param cnt int
     */
    public final void setReplyDataCount(int cnt)
    {
        setNTParameter(5, cnt);
    }

    /**
     * Set the reply data offset
     * 
     * @param off int
     */
    public final void setReplyDataOffset(int off)
    {
        setNTParameter(6, off);
    }

    /**
     * Set the reply data bytes displacement
     * 
     * @param disp int
     */
    public final void setReplyDataDisplacement(int disp)
    {
        setNTParameter(7, disp);
    }

    /**
     * Set the parameter block offset within the packet
     * 
     * @param off int
     */
    public final void setParameterBlockOffset(int off)
    {
        setNTParameter(5, off != 0 ? off - RFCNetBIOSProtocol.HEADER_LEN : 0);
    }

    /**
     * Set the data block count
     * 
     * @param cnt int
     */
    public final void setDataBlockCount(int cnt)
    {
        setNTParameter(6, cnt);
    }

    /**
     * Set the data block offset
     * 
     * @param disp int
     */
    public final void setDataBlockOffset(int off)
    {
        setNTParameter(7, off != 0 ? off - RFCNetBIOSProtocol.HEADER_LEN : 0);
    }

    /**
     * Set an NT parameter (32bit)
     * 
     * @param idx int
     * @param val int
     */
    public final void setNTParameter(int idx, int val)
    {
        int pos = NTParams + (4 * idx);
        DataPacker.putIntelInt(val, getBuffer(), pos);
    }

    /**
     * Set the maximum setup parameter count
     * 
     * @param cnt Maximum count of setup paramater words
     */
    public final void setMaximumSetupCount(int cnt)
    {
        byte[] buf = getBuffer();
        buf[NTMaxSetupCount] = (byte) cnt;
    }

    /**
     * Set the setup parameter count
     * 
     * @param cnt Count of setup paramater words
     */
    public final void setSetupCount(int cnt)
    {
        byte[] buf = getBuffer();
        buf[NTSetupCount] = (byte) cnt;
    }

    /**
     * Set the specified setup parameter
     * 
     * @param setupIdx Setup parameter index
     * @param setupVal Setup parameter value
     */
    public final void setSetupParameter(int setupIdx, int setupVal)
    {
        int pos = NTSetupCount + 1 + (setupIdx * 2);
        DataPacker.putIntelShort(setupVal, getBuffer(), pos);
    }

    /**
     * Set the NT transaction function code
     * 
     * @param func int
     */
    public final void setNTFunction(int func)
    {
        byte[] buf = getBuffer();
        DataPacker.putIntelShort(func, buf, NTFunction);
    }

    /**
     * Reset the byte/parameter pointer area for packing/unpacking setup paramaters items to the
     * packet
     */
    public final void resetSetupPointer()
    {
        m_pos = NTFunction + 2;
        m_endpos = m_pos;
    }

    /**
     * Reset the byte/parameter pointer area for packing/unpacking the transaction data block
     */
    public final void resetDataBlockPointer()
    {
        m_pos = getDataBlockOffset();
        m_endpos = m_pos;
    }

    /**
     * Reset the byte/parameter pointer area for packing/unpacking the transaction paramater block
     */
    public final void resetParameterBlockPointer()
    {
        m_pos = getParameterBlockOffset();
        m_endpos = m_pos;
    }
}
