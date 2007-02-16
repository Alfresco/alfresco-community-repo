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
package org.alfresco.filesys.smb.mailslot;

import org.alfresco.filesys.util.DataPacker;

/**
 * SMB Mailslot Packet Class
 */
public class SMBMailslotPacket
{
    //  SMB packet offsets

    public static final int SIGNATURE   = 0;
    public static final int COMMAND         = 4;
    public static final int ERRORCODE   = 5;
    public static final int ERRORCLASS  = 5;
    public static final int ERROR           = 7;
    public static final int FLAGS           = 9;
    public static final int FLAGS2          = 10;
    public static final int PIDHIGH         = 12;
    public static final int SID                 = 18;
    public static final int SEQNO           = 20;
    public static final int TID                 = 24;
    public static final int PID                 = 26;
    public static final int UID                 = 28;
    public static final int MID                 = 30;
    public static final int WORDCNT         = 32;
    public static final int ANDXCOMMAND = 33;
    public static final int ANDXRESERVED= 34;
    public static final int PARAMWORDS  = 33;

    //  SMB packet header length for a transaction type request

    public static final int TRANS_HEADERLEN = 66;

    //  Minimum receive length for a valid SMB packet

    public static final int MIN_RXLEN = 32;

    //  Default buffer size to allocate for SMB mailslot packets

    public static final int DEFAULT_BUFSIZE = 500;

    //  Flag bits

    public static final int FLG_SUBDIALECT  = 0x01;
    public static final int FLG_CASELESS        = 0x08;
    public static final int FLG_CANONICAL   = 0x10;
    public static final int FLG_OPLOCK          = 0x20;
    public static final int FLG_NOTIFY          = 0x40;
    public static final int FLG_RESPONSE        = 0x80;

    //  Flag2 bits

    public static final int FLG2_LONGFILENAMES  = 0x0001;
    public static final int FLG2_EXTENDEDATTRIB = 0x0002;
    public static final int FLG2_READIFEXE          = 0x2000;
    public static final int FLG2_LONGERRORCODE  = 0x4000;
    public static final int FLG2_UNICODE                = 0x8000;

    //  SMB packet buffer and offset

    private byte[] m_smbbuf;
    private int m_offset;

    // Define the number of standard parameters for a server response

    private static final int STD_PARAMS = 14;

    // SMB packet types we expect to receive in a mailslot

    public static final int Transaction = 0x25;
    public static final int Transaction2 = 0x32;

    /**
     * Default constructor
     */
    public SMBMailslotPacket()
    {
        m_smbbuf = new byte[DEFAULT_BUFSIZE];
        m_offset = 0;
    }

    /**
     * Class constructor
     * 
     * @param buf byte[]
     */
    public SMBMailslotPacket(byte[] buf)
    {
        m_smbbuf = buf;
        m_offset = 0;
    }

    /**
     * Class constructor
     * 
     * @param buf byte[]
     * @param off int
     */
    public SMBMailslotPacket(byte[] buf, int off)
    {
        m_smbbuf = buf;
        m_offset = off;
    }

    /**
     * Reset the mailslot packet to use the specified buffer and offset
     * 
     * @param buf byte[]
     * @param offset int
     */
    public final void resetPacket(byte[] buf, int offset)
    {
        m_smbbuf = buf;
        m_offset = offset;
    }

    /**
     * Get the secondary command code
     * 
     * @return Secondary command code
     */
    public final int getAndXCommand()
    {
        return (int) (m_smbbuf[ANDXCOMMAND + m_offset] & 0xFF);
    }

    /**
     * Return the byte array used for the SMB packet
     * 
     * @return Byte array used for the SMB packet.
     */
    public final byte[] getBuffer()
    {
        return m_smbbuf;
    }

    /**
     * Return the total buffer size available to the SMB request
     * 
     * @return Total SMB buffer length available.
     */
    public final int getBufferLength()
    {
        return m_smbbuf.length - m_offset;
    }

    /**
     * Get the data byte count for the SMB packet
     * 
     * @return Data byte count
     */
    public final int getByteCount()
    {

        // Calculate the offset of the byte count

        int pos = PARAMWORDS + (2 * getParameterCount());
        return (int) DataPacker.getIntelShort(m_smbbuf, pos);
    }

    /**
     * Get the data byte area offset within the SMB packet
     * 
     * @return Data byte offset within the SMB packet.
     */
    public final int getByteOffset()
    {

        // Calculate the offset of the byte buffer

        int pCnt = getParameterCount();
        int pos = WORDCNT + (2 * pCnt) + 3 + m_offset;
        return pos;
    }

