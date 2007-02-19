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

import org.alfresco.filesys.netbios.NetBIOSSession;
import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.smb.PacketType;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.util.DataPacker;

/**
 * SMB packet type class
 */
public class SMBPacket
{

    // SMB packet offsets, assuming an RFC NetBIOS transport

    public static final int SIGNATURE       = RFCNetBIOSProtocol.HEADER_LEN;
    public static final int COMMAND         = 4 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int ERRORCODE       = 5 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int ERRORCLASS      = 5 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int ERROR           = 7 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int FLAGS           = 9 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int FLAGS2          = 10 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int PIDHIGH         = 12 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int SID             = 18 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int SEQNO           = 20 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int TID             = 24 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int PID             = 26 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int UID             = 28 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int MID             = 30 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int WORDCNT         = 32 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int ANDXCOMMAND     = 33 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int ANDXRESERVED    = 34 + RFCNetBIOSProtocol.HEADER_LEN;
    public static final int PARAMWORDS      = 33 + RFCNetBIOSProtocol.HEADER_LEN;

    // SMB packet header length for a transaction type request

    public static final int TRANS_HEADERLEN = 66 + RFCNetBIOSProtocol.HEADER_LEN;

    // Minimum receive length for a valid SMB packet

    public static final int MIN_RXLEN = 32;

    // Default buffer size to allocate for SMB packets

    public static final int DEFAULT_BUFSIZE = 4096;

    // Flag bits

    public static final int FLG_SUBDIALECT = 0x01;
    public static final int FLG_CASELESS = 0x08;
    public static final int FLG_CANONICAL = 0x10;
    public static final int FLG_OPLOCK = 0x20;
    public static final int FLG_NOTIFY = 0x40;
    public static final int FLG_RESPONSE = 0x80;

    // Flag2 bits

    public static final int FLG2_LONGFILENAMES = 0x0001;
    public static final int FLG2_EXTENDEDATTRIB = 0x0002;
    public static final int FLG2_SECURITYSIGS = 0x0004;
    public static final int FLG2_LONGNAMESUSED = 0x0040;
    public static final int FLG2_EXTENDNEGOTIATE = 0x0800;
    public static final int FLG2_DFSRESOLVE = 0x1000;
    public static final int FLG2_READIFEXE = 0x2000;
    public static final int FLG2_LONGERRORCODE = 0x4000;
    public static final int FLG2_UNICODE = 0x8000;

    // Security mode bits

    public static final int SEC_USER = 0x0001;
    public static final int SEC_ENCRYPT = 0x0002;

    // Raw mode bits

    public static final int RAW_READ = 0x0001;
    public static final int RAW_WRITE = 0x0002;

    // SMB packet buffer

    private byte[] m_smbbuf;

    // Packet type

    private int m_pkttype;

    // Current byte area pack/unpack position

    protected int m_pos;
    protected int m_endpos;

    /**
     * Default constructor
     */
    public SMBPacket()
    {
        m_smbbuf = new byte[DEFAULT_BUFSIZE];
        InitializeBuffer();
    }

    /**
     * Construct an SMB packet using the specified packet buffer.
     * 
     * @param buf SMB packet buffer.
     */
    public SMBPacket(byte[] buf)
    {
        m_smbbuf = buf;
    }

    /**
     * Construct an SMB packet of the specified size.
     * 
     * @param siz Size of SMB packet buffer to allocate.
     */
    public SMBPacket(int siz)
    {
        m_smbbuf = new byte[siz];
        InitializeBuffer();
    }

    /**
     * Copy constructor
     * 
     * @param pkt SMBPacket
     */
    public SMBPacket(SMBPacket pkt)
    {

        // Allocate a new buffer

        m_smbbuf = new byte[pkt.getBuffer().length];

        // Copy the valid data to the new packet

        System.arraycopy(pkt.getBuffer(), 0, m_smbbuf, 0, pkt.getLength());
    }

    /**
     * Clear the data byte count
     */
    public final void clearBytes()
    {
        int offset = getByteOffset() - 2;
        DataPacker.putIntelShort(0, m_smbbuf, offset);
    }

    /**
     * Dump the SMB packet to the debug stream
     */
    public final void DumpPacket()
    {
    }

