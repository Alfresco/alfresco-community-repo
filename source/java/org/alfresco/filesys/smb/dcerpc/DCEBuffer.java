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
package org.alfresco.filesys.smb.dcerpc;

import org.alfresco.filesys.smb.NTTime;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.smb.TransactBuffer;
import org.alfresco.filesys.util.DataBuffer;
import org.alfresco.filesys.util.DataPacker;
import org.alfresco.filesys.util.HexDump;

/**
 * DCE Buffer Class
 */
public class DCEBuffer
{

    // Header value types

    public static final int HDR_VERMAJOR        = 0;
    public static final int HDR_VERMINOR        = 1;
    public static final int HDR_PDUTYPE         = 2;
    public static final int HDR_FLAGS           = 3;
    public static final int HDR_DATAREP         = 4;
    public static final int HDR_FRAGLEN         = 5;
    public static final int HDR_AUTHLEN         = 6;
    public static final int HDR_CALLID          = 7;
    public static final int HDR_ALLOCHINT       = 8;
    public static final int HDR_OPCODE          = 9;
    
    //  Header flags
    
    public static final int FLG_FIRSTFRAG       = 0x01;
    public static final int FLG_LASTFRAG        = 0x02;
    public static final int FLG_CANCEL          = 0x04;
    public static final int FLG_IDEMPOTENT      = 0x20;
    public static final int FLG_BROADCAST       = 0x40;
    
    public static final int FLG_ONLYFRAG        = 0x03;
    
    //  DCE/RPC header offsets
    
    public static final int VERSIONMAJOR        = 0;
    public static final int VERSIONMINOR        = 1;
    public static final int PDUTYPE             = 2;
    public static final int HEADERFLAGS         = 3;
    public static final int PACKEDDATAREP       = 4;
    public static final int FRAGMENTLEN         = 8;
    public static final int AUTHLEN             = 10;
    public static final int CALLID              = 12;
    public static final int DCEDATA             = 16;
    
    //  DCE/RPC Request offsets
    
    public static final int ALLOCATIONHINT      = 16;
    public static final int PRESENTIDENT        = 20;
    public static final int OPERATIONID         = 22;
    public static final int OPERATIONDATA       = 24;
    
    //  DCE/RPC header constants
    
    private static final byte VAL_VERSIONMAJOR  = 5;
    private static final byte VAL_VERSIONMINOR  = 0;
    private static final int  VAL_PACKEDDATAREP = 0x00000010;
    
    //  Data alignment types
    
    public final static int ALIGN_NONE      = -1;
    public final static int ALIGN_SHORT     = 0;
    public final static int ALIGN_INT       = 1;
    public final static int ALIGN_LONG      = 2;
    
    //  Maximum string length
    
    public final static int MAX_STRING_LEN  = 1000;
    
    //  Alignment masks and rounding
    
    private final static int[] _alignMask  = { 0xFFFFFFFE, 0xFFFFFFFC, 0xFFFFFFF8 };
    private final static int[] _alignRound = { 1, 3, 7 };
    
    //  Default buffer allocation
    
    private static final int DEFAULT_BUFSIZE    = 8192;

    //  Maximum buffer size, used when the buffer is reset to release large buffers
    
    private static final int MAX_BUFFER_SIZE    = 65536;
    
    //  Dummy address value to use for pointers within the buffer
    
    private static final int DUMMY_ADDRESS      = 0x12345678;
        
    //  Data buffer and current read/write positions
    
    private byte[] m_buffer;
    private int m_base;
    private int m_pos;
    private int m_rdpos;

    // Error status

    private int m_errorCode;

    /**
     * Default constructor
     */
    public DCEBuffer()
    {
        m_buffer = new byte[DEFAULT_BUFSIZE];
        m_pos = 0;
        m_rdpos = 0;
        m_base = 0;
    }

    /**
     * Class constructor
     * 
     * @param siz int
     */
    public DCEBuffer(int siz)
    {
        m_buffer = new byte[siz];
        m_pos = 0;
        m_rdpos = 0;
        m_base = 0;
    }

    /**
     * Class constructor
     * 
     * @param buf byte[]
     * @param startPos int
     * @param len int
     */
    public DCEBuffer(byte[] buf, int startPos, int len)
    {
        m_buffer = buf;
        m_pos = startPos + len;
        m_rdpos = startPos;
        m_base = startPos;
    }

    /**
     * Class constructor
     * 
     * @param buf byte[]
     * @param startPos int
     */
    public DCEBuffer(byte[] buf, int startPos)
    {
        m_buffer = buf;
        m_pos = startPos;
        m_rdpos = startPos;
        m_base = startPos;
    }

    /**
     * Class constructor
     * 
     * @param tbuf TransactBuffer
     */
    public DCEBuffer(TransactBuffer tbuf)
    {
        DataBuffer dataBuf = tbuf.getDataBuffer();
        m_buffer = dataBuf.getBuffer();
        m_rdpos = dataBuf.getOffset();
        m_base = dataBuf.getOffset();
        m_pos = m_rdpos + dataBuf.getLength();
    }