    /**
     * Get the SMB command
     * 
     * @return SMB command code.
     */
    public final int getCommand()
    {
        return (int) (m_smbbuf[COMMAND + m_offset] & 0xFF);
    }

    /**
     * Determine if normal or long error codes have been returned
     * 
     * @return boolean
     */
    public final boolean hasLongErrorCode()
    {
        if ((getFlags2() & FLG2_LONGERRORCODE) == 0)
            return false;
        return true;
    }

    /**
     * Get the SMB error class
     * 
     * @return SMB error class.
     */
    public final int getErrorClass()
    {
        return (int) m_smbbuf[ERRORCLASS + m_offset] & 0xFF;
    }

    /**
     * Get the SMB error code
     * 
     * @return SMB error code.
     */
    public final int getErrorCode()
    {
        return (int) m_smbbuf[ERROR + m_offset] & 0xFF;
    }

    /**
     * Get the SMB flags value.
     * 
     * @return SMB flags value.
     */
    public final int getFlags()
    {
        return (int) m_smbbuf[FLAGS + m_offset] & 0xFF;
    }

    /**
     * Get the SMB flags2 value.
     * 
     * @return SMB flags2 value.
     */
    public final int getFlags2()
    {
        return (int) DataPacker.getIntelShort(m_smbbuf, FLAGS2 + m_offset);
    }

    /**
     * Calculate the total used packet length.
     * 
     * @return Total used packet length.
     */
    public final int getLength()
    {
        return (getByteOffset() + getByteCount()) - m_offset;
    }

    /**
     * Get the long SMB error code
     * 
     * @return Long SMB error code.
     */
    public final int getLongErrorCode()
    {
        return DataPacker.getIntelInt(m_smbbuf, ERRORCODE + m_offset);
    }

    /**
     * Get the multiplex identifier.
     * 
     * @return Multiplex identifier.
     */
    public final int getMultiplexId()
    {
        return DataPacker.getIntelShort(m_smbbuf, MID + m_offset);
    }

    /**
     * Get a parameter word from the SMB packet.
     * 
     * @param idx Parameter index (zero based).
     * @return Parameter word value.
     * @exception java.lang.IndexOutOfBoundsException If the parameter index is out of range.
     */
    public final int getParameter(int idx) throws java.lang.IndexOutOfBoundsException
    {

        // Range check the parameter index

        if (idx > getParameterCount())
            throw new java.lang.IndexOutOfBoundsException();

        // Calculate the parameter word offset

        int pos = WORDCNT + (2 * idx) + 1 + m_offset;
        return (int) (DataPacker.getIntelShort(m_smbbuf, pos) & 0xFFFF);
    }

    /**
     * Get the parameter count
     * 
     * @return Parameter word count.
     */
    public final int getParameterCount()
    {
        return (int) m_smbbuf[WORDCNT + m_offset];
    }

    /**
     * Get the process indentifier (PID)
     * 
     * @return Process identifier value.
     */
    public final int getProcessId()
    {
        return DataPacker.getIntelShort(m_smbbuf, PID + m_offset);
    }

    /**
     * Get the tree identifier (TID)
     * 
     * @return Tree identifier (TID)
     */
    public final int getTreeId()
    {
        return DataPacker.getIntelShort(m_smbbuf, TID + m_offset);
    }

    /**
     * Get the user identifier (UID)
     * 
     * @return User identifier (UID)
     */
    public final int getUserId()
    {
        return DataPacker.getIntelShort(m_smbbuf, UID + m_offset);
    }

