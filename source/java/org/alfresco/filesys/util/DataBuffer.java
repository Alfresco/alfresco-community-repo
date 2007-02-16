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
package org.alfresco.filesys.util;

/**
 * Data Buffer Class
 * <p>
 * Dynamic buffer for getting/setting data blocks.
 */
public class DataBuffer
{

    // Constants

    private static final int DefaultBufferSize = 256;

    // Data buffer, current position and offset

    private byte[] m_data;
    private int m_pos;
    private int m_endpos;
    private int m_offset;

    /**
     * Default constructor
     */
    public DataBuffer()
    {
        m_data = new byte[DefaultBufferSize];
        m_pos = 0;
        m_offset = 0;
    }

    /**
     * Create a data buffer to write data to
     * 
     * @param siz int
     */
    public DataBuffer(int siz)
    {
        m_data = new byte[siz];
        m_pos = 0;
        m_offset = 0;
    }

    /**
     * Create a data buffer to read data from
     * 
     * @param buf byte[]
     * @param off int
     * @param len int
     */
    public DataBuffer(byte[] buf, int off, int len)
    {
        m_data = buf;
        m_offset = off;
        m_pos = off;
        m_endpos = off + len;
    }

    /**
     * Return the data buffer
     * 
     * @return byte[]
     */
    public final byte[] getBuffer()
    {
        return m_data;
    }

    /**
     * Return the data length
     * 
     * @return int
     */
    public final int getLength()
    {
        if (m_endpos != 0)
            return m_endpos - m_offset;
        return m_pos - m_offset;
    }

    /**
     * Return the data length in words
     * 
     * @return int
     */
    public final int getLengthInWords()
    {
        return getLength() / 2;
    }

    /**
     * Return the available data length
     * 
     * @return int
     */
    public final int getAvailableLength()
    {
        if (m_endpos == 0)
            return -1;
        return m_endpos - m_pos;
    }

    /**
     * Return the displacement from the start of the buffer to the current buffer position
     * 
     * @return int
     */
    public final int getDisplacement()
    {
        return m_pos - m_offset;
    }

    /**
     * Return the buffer base offset
     * 
     * @return int
     */
    public final int getOffset()
    {
        return m_offset;
    }

    /**
     * Get a byte from the buffer
     * 
     * @return int
     */
    public final int getByte()
    {

        // Check if there is enough data in the buffer

        if (m_data.length - m_pos < 1)
            throw new ArrayIndexOutOfBoundsException("End of data buffer");

        // Unpack the byte value

        int bval = (int) (m_data[m_pos] & 0xFF);
        m_pos++;
        return bval;
    }

    /**
     * Get a short from the buffer
     * 
     * @return int
     */
    public final int getShort()
    {

        // Check if there is enough data in the buffer

        if (m_data.length - m_pos < 2)
            throw new ArrayIndexOutOfBoundsException("End of data buffer");

        // Unpack the integer value

        int sval = (int) DataPacker.getIntelShort(m_data, m_pos);
        m_pos += 2;
        return sval;
    }

    /**
     * Get an integer from the buffer
     * 
     * @return int
     */
    public final int getInt()
    {

        // Check if there is enough data in the buffer

        if (m_data.length - m_pos < 4)
            throw new ArrayIndexOutOfBoundsException("End of data buffer");

        // Unpack the integer value

        int ival = DataPacker.getIntelInt(m_data, m_pos);
        m_pos += 4;
        return ival;
    }

    /**
     * Get a long (64 bit) value from the buffer
     * 
     * @return long
     */
    public final long getLong()
    {

        // Check if there is enough data in the buffer

        if (m_data.length - m_pos < 8)
            throw new ArrayIndexOutOfBoundsException("End of data buffer");

        // Unpack the long value

        long lval = DataPacker.getIntelLong(m_data, m_pos);
        m_pos += 8;
        return lval;
    }

    /**
     * Get a string from the buffer
     * 
     * @param uni boolean
     * @return String
     */
    public final String getString(boolean uni)
    {
        return getString(255, uni);
    }

