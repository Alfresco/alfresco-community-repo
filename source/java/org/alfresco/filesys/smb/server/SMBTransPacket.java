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

import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.smb.PacketType;
import org.alfresco.filesys.util.DataPacker;

/**
 * SMB transact packet class
 */

public class SMBTransPacket extends SMBSrvPacket
{

    // Define the number of standard parameters

    protected static final int STD_PARAMS = 14;

    // Transaction status that indicates that this transaction has more data
    // to be returned.

    public static final int IsContinued = 234;

    // Transact name, not used for transact 2

    protected String m_transName;

    // Parameter count for this transaction

    protected int m_paramCnt;

    // Multiplex identifier, to identify each transaction request

    private static int m_nextMID = 1;

    /**
     * Construct an SMB transaction packet
     * 
     * @param buf Buffer that contains the SMB transaction packet.
     */
    public SMBTransPacket(byte[] buf)
    {
        super(buf);
    }

    /**
     * Construct an SMB transaction packet
     * 
     * @param siz Size of packet to allocate.
     */
    public SMBTransPacket(int siz)
    {
        super(siz);

        // Set the multiplex id for this transaction

        setMultiplexId(getNextMultiplexId());
    }

    /**
     * Get the next multiplex id to uniquely identify this transaction
     * 
     * @return Unique multiplex id for this transaction
     */
    public final static int getNextMultiplexId()
    {
        return m_nextMID++;
    }

    /**
     * Return the total parameter byte count
     * 
     * @return int
     */
    public final int getTotalParameterCount()
    {
        return getParameter(0);
    }

    /**
     * Return the total data byte count
     * 
     * @return int
     */
    public final int getTotalDataCount()
    {
        return getParameter(1);
    }

    /**
     * Return the parameter count size in bytes for this section
     * 
     * @return int
     */
    public final int getParameterBlockCount()
    {
        return getParameter(9);
    }

    /**
     * Return the parameter block offset
     * 
     * @return Paramter block offset within the SMB packet
     */
    public final int getParameterBlockOffset()
    {
        return getParameter(10) + RFCNetBIOSProtocol.HEADER_LEN;
    }

    /**
     * Return the data block size in bytes for this section
     * 
     * @return int
     */
    public final int getDataBlockCount()
    {
        return getParameter(11);
    }

    /**
     * Return the data block offset
     * 
     * @return Data block offset within the SMB packet.
     */
    public final int getDataBlockOffset()
    {
        return getParameter(12) + RFCNetBIOSProtocol.HEADER_LEN;
    }

    /**
     * Return the secondary parameter block size in bytes
     * 
     * @return int
     */
    public final int getSecondaryParameterBlockCount()
    {
        return getParameter(2);
    }

    /**
     * Return the secondary parameter block offset
     * 
     * @return int
     */
    public final int getSecondaryParameterBlockOffset()
    {
        return getParameter(3) + RFCNetBIOSProtocol.HEADER_LEN;
    }

    /**
     * Return the secondary parameter block displacement
     * 
     * @return int
     */
    public final int getParameterBlockDisplacement()
    {
        return getParameter(4);
    }

    /**
     * Return the secondary data block size in bytes
     * 
     * @return int
     */
    public final int getSecondaryDataBlockCount()
    {
        return getParameter(5);
    }

    /**
     * Return the secondary data block offset
     * 
     * @return int
     */
    public final int getSecondaryDataBlockOffset()
    {
        return getParameter(6) + RFCNetBIOSProtocol.HEADER_LEN;
    }

    /**
     * Return the secondary data block displacement
     * 
     * @return int
     */
    public final int getDataBlockDisplacement()
    {
        return getParameter(7);
    }

    /**
     * Return the transaction sub-command
     * 
     * @return int
     */
    public final int getSubFunction()
    {
        return getParameter(14);
    }