    /**
     * Return the DCE buffer
     * 
     * @return byte[]
     */
    public final byte[] getBuffer()
    {
        return m_buffer;
    }

    /**
     * Return the current used buffer length
     * 
     * @return int
     */
    public final int getLength()
    {
        return m_pos;
    }

    /**
     * Return the read buffer position
     * 
     * @return int
     */
    public final int getReadPosition()
    {
        return m_rdpos;
    }

    /**
     * Return the write buffer position
     * 
     * @return int
     */
    public final int getWritePosition()
    {
        return m_pos;
    }

    /**
     * Return the amount of data left to read
     * 
     * @return int
     */
    public final int getAvailableLength()
    {
        return m_pos - m_rdpos;
    }

    /**
     * Get a byte from the buffer
     * 
     * @param align int
     * @return int
     * @exception DCEBufferException
     */
    public final int getByte(int align) throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length - m_rdpos < 1)
            throw new DCEBufferException("End of DCE buffer");

        // Unpack the integer value

        int bval = (int) (m_buffer[m_rdpos++] & 0xFF);
        alignRxPosition(align);
        return bval;
    }

    /**
     * Get a block of bytes from the buffer
     * 
     * @param buf byte[]
     * @param len int
     * @return byte[]
     * @throws DCEBufferException
     */
    public final byte[] getBytes(byte[] buf, int len) throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length - m_rdpos < len)
            throw new DCEBufferException("End of DCE buffer");

        // Check if a return buffer should be allocated

        if (buf == null)
            buf = new byte[len];

        // Unpack the bytes

        for (int i = 0; i < len; i++)
            buf[i] = m_buffer[m_rdpos++];
        return buf;
    }

    /**
     * Get a short from the buffer
     * 
     * @return int
     * @exception DCEBufferException
     */
    public final int getShort() throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length - m_rdpos < 2)
            throw new DCEBufferException("End of DCE buffer");

        // Unpack the integer value

        int sval = (int) DataPacker.getIntelShort(m_buffer, m_rdpos);
        m_rdpos += 2;
        return sval;
    }

    /**
     * Get a short from the buffer and align the read pointer
     * 
     * @param align int
     * @return int
     * @exception DCEBufferException
     */
    public final int getShort(int align) throws DCEBufferException
    {

        // Read the short

        int sval = getShort();

        // Align the read position

        alignRxPosition(align);

        // Return the short value

        return sval;
    }

    /**
     * Get an integer from the buffer
     * 
     * @return int
     * @exception DCEBufferException
     */
    public final int getInt() throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length - m_rdpos < 4)
            throw new DCEBufferException("End of DCE buffer");

        // Unpack the integer value

        int ival = DataPacker.getIntelInt(m_buffer, m_rdpos);
        m_rdpos += 4;
        return ival;
    }

    /**
     * Get a pointer from the buffer
     * 
     * @return int
     * @exception DCEBufferException
     */
    public final int getPointer() throws DCEBufferException
    {
        return getInt();
    }

    /**
     * Get a pointer from the buffer and return either an empty string if the pointer is valid or
     * null.
     * 
     * @return String
     * @exception DCEBufferException
     */
    public final String getStringPointer() throws DCEBufferException
    {
        if (getInt() == 0)
            return null;
        return "";
    }

    /**
     * Get a character array header from the buffer and return either an empty string if the pointer
     * is valid or null.
     * 
     * @return String
     * @exception DCEBufferException
     */
    public final String getCharArrayPointer() throws DCEBufferException
    {

        // Get the array length and size

        int len = getShort();
        int siz = getShort();
        return getStringPointer();
    }

    /**
     * Get a character array from the buffer if the String variable is not null, and align on the
     * specified boundary
     * 
     * @param strVar String
     * @param align int
     * @return String
     * @exception DCEBufferException
     */
    public final String getCharArrayNotNull(String strVar, int align) throws DCEBufferException
    {

        // Check if the string variable is not null

        String str = "";

        if (strVar != null)
        {

            // Read the string

            str = getCharArray();

            // Align the read position

            alignRxPosition(align);
        }

        // Return the string

        return str;
    }

    /**
     * Get a long (64 bit) value from the buffer
     * 
     * @return long
     * @exception DCEBufferException
     */
    public final long getLong() throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length - m_rdpos < 8)
            throw new DCEBufferException("End of DCE buffer");

        // Unpack the integer value

        long lval = DataPacker.getIntelLong(m_buffer, m_rdpos);
        m_rdpos += 8;
        return lval;
    }

    /**
     * Return a DCE/RPC header value
     * 
     * @param valTyp int
     * @return int
     */
    public final int getHeaderValue(int valTyp)
    {

        int result = -1;

        switch (valTyp)
        {

        // Version major

        case HDR_VERMAJOR:
            result = (int) (m_buffer[m_base + VERSIONMAJOR] & 0xFF);
            break;

        // Version minor

        case HDR_VERMINOR:
            result = (int) (m_buffer[m_base + VERSIONMINOR] & 0xFF);
            break;

        // PDU type

        case HDR_PDUTYPE:
            result = (int) (m_buffer[m_base + PDUTYPE] & 0xFF);
            break;

        // Flags

        case HDR_FLAGS:
            result = (int) (m_buffer[m_base + HEADERFLAGS] & 0xFF);
            break;

        // Data representation

        case HDR_DATAREP:
            result = DataPacker.getIntelInt(m_buffer, m_base + VERSIONMINOR);
            break;

        // Authorisation length

        case HDR_AUTHLEN:
            result = DataPacker.getIntelInt(m_buffer, m_base + AUTHLEN);
            break;

        // Fragment length

        case HDR_FRAGLEN:
            result = DataPacker.getIntelInt(m_buffer, m_base + FRAGMENTLEN);
            break;

        // Call id

        case HDR_CALLID:
            result = DataPacker.getIntelInt(m_buffer, m_base + CALLID);
            break;

        // Request allocation hint

        case HDR_ALLOCHINT:
            result = DataPacker.getIntelInt(m_buffer, m_base + ALLOCATIONHINT);
            break;

        // Request opcode

        case HDR_OPCODE:
            result = DataPacker.getIntelShort(m_buffer, m_base + OPERATIONID);
            break;
        }

        // Return the header value

        return result;
    }

    /**
     * Set a DCE/RPC header value
     * 
     * @param typ int
     * @param val int
     */
    public final void setHeaderValue(int typ, int val)
    {

        switch (typ)
        {

        // Version major

        case HDR_VERMAJOR:
            m_buffer[m_base + VERSIONMAJOR] = (byte) (val & 0xFF);
            break;

        // Version minor

        case HDR_VERMINOR:
            m_buffer[m_base + VERSIONMINOR] = (byte) (val & 0xFF);
            break;

        // PDU type

        case HDR_PDUTYPE:
            m_buffer[m_base + PDUTYPE] = (byte) (val & 0xFF);
            break;

        // Flags

        case HDR_FLAGS:
            m_buffer[m_base + HEADERFLAGS] = (byte) (val & 0xFF);
            break;

        // Data representation

        case HDR_DATAREP:
            DataPacker.putIntelInt(val, m_buffer, m_base + PACKEDDATAREP);
            break;

        // Authorisation length

        case HDR_AUTHLEN:
            DataPacker.putIntelInt(val, m_buffer, m_base + AUTHLEN);
            break;

        // Fragment length

        case HDR_FRAGLEN:
            DataPacker.putIntelInt(val, m_buffer, m_base + FRAGMENTLEN);
            break;

        // Call id

        case HDR_CALLID:
            DataPacker.putIntelInt(val, m_buffer, m_base + CALLID);
            break;

        // Request allocation hint

        case HDR_ALLOCHINT:
            DataPacker.putIntelInt(val, m_buffer, m_base + ALLOCATIONHINT);
            break;

        // Request opcode

        case HDR_OPCODE:
            DataPacker.putIntelShort(val, m_buffer, m_base + OPERATIONID);
            break;
        }
    }

    /**
     * Determine if this is the first fragment
     * 
     * @return boolean
     */
    public final boolean isFirstFragment()
    {
        if ((getHeaderValue(HDR_FLAGS) & FLG_FIRSTFRAG) != 0)
            return true;
        return false;
    }

    /**
     * Determine if this is the last fragment
     * 
     * @return boolean
     */
    public final boolean isLastFragment()
    {
        if ((getHeaderValue(HDR_FLAGS) & FLG_LASTFRAG) != 0)
            return true;
        return false;
    }

    /**
     * Determine if this is the only fragment in the request
     * 
     * @return boolean
     */
    public final boolean isOnlyFragment()
    {
        if ((getHeaderValue(HDR_FLAGS) & FLG_ONLYFRAG) == FLG_ONLYFRAG)
            return true;
        return false;
    }

    /**
     * Check if the status indicates that there are more entries available
     * 
     * @return boolean
     */
    public final boolean hasMoreEntries()
    {
        return getStatusCode() == SMBStatus.Win32MoreEntries ? true : false;
    }

    /**
     * Check if the status indicates success
     * 
     * @return boolean
     */
    public final boolean hasSuccessStatus()
    {
        return getStatusCode() == SMBStatus.NTSuccess ? true : false;
    }

    /**
     * Skip over a number of bytes
     * 
     * @param cnt int
     * @exception DCEBufferException
     */
    public final void skipBytes(int cnt) throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length - m_rdpos < cnt)
            throw new DCEBufferException("End of DCE buffer");

        // Skip bytes

        m_rdpos += cnt;
    }

    /**
     * Skip over a pointer
     * 
     * @exception DCEBufferException
     */
    public final void skipPointer() throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length - m_rdpos < 4)
            throw new DCEBufferException("End of DCE buffer");

        // Skip the 32bit pointer value

        m_rdpos += 4;
    }

    /**
     * Set the read position
     * 
     * @param pos int
     * @exception DCEBufferException
     */
    public final void positionAt(int pos) throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length < pos)
            throw new DCEBufferException("End of DCE buffer");

        // Set the read position

        m_rdpos = pos;
    }

    /**
     * Get a number of Unicode characters from the buffer and return as a string
     * 
     * @param len int
     * @return String
     * @exception DCEBufferException
     */
    public final String getChars(int len) throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length - m_rdpos < (len * 2))
            throw new DCEBufferException("End of DCE buffer");

        // Build up the return string

        StringBuffer str = new StringBuffer(len);
        char curChar;

        while (len-- > 0)
        {

            // Get a Unicode character from the buffer

            curChar = (char) ((m_buffer[m_rdpos + 1] << 8) + m_buffer[m_rdpos]);
            m_rdpos += 2;

            // Add the character to the string

            str.append(curChar);
        }

        // Return the string

        return str.toString();
    }

    /**
     * Get the status code from the end of the data block
     * 
     * @return int
     */
    public final int getStatusCode()
    {

        // Read the integer value at the end of the buffer

        int ival = DataPacker.getIntelInt(m_buffer, m_pos - 4);
        return ival;
    }

    /**
     * Get a string from the buffer
     * 
     * @return String
     * @exception DCEBufferException
     */
    public final String getString() throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length - m_rdpos < 12)
            throw new DCEBufferException("End of DCE buffer");

        // Unpack the string

        int maxLen = getInt();
        skipBytes(4); // offset
        int strLen = getInt();

        String str = DataPacker.getUnicodeString(m_buffer, m_rdpos, strLen);
        m_rdpos += (strLen * 2);
        return str;
    }

    /**
     * Get a character array from the buffer
     * 
     * @return String
     * @exception DCEBufferException
     */
    public final String getCharArray() throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length - m_rdpos < 12)
            throw new DCEBufferException("End of DCE buffer");

        // Unpack the string

        int maxLen = getInt();
        skipBytes(4); // offset
        int strLen = getInt(); // in unicode chars

        String str = null;
        if (strLen > 0)
        {
            str = DataPacker.getUnicodeString(m_buffer, m_rdpos, strLen);
            m_rdpos += (strLen * 2);
        }
        return str;
    }

    /**
     * Get a character array from the buffer and align on the specified boundary
     * 
     * @param align int
     * @return String
     * @exception DCEBufferException
     */
    public final String getCharArray(int align) throws DCEBufferException
    {

        // Read the string

        String str = getCharArray();

        // Align the read position

        alignRxPosition(align);

        // Return the string

        return str;
    }

    /**
     * Get a string from the buffer and align on the specified boundary
     * 
     * @param align int
     * @return String
     * @exception DCEBufferException
     */
    public final String getString(int align) throws DCEBufferException
    {

        // Read the string

        String str = getString();

        // Align the read position

        alignRxPosition(align);

        // Return the string

        return str;
    }

    /**
     * Get a string from the buffer if the String variable is not null, and align on the specified
     * boundary
     * 
     * @param strVar String
     * @param align int
     * @return String
     * @exception DCEBufferException
     */
    public final String getStringNotNull(String strVar, int align) throws DCEBufferException
    {

        // Check if the string variable is not null

        String str = "";

        if (strVar != null)
        {

            // Read the string

            str = getString();

            // Align the read position

            alignRxPosition(align);
        }

        // Return the string

        return str;
    }

    /**
     * Get a string from a particular position in the buffer
     * 
     * @param pos int
     * @return String
     * @exception DCEBufferException
     */
    public final String getStringAt(int pos) throws DCEBufferException
    {

        // Check if position is within the buffer

        if (m_buffer.length < pos)
            throw new DCEBufferException("Buffer offset out of range, " + pos);

        // Unpack the string

        String str = DataPacker.getUnicodeString(m_buffer, pos, MAX_STRING_LEN);
        return str;
    }

    /**
     * Read a Unicode string header and return the string length. -1 indicates a null pointer in the
     * string header.
     * 
     * @return int
     * @exception DCEBufferException
     */
    public final int getUnicodeHeaderLength() throws DCEBufferException
    {

        // Check if there is enough data in the buffer for the Unicode header

        if (m_buffer.length - m_rdpos < 8)
            throw new DCEBufferException("End of DCE buffer");

        // Get the string length

        int len = (int) DataPacker.getIntelShort(m_buffer, m_rdpos);
        m_rdpos += 4; // skip the max length too
        int ptr = DataPacker.getIntelInt(m_buffer, m_rdpos);
        m_rdpos += 4;

        // Check if the pointer is valid

        if (ptr == 0)
            return -1;
        return len;
    }

    /**
     * Get a unicode string from the current position in the buffer
     * 
     * @return String
     * @exception DCEBufferException
     */
    public final String getUnicodeString() throws DCEBufferException
    {

        // Check if there is any buffer to read

        if (m_buffer.length - m_rdpos <= 0)
            throw new DCEBufferException("No more buffer");

        // Unpack the string

        String str = DataPacker.getUnicodeString(m_buffer, m_rdpos, MAX_STRING_LEN);
        if (str != null)
            m_rdpos += (str.length() * 2) + 2;
        return str;
    }

    /**
     * Get a data block from the buffer and align on the specified boundary
     * 
     * @param align int
     * @return byte[]
     * @exception DCEBufferException
     */
    public final byte[] getDataBlock(int align) throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length - m_rdpos < 12)
            throw new DCEBufferException("End of DCE buffer");

        // Unpack the data block

        int len = getInt();
        m_rdpos += 8; // skip undoc and max_len ints

        // Copy the raw data block

        byte[] dataBlk = null;

        if (len > 0)
        {

            // Allocate the data block buffer

            dataBlk = new byte[len];

            // Copy the raw data

            System.arraycopy(m_buffer, m_rdpos, dataBlk, 0, len);
        }

        // Update the buffer position and align

        m_rdpos += len;
        alignRxPosition(align);
        return dataBlk;
    }

    /**
     * Get a UUID from the buffer
     * 
     * @param readVer boolean
     * @return UUID
     * @exception DCEBufferException
     */
    public final UUID getUUID(boolean readVer) throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        int len = UUID.UUID_LENGTH_BINARY;
        if (readVer == true)
            len += 4;

        if (m_buffer.length - m_rdpos < len)
            throw new DCEBufferException("End of DCE buffer");

        // Unpack the UUID

        UUID uuid = new UUID(m_buffer, m_rdpos);
        m_rdpos += UUID.UUID_LENGTH_BINARY;

        if (readVer == true)
        {
            int ver = getInt();
            uuid.setVersion(ver);
        }

        return uuid;
    }

    /**
     * Get an NT 64bit time value. If the value is valid then convert to a Java time value
     * 
     * @return long
     * @throws DCEBufferException
     */
    public final long getNTTime() throws DCEBufferException
    {

        // Get the raw NT time value

        long ntTime = getLong();
        if (ntTime == 0 || ntTime == NTTime.InfiniteTime)
            return ntTime;

        // Convert the time to a Java time value

        return NTTime.toJavaDate(ntTime);
    }

    /**
     * Get a byte structure that has a header
     * 
     * @param buf byte[]
     * @throws DCEBufferException
     */
    public final byte[] getByteStructure(byte[] buf) throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length - m_rdpos < 12)
            throw new DCEBufferException("End of DCE buffer");

        // Unpack the header

        int maxLen = getInt();
        skipBytes(4); // offset
        int bytLen = getInt();

        byte[] bytBuf = buf;
        if (bytBuf.length < bytLen)
            bytBuf = new byte[bytLen];
        return getBytes(bytBuf, bytLen);
    }

    /**
     * Get a handle from the buffer
     * 
     * @param handle PolicyHandle
     * @exception DCEBufferException
     */
    public final void getHandle(PolicyHandle handle) throws DCEBufferException
    {

        // Check if there is enough data in the buffer

        if (m_buffer.length - m_rdpos < PolicyHandle.POLICY_HANDLE_SIZE)
            throw new DCEBufferException("End of DCE buffer");

        // Unpack the policy handle

        m_rdpos = handle.loadPolicyHandle(m_buffer, m_rdpos);
    }

    /**
     * Copy data from the DCE buffer to the user buffer, and update the current read position.
     * 
     * @param buf byte[]
     * @param off int
     * @param cnt int
     * @return int
     * @exception DCEBufferException
     */
    public final int copyData(byte[] buf, int off, int cnt) throws DCEBufferException
    {

        // Check if there is any more data to copy

        if (m_rdpos == m_pos)
            return 0;

        // Calculate the amount of data to copy

        int siz = m_pos - m_rdpos;
        if (siz > cnt)
            siz = cnt;

        // Copy the data to the user buffer and update the current read position

        System.arraycopy(m_buffer, m_rdpos, buf, off, siz);
        m_rdpos += siz;

        // Return the amount of data copied

        return siz;
    }

    /**
     * Append a raw data block to the buffer
     * 
     * @param buf byte[]
     * @param off int
     * @param len int
     * @exception DCEBufferException
     */
    public final void appendData(byte[] buf, int off, int len) throws DCEBufferException
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < len)
            extendBuffer(len);

        // Copy the data to the buffer and update the current write position

        System.arraycopy(buf, off, m_buffer, m_pos, len);
        m_pos += len;
    }

    /**
     * Append an integer to the buffer
     * 
     * @param ival int
     */
    public final void putInt(int ival)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < 4)
            extendBuffer();

        // Pack the integer value

        DataPacker.putIntelInt(ival, m_buffer, m_pos);
        m_pos += 4;
    }

    /**
     * Append a byte value to the buffer
     * 
     * @param bval int
     */
    public final void putByte(int bval)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < 1)
            extendBuffer();

        // Pack the short value

        m_buffer[m_pos++] = (byte) (bval & 0xFF);
    }

    /**
     * Append a byte value to the buffer and align to the specified boundary
     * 
     * @param bval byte
     * @param align int
     */
    public final void putByte(byte bval, int align)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < 1)
            extendBuffer();

        // Pack the short value

        m_buffer[m_pos++] = bval;
        alignPosition(align);
    }

    /**
     * Append a byte value to the buffer and align to the specified boundary
     * 
     * @param bval int
     * @param align int
     */
    public final void putByte(int bval, int align)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < 1)
            extendBuffer();

        // Pack the short value

        m_buffer[m_pos++] = (byte) (bval & 0xFF);
        alignPosition(align);
    }

    /**
     * Append a block of bytes to the buffer
     * 
     * @param bval byte[]
     * @param len int
     */
    public final void putBytes(byte[] bval, int len)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < len)
            extendBuffer();

        // Pack the bytes

        for (int i = 0; i < len; i++)
            m_buffer[m_pos++] = bval[i];
    }

    /**
     * Append a block of bytes to the buffer
     * 
     * @param bval byte[]
     * @param len int
     * @param align int
     */
    public final void putBytes(byte[] bval, int len, int align)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < len)
            extendBuffer();

        // Pack the bytes

        for (int i = 0; i < len; i++)
            m_buffer[m_pos++] = bval[i];

        // Align the new buffer position

        alignPosition(align);
    }

    /**
     * Append a short value to the buffer
     * 
     * @param sval int
     */
    public final void putShort(int sval)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < 2)
            extendBuffer();

        // Pack the short value

        DataPacker.putIntelShort(sval, m_buffer, m_pos);
        m_pos += 2;
    }

    /**
     * Append a DCE string to the buffer
     * 
     * @param str String
     */
    public final void putString(String str)
    {

        // Check if there is enough space in the buffer

        int reqLen = (str.length() * 2) + 24;

        if (m_buffer.length - m_pos < reqLen)
            extendBuffer(reqLen);

        // Pack the string

        m_pos = DCEDataPacker.putDCEString(m_buffer, m_pos, str, false);
    }

    /**
     * Append a DCE string to the buffer and align to the specified boundary
     * 
     * @param str String
     * @param align int
     */
    public final void putString(String str, int align)
    {

        // Check if there is enough space in the buffer

        int reqLen = (str.length() * 2) + 24;

        if (m_buffer.length - m_pos < reqLen)
            extendBuffer(reqLen);

        // Pack the string

        m_pos = DCEDataPacker.putDCEString(m_buffer, m_pos, str, false);

        // Align the new buffer position

        alignPosition(align);
    }

    /**
     * Append a DCE string to the buffer, specify whether the nul is included in the string length
     * or not
     * 
     * @param str String
     * @param align int
     * @param incNul boolean
     */
    public final void putString(String str, int align, boolean incNul)
    {

        // Check if there is enough space in the buffer

        int reqLen = (str.length() * 2) + 24;
        if (incNul)
            reqLen += 2;

        if (m_buffer.length - m_pos < reqLen)
            extendBuffer(reqLen);

        // Pack the string

        m_pos = DCEDataPacker.putDCEString(m_buffer, m_pos, str, incNul);

        // Align the new buffer position

        alignPosition(align);
    }

    /**
     * Append string return buffer details. Some DCE/RPC requests incorrectly send output parameters
     * as input.
     * 
     * @param len int
     * @param align int
     */
    public final void putStringReturn(int len, int align)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < 20)
            extendBuffer();

        // Pack the string return details

        DataPacker.putIntelInt(len, m_buffer, m_pos);
        DataPacker.putZeros(m_buffer, m_pos + 4, 8);
        DataPacker.putIntelInt(DUMMY_ADDRESS, m_buffer, m_pos + 12);
        m_pos += 16;

        // Align the new buffer position

        alignPosition(align);
    }

    /**
     * Append a DCE string header to the buffer
     * 
     * @param str String
     * @param incNul boolean
     */
    public final void putUnicodeHeader(String str, boolean incNul)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < 8)
            extendBuffer();

        // Calculate the string length in bytes

        int sLen = 0;
        if (str != null)
            sLen = str.length() * 2;

        // Pack the string header

        if (str != null)
            DataPacker.putIntelShort(incNul ? sLen + 2 : sLen, m_buffer, m_pos);
        else
            DataPacker.putIntelShort(0, m_buffer, m_pos);

        DataPacker.putIntelShort(sLen != 0 ? sLen + 2 : 0, m_buffer, m_pos + 2);
        DataPacker.putIntelInt(str != null ? DUMMY_ADDRESS : 0, m_buffer, m_pos + 4);

        m_pos += 8;
    }

    /**
     * Append a Unicode return string header to the buffer. Some DCE/RPC requests incorrectly send
     * output parameters as input.
     * 
     * @param len int
     */
    public final void putUnicodeReturn(int len)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < 8)
            extendBuffer();

        // Pack the string header

        DataPacker.putIntelShort(0, m_buffer, m_pos);
        DataPacker.putIntelShort(len, m_buffer, m_pos + 2);
        DataPacker.putIntelInt(DUMMY_ADDRESS, m_buffer, m_pos + 4);

        m_pos += 8;
    }

    /**
     * Append a DCE string header to the buffer
     * 
     * @param len int
     * @param incNul boolean
     */
    public final void putUnicodeHeader(int len)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < 8)
            extendBuffer();

        // Calculate the string length in bytes

        int sLen = len * 2;

        // Pack the string header

        DataPacker.putIntelShort(sLen, m_buffer, m_pos);
        DataPacker.putIntelShort(sLen + 2, m_buffer, m_pos + 2);
        DataPacker.putIntelInt(sLen != 0 ? DUMMY_ADDRESS : 0, m_buffer, m_pos + 4);

        m_pos += 8;
    }

    /**
     * Append an ASCII string to the DCE buffer
     * 
     * @param str String
     * @param incNul boolean
     */
    public final void putASCIIString(String str, boolean incNul)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < (str.length() + 1))
            extendBuffer(str.length() + 2);

        // Pack the string

        m_pos = DataPacker.putString(str, m_buffer, m_pos, incNul);
    }

    /**
     * Append an ASCII string to the DCE buffer, and align on the specified boundary
     * 
     * @param str String
     * @param incNul boolean
     * @param align int
     */
    public final void putASCIIString(String str, boolean incNul, int align)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < (str.length() + 1))
            extendBuffer(str.length() + 8);

        // Pack the string

        m_pos = DataPacker.putString(str, m_buffer, m_pos, incNul);

        // Align the buffer position

        alignPosition(align);
    }

    /**
     * Append a pointer to the buffer.
     * 
     * @param obj Object
     */
    public final void putPointer(Object obj)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < 4)
            extendBuffer();

        // Check if the object is valid, if not then put a null pointer into the buffer

        if (obj == null)
            DataPacker.putZeros(m_buffer, m_pos, 4);
        else
            DataPacker.putIntelInt(DUMMY_ADDRESS, m_buffer, m_pos);
        m_pos += 4;
    }

    /**
     * Append a pointer to the buffer.
     * 
     * @param notNull boolean
     */
    public final void putPointer(boolean notNull)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < 4)
            extendBuffer();

        // Check if the object is valid, if not then put a null pointer into the buffer

        if (notNull == false)
            DataPacker.putZeros(m_buffer, m_pos, 4);
        else
            DataPacker.putIntelInt(DUMMY_ADDRESS, m_buffer, m_pos);
        m_pos += 4;
    }

    /**
     * Append a UUID to the buffer
     * 
     * @param uuid UUID
     * @param writeVer boolean
     */
    public final void putUUID(UUID uuid, boolean writeVer)
    {

        // Check if there is enough space in the buffer

        int len = UUID.UUID_LENGTH_BINARY;
        if (writeVer == true)
            len += 4;

        if (m_buffer.length - m_pos < len)
            extendBuffer();

        // Pack the UUID

        m_pos = uuid.storeUUID(m_buffer, m_pos, writeVer);
    }

    /**
     * Append a policy handle to the buffer
     * 
     * @param handle PolicyHandle
     */
    public final void putHandle(PolicyHandle handle)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < PolicyHandle.POLICY_HANDLE_SIZE)
            extendBuffer(PolicyHandle.POLICY_HANDLE_SIZE);

        // Pack the policy handle

        m_pos = handle.storePolicyHandle(m_buffer, m_pos);
    }

    /**
     * Append a DCE buffer to the current DCE buffer
     * 
     * @param buf DCEBuffer
     */
    public final void putBuffer(DCEBuffer buf)
    {
        try
        {
            appendData(buf.getBuffer(), buf.getReadPosition(), buf.getLength());
        }
        catch (DCEBufferException ex)
        {
        }
    }

    /**
     * Append an error status to the buffer, also sets the error status value
     * 
     * @param sts int
     */
    public final void putErrorStatus(int sts)
    {

        // Check if there is enough space in the buffer

        if (m_buffer.length - m_pos < 4)
            extendBuffer();

        // Pack the status value

        DataPacker.putIntelInt(sts, m_buffer, m_pos);
        m_pos += 4;

        // Save the status value

        m_errorCode = sts;
    }

    /**
     * Append a DCE header to the buffer
     * 
     * @param pdutyp int
     * @param callid int
     */
    public final void putHeader(int pdutyp, int callid)
    {
        m_buffer[m_pos++] = VAL_VERSIONMAJOR;
        m_buffer[m_pos++] = VAL_VERSIONMINOR;
        m_buffer[m_pos++] = (byte) (pdutyp & 0xFF);
        m_buffer[m_pos++] = 0;

        DataPacker.putIntelInt(VAL_PACKEDDATAREP, m_buffer, m_pos);
        m_pos += 4;

        DataPacker.putZeros(m_buffer, m_pos, 4);
        m_pos += 4;

        DataPacker.putIntelInt(callid, m_buffer, m_pos);
        m_pos += 4;
    }

    /**
     * Append a bind header to the buffer
     * 
     * @param callid int
     */
    public final void putBindHeader(int callid)
    {
        putHeader(DCECommand.BIND, callid);
    }

    /**
     * Append a bind acknowlegde header to the buffer
     * 
     * @param callid int
     */
    public final void putBindAckHeader(int callid)
    {
        putHeader(DCECommand.BINDACK, callid);
    }

    /**
     * Append a request header to the buffer
     * 
     * @param callid int
     * @param opcode int
     * @param allocHint int
     */
    public final void putRequestHeader(int callid, int opcode, int allocHint)
    {
        putHeader(DCECommand.REQUEST, callid);
        DataPacker.putIntelInt(allocHint, m_buffer, m_pos);
        m_pos += 4;
        DataPacker.putZeros(m_buffer, m_pos, 2);
        m_pos += 2;
        DataPacker.putIntelShort(opcode, m_buffer, m_pos);
        m_pos += 2;
    }

    /**
     * Append a response header to the buffer
     * 
     * @param callid int
     * @param allocHint int
     */
    public final void putResponseHeader(int callid, int allocHint)
    {
        putHeader(DCECommand.RESPONSE, callid);
        DataPacker.putIntelInt(allocHint, m_buffer, m_pos);
        m_pos += 4;
        DataPacker.putZeros(m_buffer, m_pos, 4);
        m_pos += 4;
    }

    /**
     * Append zero integers to the buffer
     * 
     * @param cnt int
     */
    public final void putZeroInts(int cnt)
    {

        // Check if there is enough space in the buffer

        int bytCnt = cnt * 4;
        if (m_buffer.length - m_pos < bytCnt)
            extendBuffer(bytCnt * 2);

        // Pack the zero integer values

        DataPacker.putZeros(m_buffer, m_pos, bytCnt);
        m_pos += bytCnt;
    }

    /**
     * Reset the buffer pointers to reuse the buffer
     */
    public final void resetBuffer()
    {

        // Reset the read/write positions

        m_pos = 0;
        m_rdpos = 0;

        // If the buffer is over sized release it and allocate a standard sized buffer

        if (m_buffer.length >= MAX_BUFFER_SIZE)
            m_buffer = new byte[DEFAULT_BUFSIZE];
    }

    /**
     * Set the new write position
     * 
     * @param pos int
     */
    public final void setWritePosition(int pos)
    {
        m_pos = pos;
    }

    /**
     * Update the write position by the specified amount
     * 
     * @param len int
     */
    public final void updateWritePosition(int len)
    {
        m_pos += len;
    }

    /**
     * Determine if there is an error status set
     * 
     * @return boolean
     */
    public final boolean hasErrorStatus()
    {
        return m_errorCode != 0 ? true : false;
    }

    /**
     * Return the error status code
     * 
     * @return int
     */
    public final int getErrorStatus()
    {
        return m_errorCode;
    }

    /**
     * Set the error status code
     * 
     * @param sts int
     */
    public final void setErrorStatus(int sts)
    {
        m_errorCode = sts;
    }

    /**
     * Extend the DCE buffer by the specified amount
     * 
     * @param ext int
     */
    private final void extendBuffer(int ext)
    {

        // Create a new buffer of the required size

        byte[] newBuf = new byte[m_buffer.length + ext];

        // Copy the data from the current buffer to the new buffer

        System.arraycopy(m_buffer, 0, newBuf, 0, m_buffer.length);

        // Set the new buffer to be the main buffer

        m_buffer = newBuf;
    }

    /**
     * Extend the DCE buffer, double the currently allocated buffer size
     */
    private final void extendBuffer()
    {
        extendBuffer(m_buffer.length * 2);
    }

    /**
     * Align the current buffer position on the specified boundary
     * 
     * @param align int
     */
    private final void alignPosition(int align)
    {

        // Range check the alignment

        if (align < 0 || align > 2)
            return;

        // Align the buffer position on the required boundary

        m_pos = (m_pos + _alignRound[align]) & _alignMask[align];
    }

    /**
     * Align the receive buffer position on the specified boundary
     * 
     * @param align int
     */
    private final void alignRxPosition(int align)
    {

        // Range check the alignment

        if (align < 0 || align > 2 || m_rdpos >= m_buffer.length)
            return;

        // Align the buffer position on the required boundary

        m_rdpos = (m_rdpos + _alignRound[align]) & _alignMask[align];
    }

    /**
     * Dump the DCE buffered data
     */
    public final void Dump()
    {
        int len = getLength();
        if (len == 0)
            len = 24;
        HexDump.Dump(getBuffer(), len, m_base);
    }
}