    /**
     * Get a string from the buffer
     * 
     * @param maxlen int
     * @param uni boolean
     * @return String
     */
    public final String getString(int maxlen, boolean uni)
    {

        // Check for Unicode or ASCII

        String ret = null;
        int availLen = -1;

        if (uni)
        {

            // Word align the current buffer position, calculate the available
            // length

            m_pos = DataPacker.wordAlign(m_pos);
            availLen = (m_endpos - m_pos) / 2;
            if (availLen < maxlen)
                maxlen = availLen;

            ret = DataPacker.getUnicodeString(m_data, m_pos, maxlen);
            if (ret != null) {
                if ( ret.length() < maxlen)
                    m_pos += (ret.length() * 2) + 2;
                else
                    m_pos += maxlen * 2;
            }
        }
        else
        {

            // Calculate the available length

            availLen = m_endpos - m_pos;
            if (availLen < maxlen)
                maxlen = availLen;

            // Unpack the ASCII string

            ret = DataPacker.getString(m_data, m_pos, maxlen);
            if (ret != null) {
                if ( ret.length() < maxlen)
                    m_pos += ret.length() + 1;
                else
                    m_pos += maxlen;
            }
        }

        // Return the string

        return ret;
    }

    /**
     * Get a short from the buffer at the specified index
     * 
     * @param idx int
     * @return int
     */
    public final int getShortAt(int idx)
    {

        // Check if there is enough data in the buffer

        int pos = m_offset + (idx * 2);
        if (m_data.length - pos < 2)
            throw new ArrayIndexOutOfBoundsException("End of data buffer");

        // Unpack the integer value

        int sval = (int) DataPacker.getIntelShort(m_data, pos) & 0xFFFF;
        return sval;
    }

    /**
     * Get an integer from the buffer at the specified index
     * 
     * @param idx int
     * @return int
     */
    public final int getIntAt(int idx)
    {

        // Check if there is enough data in the buffer

        int pos = m_offset + (idx * 2);
        if (m_data.length - pos < 4)
            throw new ArrayIndexOutOfBoundsException("End of data buffer");

        // Unpack the integer value

        int ival = DataPacker.getIntelInt(m_data, pos);
        return ival;
    }

    /**
     * Get a long (64 bit) value from the buffer at the specified index
     * 
     * @param idx int
     * @return long
     */
    public final long getLongAt(int idx)
    {

        // Check if there is enough data in the buffer

        int pos = m_offset + (idx * 2);
        if (m_data.length - pos < 8)
            throw new ArrayIndexOutOfBoundsException("End of data buffer");

        // Unpack the long value

        long lval = DataPacker.getIntelLong(m_data, pos);
        return lval;
    }

    /**
     * Skip over a number of bytes
     * 
     * @param cnt int
     */
    public final void skipBytes(int cnt)
    {

        // Check if there is enough data in the buffer

        if (m_data.length - m_pos < cnt)
            throw new ArrayIndexOutOfBoundsException("End of data buffer");

        // Skip bytes

        m_pos += cnt;
    }

    /**
     * Return the data position
     * 
     * @return int
     */
    public final int getPosition()
    {
        return m_pos;
    }

    /**
     * Set the read/write buffer position
     * 
     * @param pos int
     */
    public final void setPosition(int pos)
    {
        m_pos = pos;
    }

    /**
     * Set the end of buffer position, and reset the read position to the beginning of the buffer
     */
    public final void setEndOfBuffer()
    {
        m_endpos = m_pos;
        m_pos = m_offset;
    }

    /**
     * Set the data length
     * 
     * @param len int
     */
    public final void setLength(int len)
    {
        m_pos = m_offset + len;
    }

    /**
     * Append a byte value to the buffer
     * 
     * @param bval int
     */
    public final void putByte(int bval)
    {

        // Check if there is enough space in the buffer

        if (m_data.length - m_pos < 1)
            extendBuffer();

        // Pack the byte value

        m_data[m_pos++] = (byte) (bval & 0xFF);
    }

    /**
     * Append a short value to the buffer
     * 
     * @param sval int
     */
    public final void putShort(int sval)
    {

        // Check if there is enough space in the buffer

        if (m_data.length - m_pos < 2)
            extendBuffer();

        // Pack the short value

        DataPacker.putIntelShort(sval, m_data, m_pos);
        m_pos += 2;
    }