    /**
     * Return the offset to the data block within the SMB packet. The data block is word aligned
     * within the byte buffer area of the SMB packet. This method must be called after the parameter
     * count and parameter block length have been set.
     * 
     * @return int Offset to the data block area.
     */
    public final int getDataBlockOffset()
    {

        // Get the position of the parameter block

        int pos = (getParameterBlockOffset() + getParameter(3)) + m_offset;
        if ((pos & 0x01) != 0)
            pos++;
        return pos;
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
     * Return the parameter block offset where the parameter bytes should be placed. This method
     * must be called after the paramter count has been set. The parameter offset is word aligned.
     * 
     * @return int Offset to the parameter block area.
     */
    public final int getParameterBlockOffset()
    {

        // Get the offset to the byte buffer area of the SMB packet

        int pos = getByteOffset() + m_offset;
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
        return getParameter(12) + m_offset;
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

        // Get the offset to the parameter words

        return getParameter(10) + m_offset;
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
     * @return int
     */
    public final String getRxParameterString(int pos)
    {

        // Get the parameter block offset

        pos += getRxParameterBlock();

        // Get the transact parameter string

        byte[] buf = getBuffer();
        int len = (buf[pos++] & 0x00FF);
        return DataPacker.getString(buf, pos, len);
    }

    /**
     * Get the required transact parameter string.
     * 
     * @param pos Offset to the string within the parameter block.
     * @param len Length of the string.
     * @return int
     */
    public final String getRxParameterString(int pos, int len)
    {

        // Get the parameter block offset

        pos += getRxParameterBlock();

        // Get the transact parameter string

        byte[] buf = getBuffer();
        return DataPacker.getString(buf, pos, len);
    }

    /**
     * Return the received transaction name.
     * 
     * @return java.lang.String
     */
    public final String getRxTransactName()
    {

        // Check if the transaction has a name

        if (getCommand() == Transaction2)
            return "";

        // Unpack the transaction name string

        int pos = getByteOffset();
        return DataPacker.getString(getBuffer(), pos, getByteCount());
    }

    /**
     * Return the specified transaction setup parameter.
     * 
     * @param idx Setup parameter index.
     */
    public final int getSetupParameter(int idx) throws java.lang.ArrayIndexOutOfBoundsException
    {

        // Check if the setup parameter index is valid

        if (idx >= getRxParameterCount())
            throw new java.lang.ArrayIndexOutOfBoundsException();

        // Get the setup parameter

        return getParameter(idx + STD_PARAMS);
    }

    /**
     * Return the mailslot opcode
     * 
     * @return int
     */
    public final int getMailslotOpcode()
    {
        try
        {
            return getSetupParameter(0);
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
        }
        return -1;
    }

    /**
     * Return the mailslot priority
     * 
     * @return int
     */
    public final int getMailslotPriority()
    {
        try
        {
            return getSetupParameter(1);
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
        }
        return -1;
    }

    /**
     * Return the mailslot class of service
     * 
     * @return int
     */
    public final int getMailslotClass()
    {
        try
        {
            return getSetupParameter(2);
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
        }
        return -1;
    }

    /**
     * Return the mailslot sub-opcode, the first byte from the mailslot data
     * 
     * @return int
     */
    public final int getMailslotSubOpcode()
    {
        return (int) (m_smbbuf[getMailslotDataOffset()] & 0xFF);
    }

    /**
     * Return the mailslot data offset
     * 
     * @return int
     */
    public final int getMailslotDataOffset()
    {
        return getRxDataBlock();
    }

    /**
     * Initialize a mailslot SMB
     * 
     * @param name Mailslot name
     * @param data Request data bytes
     * @param dlen Data length
     */
    public final void initializeMailslotSMB(String name, byte[] data, int dlen)
    {

        // Initialize the SMB packet header

        initializeBuffer();

        // Clear header values

        setFlags(0);
        setFlags2(0);
        setUserId(0);
        setMultiplexId(0);
        setTreeId(0);
        setProcessId(0);

        // Initialize the transaction

        initializeTransact(name, 17, null, 0, data, dlen);

        // Initialize the transactin setup parameters for a mailslot write

        setSetupParameter(0, MailSlot.WRITE);
        setSetupParameter(1, 1);
        setSetupParameter(2, MailSlot.UNRELIABLE);
    }

    /**
     * Initialize the transact SMB packet
     * 
     * @param name Transaction name
     * @param pcnt Total parameter count for this transaction
     * @param paramblk Parameter block data bytes
     * @param plen Parameter block data length
     * @param datablk Data block data bytes
     * @param dlen Data block data length
     */
    protected final void initializeTransact(String name, int pcnt, byte[] paramblk, int plen, byte[] datablk, int dlen)
    {

        // Set the SMB command code

        if (name == null)
            setCommand(Transaction2);
        else
            setCommand(Transaction);

        // Set the parameter count

        setParameterCount(pcnt);

        // Initialize the parameters

        setParameter(0, plen); // total parameter bytes being sent
        setParameter(1, dlen); // total data bytes being sent

        for (int i = 2; i < 9; setParameter(i++, 0))
            ;

        setParameter(6, 1000); // timeout 1 second
        setParameter(9, plen); // parameter bytes sent in this packet
        setParameter(11, dlen); // data bytes sent in this packet

        setParameter(13, pcnt - STD_PARAMS); // number of setup words

        // Get the data byte offset

        int pos = getByteOffset();
        int startPos = pos;

        // Check if this is a named transaction, if so then store the name

        int idx;
        byte[] buf = getBuffer();

        if (name != null)
        {

            // Store the transaction name

            byte[] nam = name.getBytes();

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

            setParameter(10, pos - m_offset);

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

            setParameter(12, pos - m_offset);

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
     * Set the secondary SMB command
     * 
     * @param cmd Secondary SMB command code.
     */
    public final void setAndXCommand(int cmd)
    {
        m_smbbuf[ANDXCOMMAND + m_offset] = (byte) cmd;
        m_smbbuf[ANDXRESERVED + m_offset] = (byte) 0;
    }

    /**
     * Set the data byte count for this SMB packet
     * 
     * @param cnt Data byte count.
     */
    public final void setByteCount(int cnt)
    {
        int offset = getByteOffset() - 2;
        DataPacker.putIntelShort(cnt, m_smbbuf, offset);
    }

    /**
     * Set the data byte area in the SMB packet
     * 
     * @param byts Byte array containing the data to be copied to the SMB packet.
     */
    public final void setBytes(byte[] byts)
    {
        int offset = getByteOffset() - 2;
        DataPacker.putIntelShort(byts.length, m_smbbuf, offset);

        offset += 2;

        for (int idx = 0; idx < byts.length; m_smbbuf[offset + idx] = byts[idx++])
            ;
    }

    /**
     * Set the SMB command
     * 
     * @param cmd SMB command code
     */
    public final void setCommand(int cmd)
    {
        m_smbbuf[COMMAND + m_offset] = (byte) cmd;
    }

    /**
     * Set the SMB error class.
     * 
     * @param cl SMB error class.
     */
    public final void setErrorClass(int cl)
    {
        m_smbbuf[ERRORCLASS + m_offset] = (byte) (cl & 0xFF);
    }

    /**
     * Set the SMB error code
     * 
     * @param sts SMB error code.
     */
    public final void setErrorCode(int sts)
    {
        m_smbbuf[ERROR + m_offset] = (byte) (sts & 0xFF);
    }

    /**
     * Set the SMB flags value.
     * 
     * @param flg SMB flags value.
     */
    public final void setFlags(int flg)
    {
        m_smbbuf[FLAGS + m_offset] = (byte) flg;
    }

    /**
     * Set the SMB flags2 value.
     * 
     * @param flg SMB flags2 value.
     */
    public final void setFlags2(int flg)
    {
        DataPacker.putIntelShort(flg, m_smbbuf, FLAGS2 + m_offset);
    }

    /**
     * Set the multiplex identifier.
     * 
     * @param mid Multiplex identifier
     */
    public final void setMultiplexId(int mid)
    {
        DataPacker.putIntelShort(mid, m_smbbuf, MID + m_offset);
    }

    /**
     * Set the specified parameter word.
     * 
     * @param idx Parameter index (zero based).
     * @param val Parameter value.
     */
    public final void setParameter(int idx, int val)
    {
        int pos = WORDCNT + (2 * idx) + 1 + m_offset;
        DataPacker.putIntelShort(val, m_smbbuf, pos);
    }

    /**
     * Set the parameter count
     * 
     * @param cnt Parameter word count.
     */
    public final void setParameterCount(int cnt)
    {
        m_smbbuf[WORDCNT + m_offset] = (byte) cnt;
    }

    /**
     * Set the process identifier value (PID).
     * 
     * @param pid Process identifier value.
     */
    public final void setProcessId(int pid)
    {
        DataPacker.putIntelShort(pid, m_smbbuf, PID + m_offset);
    }

    /**
     * Set the packet sequence number, for connectionless commands.
     * 
     * @param seq Sequence number.
     */
    public final void setSeqNo(int seq)
    {
        DataPacker.putIntelShort(seq, m_smbbuf, SEQNO + m_offset);
    }

    /**
     * Set the session id.
     * 
     * @param sid Session id.
     */
    public final void setSID(int sid)
    {
        DataPacker.putIntelShort(sid, m_smbbuf, SID + m_offset);
    }

    /**
     * Set the tree identifier (TID)
     * 
     * @param tid Tree identifier value.
     */
    public final void setTreeId(int tid)
    {
        DataPacker.putIntelShort(tid, m_smbbuf, TID + m_offset);
    }

    /**
     * Set the user identifier (UID)
     * 
     * @param uid User identifier value.
     */
    public final void setUserId(int uid)
    {
        DataPacker.putIntelShort(uid, m_smbbuf, UID + m_offset);
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
     * Initialize the SMB packet buffer.
     */
    private final void initializeBuffer()
    {

        // Set the packet signature

        m_smbbuf[SIGNATURE + m_offset] = (byte) 0xFF;
        m_smbbuf[SIGNATURE + 1 + m_offset] = (byte) 'S';
        m_smbbuf[SIGNATURE + 2 + m_offset] = (byte) 'M';
        m_smbbuf[SIGNATURE + 3 + m_offset] = (byte) 'B';
    }
}