    /**
     * Check if the error class/code match the specified error/class
     * 
     * @param errClass int
     * @param errCode int
     * @return boolean
     */
    public final boolean equalsError(int errClass, int errCode)
    {
        if (getErrorClass() == errClass && getErrorCode() == errCode)
            return true;
        return false;
    }

    /**
     * Get the secondary command code
     * 
     * @return Secondary command code
     */
    public final int getAndXCommand()
    {
        return (int) (m_smbbuf[ANDXCOMMAND] & 0xFF);
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
        return m_smbbuf.length - RFCNetBIOSProtocol.HEADER_LEN;
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
        int pos = WORDCNT + (2 * pCnt) + 3;
        return pos;
    }

    /**
     * Get the SMB command
     * 
     * @return SMB command code.
     */
    public final int getCommand()
    {
        return (int) (m_smbbuf[COMMAND] & 0xFF);
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
     * Check if the packet contains ASCII or Unicode strings
     * 
     * @return boolean
     */
    public final boolean isUnicode()
    {
        return (getFlags2() & FLG2_UNICODE) != 0 ? true : false;
    }

    /**
     * Check if the packet is using caseless filenames
     * 
     * @return boolean
     */
    public final boolean isCaseless()
    {
        return (getFlags() & FLG_CASELESS) != 0 ? true : false;
    }

    /**
     * Check if long file names are being used
     * 
     * @return boolean
     */
    public final boolean isLongFileNames()
    {
        return (getFlags2() & FLG2_LONGFILENAMES) != 0 ? true : false;
    }

    /**
     * Check if long error codes are being used
     * 
     * @return boolean
     */
    public final boolean isLongErrorCode()
    {
        return (getFlags2() & FLG2_LONGERRORCODE) != 0 ? true : false;
    }

    /**
     * Get the SMB error class
     * 
     * @return SMB error class.
     */
    public final int getErrorClass()
    {
        return (int) m_smbbuf[ERRORCLASS] & 0xFF;
    }

    /**
     * Get the SMB error code
     * 
     * @return SMB error code.
     */
    public final int getErrorCode()
    {
        return (int) m_smbbuf[ERROR] & 0xFF;
    }

    /**
     * Get the SMB flags value.
     * 
     * @return SMB flags value.
     */
    public final int getFlags()
    {
        return (int) m_smbbuf[FLAGS] & 0xFF;
    }

    /**
     * Get the SMB flags2 value.
     * 
     * @return SMB flags2 value.
     */
    public final int getFlags2()
    {
        return (int) DataPacker.getIntelShort(m_smbbuf, FLAGS2);
    }

    /**
     * Calculate the total used packet length.
     * 
     * @return Total used packet length.
     */
    public final int getLength()
    {
        return (getByteOffset() + getByteCount()) - SIGNATURE;
    }

    /**
     * Get the long SMB error code
     * 
     * @return Long SMB error code.
     */
    public final int getLongErrorCode()
    {
        return DataPacker.getIntelInt(m_smbbuf, ERRORCODE);
    }

    /**
     * Get the multiplex identifier.
     * 
     * @return Multiplex identifier.
     */
    public final int getMultiplexId()
    {
        return DataPacker.getIntelShort(m_smbbuf, MID);
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

        int pos = WORDCNT + (2 * idx) + 1;
        return (int) (DataPacker.getIntelShort(m_smbbuf, pos) & 0xFFFF);
    }

    /**
     * Get the specified parameter words, as an int value.
     * 
     * @param idx Parameter index (zero based).
     * @param val Parameter value.
     */
    public final int getParameterLong(int idx)
    {
        int pos = WORDCNT + (2 * idx) + 1;
        return DataPacker.getIntelInt(m_smbbuf, pos);
    }

    /**
     * Get the parameter count
     * 
     * @return Parameter word count.
     */
    public final int getParameterCount()
    {
        return (int) m_smbbuf[WORDCNT];
    }

    /**
     * Get the process indentifier (PID)
     * 
     * @return Process identifier value.
     */
    public final int getProcessId()
    {
        return DataPacker.getIntelShort(m_smbbuf, PID);
    }

    /**
     * Get the tree identifier (TID)
     * 
     * @return Tree identifier (TID)
     */
    public final int getTreeId()
    {
        return DataPacker.getIntelShort(m_smbbuf, TID);
    }

    /**
     * Get the user identifier (UID)
     * 
     * @return User identifier (UID)
     */
    public final int getUserId()
    {
        return DataPacker.getIntelShort(m_smbbuf, UID);
    }

    /**
     * Initialize the SMB packet buffer.
     */
    private final void InitializeBuffer()
    {

        // Set the packet signature

        m_smbbuf[SIGNATURE] = (byte) 0xFF;
        m_smbbuf[SIGNATURE + 1] = (byte) 'S';
        m_smbbuf[SIGNATURE + 2] = (byte) 'M';
        m_smbbuf[SIGNATURE + 3] = (byte) 'B';
    }

    /**
     * Determine if this packet is an SMB response, or command packet
     * 
     * @return true if this SMB packet is a response, else false
     */
    public final boolean isResponse()
    {
        int resp = getFlags();
        if ((resp & FLG_RESPONSE) != 0)
            return true;
        return false;
    }

    /**
     * Check if the response packet is valid, ie. type and flags
     * 
     * @return true if the SMB packet is a response packet and the response is valid, else false.
     */
    public final boolean isValidResponse()
    {

        // Check if this is a response packet, and the correct type of packet

        if (isResponse() && getCommand() == m_pkttype)
        {

            // Check if standard error codes or NT 32-bit error codes are being used

            if ((getFlags2() & FLG2_LONGERRORCODE) == 0)
            {
                if (getErrorClass() == SMBStatus.Success)
                    return true;
            }
            else if (getLongErrorCode() == SMBStatus.NTSuccess)
                return true;
        }
        return false;
    }

    /**
     * Pack a byte (8 bit) value into the byte area
     * 
     * @param val byte
     */
    public final void packByte(byte val)
    {
        m_smbbuf[m_pos++] = val;
    }

    /**
     * Pack a byte (8 bit) value into the byte area
     * 
     * @param val int
     */
    public final void packByte(int val)
    {
        m_smbbuf[m_pos++] = (byte) val;
    }

    /**
     * Pack the specified bytes into the byte area
     * 
     * @param byts byte[]
     * @param len int
     */
    public final void packBytes(byte[] byts, int len)
    {
        for (int i = 0; i < len; i++)
            m_smbbuf[m_pos++] = byts[i];
    }

    /**
     * Pack a string using either ASCII or Unicode into the byte area
     * 
     * @param str String
     * @param uni boolean
     */
    public final void packString(String str, boolean uni)
    {

        // Check for Unicode or ASCII

        if (uni)
        {

            // Word align the buffer position, pack the Unicode string

            m_pos = DataPacker.wordAlign(m_pos);
            DataPacker.putUnicodeString(str, m_smbbuf, m_pos, true);
            m_pos += (str.length() * 2) + 2;
        }
        else
        {

            // Pack the ASCII string

            DataPacker.putString(str, m_smbbuf, m_pos, true);
            m_pos += str.length() + 1;
        }
    }

    /**
     * Pack a word (16 bit) value into the byte area
     * 
     * @param val int
     */
    public final void packWord(int val)
    {
        DataPacker.putIntelShort(val, m_smbbuf, m_pos);
        m_pos += 2;
    }

    /**
     * Pack a 32 bit integer value into the byte area
     * 
     * @param val int
     */
    public final void packInt(int val)
    {
        DataPacker.putIntelInt(val, m_smbbuf, m_pos);
        m_pos += 4;
    }

    /**
     * Pack a long integer (64 bit) value into the byte area
     * 
     * @param val long
     */
    public final void packLong(long val)
    {
        DataPacker.putIntelLong(val, m_smbbuf, m_pos);
        m_pos += 8;
    }

    /**
     * Return the current byte area buffer position
     * 
     * @return int
     */
    public final int getPosition()
    {
        return m_pos;
    }

    /**
     * Unpack a byte value from the byte area
     * 
     * @return int
     */
    public final int unpackByte()
    {
        return (int) m_smbbuf[m_pos++];
    }

    /**
     * Unpack a block of bytes from the byte area
     * 
     * @param len int
     * @return byte[]
     */
    public final byte[] unpackBytes(int len)
    {
        if (len <= 0)
            return null;

        byte[] buf = new byte[len];
        System.arraycopy(m_smbbuf, m_pos, buf, 0, len);
        m_pos += len;
        return buf;
    }

    /**
     * Unpack a word (16 bit) value from the byte area
     * 
     * @return int
     */
    public final int unpackWord()
    {
        int val = DataPacker.getIntelShort(m_smbbuf, m_pos);
        m_pos += 2;
        return val;
    }

    /**
     * Unpack an integer (32 bit) value from the byte/parameter area
     * 
     * @return int
     */
    public final int unpackInt()
    {
        int val = DataPacker.getIntelInt(m_smbbuf, m_pos);
        m_pos += 4;
        return val;
    }

    /**
     * Unpack a long integer (64 bit) value from the byte area
     * 
     * @return long
     */
    public final long unpackLong()
    {
        long val = DataPacker.getIntelLong(m_smbbuf, m_pos);
        m_pos += 8;
        return val;
    }

    /**
     * Unpack a string from the byte area
     * 
     * @param uni boolean
     * @return String
     */
    public final String unpackString(boolean uni)
    {

        // Check for Unicode or ASCII

        String ret = null;

        if (uni)
        {

            // Word align the current buffer position

            m_pos = DataPacker.wordAlign(m_pos);
            ret = DataPacker.getUnicodeString(m_smbbuf, m_pos, 255);
            if (ret != null)
                m_pos += (ret.length() * 2) + 2;
        }
        else
        {

            // Unpack the ASCII string

            ret = DataPacker.getString(m_smbbuf, m_pos, 255);
            if (ret != null)
                m_pos += ret.length() + 1;
        }

        // Return the string

        return ret;
    }

    /**
     * Unpack a string from the byte area
     * 
     * @param len int
     * @param uni boolean
     * @return String
     */
    public final String unpackString(int len, boolean uni)
    {

        // Check for Unicode or ASCII

        String ret = null;

        if (uni)
        {

            // Word align the current buffer position

            m_pos = DataPacker.wordAlign(m_pos);
            ret = DataPacker.getUnicodeString(m_smbbuf, m_pos, len);
            if (ret != null)
                m_pos += (ret.length() * 2);
        }
        else
        {

            // Unpack the ASCII string

            ret = DataPacker.getString(m_smbbuf, m_pos, len);
            if (ret != null)
                m_pos += ret.length();
        }

        // Return the string

        return ret;
    }

    /**
     * Check if there is more data in the byte area
     * 
     * @return boolean
     */
    public final boolean hasMoreData()
    {
        if (m_pos < m_endpos)
            return true;
        return false;
    }

    /**
     * Receive an SMB response packet.
     * 
     * @param sess NetBIOS session to receive the SMB packet on.
     * @exception java.io.IOException If an I/O error occurs.
     */
    private final void ReceiveSMB(NetBIOSSession sess) throws java.io.IOException
    {

        if (sess.Receive(m_smbbuf, RFCNetBIOSProtocol.TMO) >= MIN_RXLEN)
            return;

        // Not enough data received for an SMB header

        throw new java.io.IOException("Short NetBIOS receive");
    }

    /**
     * Set the secondary SMB command
     * 
     * @param cmd Secondary SMB command code.
     */
    public final void setAndXCommand(int cmd)
    {

        // Set the chained command packet type

        m_smbbuf[ANDXCOMMAND] = (byte) cmd;
        m_smbbuf[ANDXRESERVED] = (byte) 0;

        // If the AndX command is disabled clear the offset to the chained packet

        if (cmd == PacketType.NoChainedCommand)
            setParameter(1, 0);
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
     * Set the data byte count for this SMB packet
     */

    public final void setByteCount()
    {
        int offset = getByteOffset() - 2;
        int len = m_pos - getByteOffset();
        DataPacker.putIntelShort(len, m_smbbuf, offset);
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
        m_pkttype = cmd;
        m_smbbuf[COMMAND] = (byte) cmd;
    }

    /**
     * Set the SMB error class.
     * 
     * @param cl SMB error class.
     */
    public final void setErrorClass(int cl)
    {
        m_smbbuf[ERRORCLASS] = (byte) (cl & 0xFF);
    }

    /**
     * Set the SMB error code
     * 
     * @param sts SMB error code.
     */
    public final void setErrorCode(int sts)
    {
        m_smbbuf[ERROR] = (byte) (sts & 0xFF);
    }

    /**
     * Set the SMB flags value.
     * 
     * @param flg SMB flags value.
     */
    public final void setFlags(int flg)
    {
        m_smbbuf[FLAGS] = (byte) flg;
    }

    /**
     * Set the SMB flags2 value.
     * 
     * @param flg SMB flags2 value.
     */
    public final void setFlags2(int flg)
    {
        DataPacker.putIntelShort(flg, m_smbbuf, FLAGS2);
    }

    /**
     * Set the multiplex identifier.
     * 
     * @param mid Multiplex identifier
     */
    public final void setMultiplexId(int mid)
    {
        DataPacker.putIntelShort(mid, m_smbbuf, MID);
    }

    /**
     * Set the specified parameter word.
     * 
     * @param idx Parameter index (zero based).
     * @param val Parameter value.
     */
    public final void setParameter(int idx, int val)
    {
        int pos = WORDCNT + (2 * idx) + 1;
        DataPacker.putIntelShort(val, m_smbbuf, pos);
    }

    /**
     * Set the specified parameter words.
     * 
     * @param idx Parameter index (zero based).
     * @param val Parameter value.
     */

    public final void setParameterLong(int idx, int val)
    {
        int pos = WORDCNT + (2 * idx) + 1;
        DataPacker.putIntelInt(val, m_smbbuf, pos);
    }

    /**
     * Set the parameter count
     * 
     * @param cnt Parameter word count.
     */
    public final void setParameterCount(int cnt)
    {
        m_smbbuf[WORDCNT] = (byte) cnt;
    }

    /**
     * Set the process identifier value (PID).
     * 
     * @param pid Process identifier value.
     */
    public final void setProcessId(int pid)
    {
        DataPacker.putIntelShort(pid, m_smbbuf, PID);
    }

    /**
     * Set the packet sequence number, for connectionless commands.
     * 
     * @param seq Sequence number.
     */
    public final void setSeqNo(int seq)
    {
        DataPacker.putIntelShort(seq, m_smbbuf, SEQNO);
    }

    /**
     * Set the session id.
     * 
     * @param sid Session id.
     */
    public final void setSID(int sid)
    {
        DataPacker.putIntelShort(sid, m_smbbuf, SID);
    }

    /**
     * Set the tree identifier (TID)
     * 
     * @param tid Tree identifier value.
     */
    public final void setTreeId(int tid)
    {
        DataPacker.putIntelShort(tid, m_smbbuf, TID);
    }

    /**
     * Set the user identifier (UID)
     * 
     * @param uid User identifier value.
     */
    public final void setUserId(int uid)
    {
        DataPacker.putIntelShort(uid, m_smbbuf, UID);
    }

    /**
     * Align the byte area pointer on an int (32bit) boundary
     */
    public final void alignBytePointer()
    {
        m_pos = DataPacker.longwordAlign(m_pos);
    }

    /**
     * Reset the byte/parameter pointer area for packing/unpacking data items from the packet
     */
    public final void resetBytePointer()
    {
        m_pos = getByteOffset();
        m_endpos = m_pos + getByteCount();
    }

    /**
     * Reset the byte/parameter pointer area for packing/unpacking data items from the packet, and
     * align the buffer on an int (32bit) boundary
     */
    public final void resetBytePointerAlign()
    {
        m_pos = DataPacker.longwordAlign(getByteOffset());
        m_endpos = m_pos + getByteCount();
    }

    /**
     * Reset the byte/parameter pointer area for packing/unpacking paramaters from the packet
     */
    public final void resetParameterPointer()
    {
        m_pos = PARAMWORDS;
    }

    /**
     * Set the unpack pointer to the specified offset, for AndX processing
     * 
     * @param off int
     * @param len int
     */
    public final void setBytePointer(int off, int len)
    {
        m_pos = off;
        m_endpos = m_pos + len;
    }

    /**
     * Skip a number of bytes in the parameter/byte area
     * 
     * @param cnt int
     */
    public final void skipBytes(int cnt)
    {
        m_pos += cnt;
    }
}