    /**
     * Append an integer to the buffer
     * 
     * @param ival int
     */
    public final void putInt(int ival)
    {

        // Check if there is enough space in the buffer

        if (m_data.length - m_pos < 4)
            extendBuffer();

        // Pack the integer value

        DataPacker.putIntelInt(ival, m_data, m_pos);
        m_pos += 4;
    }

    /**
     * Append a long to the buffer
     * 
     * @param lval long
     */
    public final void putLong(long lval)
    {

        // Check if there is enough space in the buffer

        if (m_data.length - m_pos < 8)
            extendBuffer();

        // Pack the long value

        DataPacker.putIntelLong(lval, m_data, m_pos);
        m_pos += 8;
    }

    /**
     * Append a short value to the buffer at the specified index
     * 
     * @param idx int
     * @param sval int
     */
    public final void putShortAt(int idx, int sval)
    {

        // Check if there is enough space in the buffer

        int pos = m_offset + (idx * 2);
        if (m_data.length - pos < 2)
            extendBuffer();

        // Pack the short value

        DataPacker.putIntelShort(sval, m_data, pos);
    }

    /**
     * Append an integer to the buffer at the specified index
     * 
     * @param idx int
     * @param ival int
     */
    public final void putIntAt(int idx, int ival)
    {

        // Check if there is enough space in the buffer

        int pos = m_offset = (idx * 2);
        if (m_data.length - pos < 4)
            extendBuffer();

        // Pack the integer value

        DataPacker.putIntelInt(ival, m_data, pos);
    }

    /**
     * Append a long to the buffer at the specified index
     * 
     * @param idx int
     * @param lval long
     */
    public final void putLongAt(int idx, int lval)
    {

        // Check if there is enough space in the buffer

        int pos = m_offset = (idx * 2);
        if (m_data.length - pos < 8)
            extendBuffer();

        // Pack the long value

        DataPacker.putIntelLong(lval, m_data, pos);
    }

    /**
     * Append a string to the buffer
     * 
     * @param str String
     * @param uni boolean
     */
    public final void putString(String str, boolean uni)
    {
        putString(str, uni, true);
    }

    /**
     * Append a string to the buffer
     * 
     * @param str String
     * @param uni boolean
     * @param nulTerm boolean
     */
    public final void putString(String str, boolean uni, boolean nulTerm)
    {

        // Check for Unicode or ASCII

        if (uni)
        {

            // Check if there is enough space in the buffer

            int bytLen = str.length() * 2;
            if ( nulTerm)
            	bytLen += 2;
            if ((m_data.length - m_pos) < (bytLen + 4))
                extendBuffer(bytLen + 4);

            // Word align the buffer position, pack the Unicode string

            m_pos = DataPacker.wordAlign(m_pos);
            DataPacker.putUnicodeString(str, m_data, m_pos, nulTerm);
            m_pos += (str.length() * 2);
            if (nulTerm)
                m_pos += 2;
        }
        else
        {

            // Check if there is enough space in the buffer

            if (m_data.length - m_pos < str.length())
                extendBuffer(str.length() + 2);

            // Pack the ASCII string

            DataPacker.putString(str, m_data, m_pos, nulTerm);
            m_pos += str.length();
            if (nulTerm)
                m_pos++;
        }
    }

    /**
     * Append a fixed length string to the buffer
     * 
     * @param str String
     * @param len int
     */
    public final void putFixedString(String str, int len)
    {

        // Check if there is enough space in the buffer

        if (m_data.length - m_pos < str.length())
            extendBuffer(str.length() + 2);

        // Pack the ASCII string

        DataPacker.putString(str, len, m_data, m_pos);
        m_pos += len;
    }

    /**
     * Append a string to the buffer at the specified buffer position
     * 
     * @param str String
     * @param pos int
     * @param uni boolean
     * @param nulTerm boolean
     * @return int
     */
    public final int putStringAt(String str, int pos, boolean uni, boolean nulTerm)
    {

        // Check for Unicode or ASCII

        int retPos = -1;

        if (uni)
        {

            // Check if there is enough space in the buffer

            int bytLen = str.length() * 2;
            if (m_data.length - pos < bytLen)
                extendBuffer(bytLen + 4);

            // Word align the buffer position, pack the Unicode string

            pos = DataPacker.wordAlign(pos);
            retPos = DataPacker.putUnicodeString(str, m_data, pos, nulTerm);
        }
        else
        {

            // Check if there is enough space in the buffer

            if (m_data.length - pos < str.length())
                extendBuffer(str.length() + 2);

            // Pack the ASCII string

            retPos = DataPacker.putString(str, m_data, pos, nulTerm);
        }

        // Return the end of string buffer position

        return retPos;
    }