    /**
     * Unpack the parameter block into the supplied array.
     * 
     * @param prmblk Array to unpack the parameter block words into.
     */
    public final void getParameterBlock(short[] prmblk) throws java.lang.ArrayIndexOutOfBoundsException
    {

        // Determine how many parameters are to be unpacked, check if the user
        // buffer is long enough

        int prmcnt = getParameter(3) / 2; // convert to number of words
        if (prmblk.length < prmcnt)
            throw new java.lang.ArrayIndexOutOfBoundsException();

        // Get the offset to the parameter words, add the NetBIOS header length
        // to the offset.

        int pos = getParameter(4) + RFCNetBIOSProtocol.HEADER_LEN;

        // Unpack the parameter words

        byte[] buf = getBuffer();

        for (int idx = 0; idx < prmcnt; idx++)
        {

            // Unpack the current parameter word

            prmblk[idx] = (short) DataPacker.getIntelShort(buf, pos);
            pos += 2;
        }
    }

    /**
     * Initialize the transact SMB packet
     * 
     * @param pcnt Total parameter count for this transaction
     * @param paramblk Parameter block data bytes
     * @param plen Parameter block data length
     * @param datablk Data block data bytes
     * @param dlen Data block data length
     */
    public final void InitializeTransact(int pcnt, byte[] paramblk, int plen, byte[] datablk, int dlen)
    {

        // Set the SMB command code

        if (m_transName == null)
            setCommand(PacketType.Transaction2);
        else
            setCommand(PacketType.Transaction);

        // Set the parameter count

        setParameterCount(pcnt);

        // Save the parameter count, add an extra parameter for the data byte count

        m_paramCnt = pcnt;

        // Initialize the parameters

        setParameter(0, plen); // total parameter bytes being sent
        setParameter(1, dlen); // total data bytes being sent

        for (int i = 2; i < 9; setParameter(i++, 0))
            ;

        setParameter(9, plen); // parameter bytes sent in this packet
        setParameter(11, dlen); // data bytes sent in this packet

        setParameter(13, pcnt - STD_PARAMS); // number of setup words

        // Get the data byte offset

        int pos = getByteOffset();
        int startPos = pos;

        // Check if this is a named transaction, if so then store the name

        int idx;
        byte[] buf = getBuffer();

        if (m_transName != null)
        {

            // Store the transaction name

            byte[] nam = m_transName.getBytes();

            for (idx = 0; idx < nam.length; idx++)
                buf[pos++] = nam[idx];
        }

        // Word align the buffer offset

        if ((pos % 2) > 0)
            pos++;

        // Store the parameter block

        if (paramblk != null)
        {

            // Set the parameter block offset

            setParameter(10, pos - RFCNetBIOSProtocol.HEADER_LEN);

            // Store the parameter block

            for (idx = 0; idx < plen; idx++)
                buf[pos++] = paramblk[idx];
        }
        else
        {

            // Clear the parameter block offset

            setParameter(10, 0);
        }

        // Word align the data block

        if ((pos % 2) > 0)
            pos++;

        // Store the data block

        if (datablk != null)
        {

            // Set the data block offset

            setParameter(12, pos - RFCNetBIOSProtocol.HEADER_LEN);

            // Store the data block

            for (idx = 0; idx < dlen; idx++)
                buf[pos++] = datablk[idx];
        }
        else
        {

            // Zero the data block offset

            setParameter(12, 0);
        }

        // Set the byte count for the SMB packet

        setByteCount(pos - startPos);
    }

    /**
     * Set the specifiec setup parameter within the SMB packet.
     * 
     * @param idx Setup parameter index.
     * @param val Setup parameter value.
     */

    public final void setSetupParameter(int idx, int val)
    {
        setParameter(STD_PARAMS + idx, val);
    }

    /**
     * Set the transaction name for normal transactions
     * 
     * @param tname Transaction name string
     */

    public final void setTransactionName(String tname)
    {
        m_transName = tname;
    }
}