    /**
     * Append a fixed length string to the buffer at the specified position
     * 
     * @param str String
     * @param len int
     * @param pos int
     * @return int
     */
    public final int putFixedStringAt(String str, int len, int pos)
    {

        // Check if there is enough space in the buffer

        if (m_data.length - pos < str.length())
            extendBuffer(str.length() + 2);

        // Pack the ASCII string

        return DataPacker.putString(str, len, m_data, pos);
    }

    /**
     * Append a string pointer to the specified buffer offset
     * 
     * @param off int
     */
    public final void putStringPointer(int off)
    {

        // Calculate the offset from the start of the data buffer to the string
        // position

        DataPacker.putIntelInt(off - m_offset, m_data, m_pos);
        m_pos += 4;
    }

    /**
     * Append zero bytes to the buffer
     * 
     * @param cnt int
     */
    public final void putZeros(int cnt)
    {

        // Check if there is enough space in the buffer

        if (m_data.length - m_pos < cnt)
            extendBuffer(cnt);

        // Pack the zero bytes

        for (int i = 0; i < cnt; i++)
            m_data[m_pos++] = 0;
    }

    /**
     * Word align the buffer position
     */
    public final void wordAlign()
    {
        m_pos = DataPacker.wordAlign(m_pos);
    }

    /**
     * Longword align the buffer position
     */
    public final void longwordAlign()
    {
        m_pos = DataPacker.longwordAlign(m_pos);
    }

    /**
     * Append a raw data block to the data buffer
     * 
     * @param buf byte[]
     * @param off int
     * @param len int
     */
    public final void appendData(byte[] buf, int off, int len)
    {

        // Check if there is enough space in the buffer

        if (m_data.length - m_pos < len)
            extendBuffer(len);

        // Copy the data to the buffer and update the current write position

        System.arraycopy(buf, off, m_data, m_pos, len);
        m_pos += len;
    }

    /**
     * Copy all data from the data buffer to the user buffer, and update the read position
     * 
     * @param buf byte[]
     * @param off int
     * @return int
     */
    public final int copyData(byte[] buf, int off)
    {
        return copyData(buf, off, getLength());
    }

    /**
     * Copy data from the data buffer to the user buffer, and update the current read position.
     * 
     * @param buf byte[]
     * @param off int
     * @param cnt int
     * @return int
     */
    public final int copyData(byte[] buf, int off, int cnt)
    {

        // Check if there is any more data to copy

        if (m_pos == m_endpos)
            return 0;

        // Calculate the amount of data to copy

        int siz = m_endpos - m_pos;
        if (siz > cnt)
            siz = cnt;

        // Copy the data to the user buffer and update the current read position

        System.arraycopy(m_data, m_pos, buf, off, siz);
        m_pos += siz;

        // Return the amount of data copied

        return siz;
    }

    /**
     * Extend the data buffer by the specified amount
     * 
     * @param ext int
     */
    private final void extendBuffer(int ext)
    {

        // Create a new buffer of the required size

        byte[] newBuf = new byte[m_data.length + ext];

        // Copy the data from the current buffer to the new buffer

        System.arraycopy(m_data, 0, newBuf, 0, m_data.length);

        // Set the new buffer to be the main buffer

        m_data = newBuf;
    }

    /**
     * Extend the data buffer, double the currently allocated buffer size
     */
    private final void extendBuffer()
    {
        extendBuffer(m_data.length * 2);
    }

    /**
     * Return the data buffer details as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[data=");
        str.append(m_data);
        str.append(",");
        str.append(m_pos);
        str.append("/");
        str.append(m_offset);
        str.append("/");
        str.append(getLength());
        str.append("]");

        return str.toString();
    }
}
