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
package org.alfresco.filesys.util;

/**
 * The data packing class is a static class that is used to pack and unpack basic data types to/from
 * network byte order and Intel byte order.
 */
public final class DataPacker
{

    // Flag to indicate the byte order of the platform that we are currently
    // running on.

    private static boolean bigEndian = false;

    /**
     * Return the current endian setting.
     * 
     * @return true if the system is big endian, else false.
     */
    public final static boolean isBigEndian()
    {
        return bigEndian;
    }

    /**
     * Unpack a null terminated data string from the data buffer.
     * 
     * @param typ Data type, as specified by SMBDataType.
     * @param bytarray Byte array to unpack the string value from.
     * @param pos Offset to start unpacking the string value.
     * @param maxlen Maximum length of data to be searched for a null character.
     * @param uni String is Unicode if true, else ASCII
     * @return String, else null if the terminating null character was not found.
     */
    public final static String getDataString(char typ, byte[] bytarray, int pos, int maxlen, boolean uni)
    {

        // Check if the data string has the required data type

        if (bytarray[pos++] == (byte) typ)
        {

            // Extract the null terminated string

            if (uni == true)
                return getUnicodeString(bytarray, wordAlign(pos), maxlen / 2);
            else
                return getString(bytarray, pos, maxlen - 1);
        }

        // Invalid data type

        return null;
    }

    /**
     * Unpack a 32-bit integer.
     * 
     * @param buf Byte buffer containing the integer to be unpacked.
     * @param pos Position within the buffer that the integer is stored.
     * @return The unpacked 32-bit integer value.
     * @exception java.lang.IndexOutOfBoundsException If there is not enough data in the buffer.
     */
    public final static int getInt(byte[] buf, int pos) throws java.lang.IndexOutOfBoundsException
    {

        // Check if the byte array is long enough

        if (buf.length < pos + 3)
            throw new java.lang.IndexOutOfBoundsException();

        // Unpack the 32-bit value

        int i1 = (int) buf[pos] & 0xFF;
        int i2 = (int) buf[pos + 1] & 0xFF;
        int i3 = (int) buf[pos + 2] & 0xFF;
        int i4 = (int) buf[pos + 3] & 0xFF;

        int iVal = (i1 << 24) + (i2 << 16) + (i3 << 8) + i4;

        // Return the unpacked value

        return iVal;
    }

    /**
     * Unpack a 32-bit integer that is stored in Intel format.
     * 
     * @param bytarray Byte array containing the Intel integer to be unpacked.
     * @param pos Offset that the Intel integer is stored within the byte array.
     * @return Unpacked integer value.
     * @exception java.lang.IndexOutOfBoundsException If there is not enough data in the buffer.
     */
    public final static int getIntelInt(byte[] bytarray, int pos) throws java.lang.IndexOutOfBoundsException
    {

        // Check if the byte array is long enough to restore the int

        if (bytarray.length < pos + 3)
            throw new java.lang.IndexOutOfBoundsException();

        // Determine the byte ordering for this platform, and restore the int

        int iVal = 0;

        // Restore the int value from the byte array

        int i1 = (int) bytarray[pos + 3] & 0xFF;
        int i2 = (int) bytarray[pos + 2] & 0xFF;
        int i3 = (int) bytarray[pos + 1] & 0xFF;
        int i4 = (int) bytarray[pos] & 0xFF;

        iVal = (i1 << 24) + (i2 << 16) + (i3 << 8) + i4;

        // Return the int value

        return iVal;
    }

    /**
     * Unpack a 64-bit long.
     * 
     * @param buf Byte buffer containing the integer to be unpacked.
     * @param pos Position within the buffer that the integer is stored.
     * @return The unpacked 64-bit long value.
     * @exception java.lang.IndexOutOfBoundsException If there is not enough data in the buffer.
     */
    public final static long getLong(byte[] buf, int pos) throws java.lang.IndexOutOfBoundsException
    {

        // Check if the byte array is long enough to restore the long

        if (buf.length < pos + 7)
            throw new java.lang.IndexOutOfBoundsException();

        // Restore the long value from the byte array

        long lVal = 0L;

        for (int i = 0; i < 8; i++)
        {

            // Get the current byte, shift the value and add to the return value

            long curVal = (long) buf[pos + i] & 0xFF;
            curVal = curVal << ((7 - i) * 8);
            lVal += curVal;
        }

        // Return the long value

        return lVal;
    }

    /**
     * Unpack a 64-bit integer that is stored in Intel format.
     * 
     * @param bytarray Byte array containing the Intel long to be unpacked.
     * @param pos Offset that the Intel integer is stored within the byte array.
     * @return Unpacked long value.
     * @exception java.lang.IndexOutOfBoundsException If there is not enough data in the buffer.
     */
    public final static long getIntelLong(byte[] bytarray, int pos) throws java.lang.IndexOutOfBoundsException
    {

        // Check if the byte array is long enough to restore the long

        if (bytarray.length < pos + 7)
            throw new java.lang.IndexOutOfBoundsException();

        // Restore the long value from the byte array

        long lVal = 0L;

        for (int i = 0; i < 8; i++)
        {

            // Get the current byte, shift the value and add to the return value

            long curVal = (long) bytarray[pos + i] & 0xFF;
            curVal = curVal << (i * 8);
            lVal += curVal;
        }

        // Return the long value

        return lVal;
    }

    /**
     * Unpack a 16-bit value that is stored in Intel format.
     * 
     * @param bytarray Byte array containing the short value to be unpacked.
     * @param pos Offset to start unpacking the short value.
     * @return Unpacked short value.
     * @exception java.lang.IndexOutOfBoiundsException If there is not enough data in the buffer.
     */
    public final static int getIntelShort(byte[] bytarray, int pos) throws java.lang.IndexOutOfBoundsException
    {

        // Check if the byte array is long enough to restore the int

        if (bytarray.length < pos)
            throw new java.lang.IndexOutOfBoundsException();

        // Restore the short value from the byte array

        int sVal = (((int) bytarray[pos + 1] << 8) + ((int) bytarray[pos] & 0xFF));

        // Return the short value

        return sVal & 0xFFFF;
    }

    /**
     * Unpack a 16-bit value.
     * 
     * @param bytarray Byte array containing the short to be unpacked.
     * @param pos Offset within the byte array that the short is stored.
     * @return Unpacked short value.
     * @exception java.lang.IndexOutOfBoundsException If there is not enough data in the buffer.
     */
    public final static int getShort(byte[] bytarray, int pos) throws java.lang.IndexOutOfBoundsException
    {

        // Check if the byte array is long enough to restore the int

        if (bytarray.length < pos)
            throw new java.lang.IndexOutOfBoundsException();

        // Determine the byte ordering for this platform, and restore the short

        int sVal = 0;

        if (bigEndian == true)
        {

            // Big endian

            sVal = ((((int) bytarray[pos + 1]) << 8) + ((int) bytarray[pos] & 0xFF));
        }
        else
        {

            // Little endian

            sVal = ((((int) bytarray[pos]) << 8) + ((int) bytarray[pos + 1] & 0xFF));
        }

        // Return the short value

        return sVal & 0xFFFF;
    }

    /**
     * Unpack a null terminated string from the data buffer.
     * 
     * @param bytarray Byte array to unpack the string value from.
     * @param pos Offset to start unpacking the string value.
     * @param maxlen Maximum length of data to be searched for a null character.
     * @return String, else null if the terminating null character was not found.
     */
    public final static String getString(byte[] bytarray, int pos, int maxlen)
    {

        // Search for the trailing null

        int maxpos = pos + maxlen;
        int endpos = pos;

        while (bytarray[endpos] != 0x00 && endpos < maxpos)
            endpos++;

        // Check if we reached the end of the buffer

        if (endpos <= maxpos)
            return new String(bytarray, pos, endpos - pos);
        return null;
    }

    /**
     * Unpack a null terminated string from the data buffer. The string may be ASCII or Unicode.
     * 
     * @param bytarray Byte array to unpack the string value from.
     * @param pos Offset to start unpacking the string value.
     * @param maxlen Maximum length of data to be searched for a null character.
     * @param isUni Unicode string if true, else ASCII string
     * @return String, else null if the terminating null character was not found.
     */
    public final static String getString(byte[] bytarray, int pos, int maxlen, boolean isUni)
    {

        // Get a string from the buffer

        String str = null;

        if (isUni)
            str = getUnicodeString(bytarray, pos, maxlen);
        else
            str = getString(bytarray, pos, maxlen);

        // return the string

        return str;
    }

    /**
     * Unpack a null terminated Unicode string from the data buffer.
     * 
     * @param byt Byte array to unpack the string value from.
     * @param pos Offset to start unpacking the string value.
     * @param maxlen Maximum length of data to be searched for a null character.
     * @return String, else null if the terminating null character was not found.
     */
    public final static String getUnicodeString(byte[] byt, int pos, int maxlen)
    {

        // Check for an empty string

        if (maxlen == 0)
            return "";

        // Search for the trailing null

        int maxpos = pos + (maxlen * 2);
        int endpos = pos;
        char[] chars = new char[maxlen];
        int cpos = 0;
        char curChar;

        do
        {

            // Get a Unicode character from the buffer

            curChar = (char) (((byt[endpos + 1] & 0xFF) << 8) + (byt[endpos] & 0xFF));

            // Add the character to the array

            chars[cpos++] = curChar;

            // Update the buffer pointer

            endpos += 2;

        } while (curChar != 0 && endpos < maxpos);

        // Check if we reached the end of the buffer

        if (endpos <= maxpos)
        {
            if (curChar == 0)
                cpos--;
            return new String(chars, 0, cpos);
        }
        return null;
    }

    /**
     * Pack a 32-bit integer into the supplied byte buffer.
     * 
     * @param val Integer value to be packed.
     * @param bytarray Byte buffer to pack the integer value into.
     * @param pos Offset to start packing the integer value.
     * @exception java.lang.IndexOutOfBoundsException If the buffer does not have enough space.
     */
    public final static void putInt(int val, byte[] bytarray, int pos) throws java.lang.IndexOutOfBoundsException
    {

        // Check if the byte array is long enough to store the int

        if (bytarray.length < pos + 3)
            throw new java.lang.IndexOutOfBoundsException();

        // Pack the integer value

        bytarray[pos] = (byte) ((val >> 24) & 0xFF);
        bytarray[pos + 1] = (byte) ((val >> 16) & 0xFF);
        bytarray[pos + 2] = (byte) ((val >> 8) & 0xFF);
        bytarray[pos + 3] = (byte) (val & 0xFF);
    }

    /**
     * Pack an 32-bit integer value in Intel format.
     * 
     * @param val Integer value to be packed.
     * @param bytarray Byte array to pack the value into.
     * @param pos Offset to start packing the integer value.
     * @exception java.lang.IndexOutOfBoundsException If there is not enough data in the buffer.
     */
    public final static void putIntelInt(int val, byte[] bytarray, int pos) throws java.lang.IndexOutOfBoundsException
    {

        // Check if the byte array is long enough to store the int

        if (bytarray.length < pos + 3)
            throw new java.lang.IndexOutOfBoundsException();

        // Store the int value in the byte array

        bytarray[pos + 3] = (byte) ((val >> 24) & 0xFF);
        bytarray[pos + 2] = (byte) ((val >> 16) & 0xFF);
        bytarray[pos + 1] = (byte) ((val >> 8) & 0xFF);
        bytarray[pos] = (byte) (val & 0xFF);
    }

    /**
     * Pack a 64-bit integer value into the buffer
     * 
     * @param val Integer value to be packed.
     * @param bytarray Byte array to pack the value into.
     * @param pos Offset to start packing the integer value.
     * @exception java.lang.IndexOutOfBoundsException If there is not enough data in the buffer.
     */
    public final static void putLong(long val, byte[] bytarray, int pos) throws java.lang.IndexOutOfBoundsException
    {

        // Check if the byte array is long enough to store the int

        if (bytarray.length < pos + 7)
            throw new java.lang.IndexOutOfBoundsException();

        // Store the long value in the byte array

        bytarray[pos] = (byte) ((val >> 56) & 0xFF);
        bytarray[pos + 1] = (byte) ((val >> 48) & 0xFF);
        bytarray[pos + 2] = (byte) ((val >> 40) & 0xFF);
        bytarray[pos + 3] = (byte) ((val >> 32) & 0xFF);
        bytarray[pos + 4] = (byte) ((val >> 24) & 0xFF);
        bytarray[pos + 5] = (byte) ((val >> 16) & 0xFF);
        bytarray[pos + 6] = (byte) ((val >> 8) & 0xFF);
        bytarray[pos + 7] = (byte) (val & 0xFF);
    }

    /**
     * Pack a 64-bit integer value in Intel format.
     * 
     * @param val Integer value to be packed.
     * @param bytarray Byte array to pack the value into.
     * @param pos Offset to start packing the integer value.
     * @exception java.lang.IndexOutOfBoundsException If there is not enough data in the buffer.
     */
    public final static void putIntelLong(long val, byte[] bytarray, int pos)
            throws java.lang.IndexOutOfBoundsException
    {

        // Check if the byte array is long enough to store the int

        if (bytarray.length < pos + 7)
            throw new java.lang.IndexOutOfBoundsException();

        // Store the long value in the byte array

        bytarray[pos + 7] = (byte) ((val >> 56) & 0xFF);
        bytarray[pos + 6] = (byte) ((val >> 48) & 0xFF);
        bytarray[pos + 5] = (byte) ((val >> 40) & 0xFF);
        bytarray[pos + 4] = (byte) ((val >> 32) & 0xFF);
        bytarray[pos + 3] = (byte) ((val >> 24) & 0xFF);
        bytarray[pos + 2] = (byte) ((val >> 16) & 0xFF);
        bytarray[pos + 1] = (byte) ((val >> 8) & 0xFF);
        bytarray[pos] = (byte) (val & 0xFF);
    }

    /**
     * Pack a 64-bit integer value in Intel format.
     * 
     * @param val Integer value to be packed.
     * @param bytarray Byte array to pack the value into.
     * @param pos Offset to start packing the integer value.
     * @exception java.lang.IndexOutOfBoundsException If there is not enough data in the buffer.
     */
    public final static void putIntelLong(int val, byte[] bytarray, int pos) throws java.lang.IndexOutOfBoundsException
    {

        // Check if the byte array is long enough to store the int

        if (bytarray.length < pos + 7)
            throw new java.lang.IndexOutOfBoundsException();

        // Store the int value in the byte array

        bytarray[pos + 7] = (byte) 0;
        bytarray[pos + 6] = (byte) 0;
        bytarray[pos + 5] = (byte) 0;
        bytarray[pos + 4] = (byte) 0;
        bytarray[pos + 3] = (byte) ((val >> 24) & 0xFF);
        bytarray[pos + 2] = (byte) ((val >> 16) & 0xFF);
        bytarray[pos + 1] = (byte) ((val >> 8) & 0xFF);
        bytarray[pos] = (byte) (val & 0xFF);
    }

    /**
     * Pack a 16 bit value in Intel byte order.
     * 
     * @param val Short value to be packed.
     * @param bytarray Byte array to pack the short value into.
     * @param pos Offset to start packing the short value.
     * @exception java.lang.IndexOutOfBoundsException If there is not enough data in the buffer.
     */
    public final static void putIntelShort(int val, byte[] bytarray, int pos)
            throws java.lang.IndexOutOfBoundsException
    {

        // Check if the byte array is long enough to store the short

        if (bytarray.length < pos)
            throw new java.lang.IndexOutOfBoundsException();

        // Pack the short value

        bytarray[pos + 1] = (byte) ((val >> 8) & 0xFF);
        bytarray[pos] = (byte) (val & 0xFF);
    }

    /**
     * Pack a 16-bit value into the supplied byte buffer.
     * 
     * @param val Short value to be packed.
     * @param bytarray Byte array to pack the short value into.
     * @param pos Offset to start packing the short value.
     * @exception java.lang.IndexOutOfBoundsException If there is not enough data in the buffer.
     */
    public final static void putShort(int val, byte[] bytarray, int pos) throws java.lang.IndexOutOfBoundsException
    {

        // Check if the byte array is long enough to store the short

        if (bytarray.length < pos)
            throw new java.lang.IndexOutOfBoundsException();

        // Pack the short value

        bytarray[pos] = (byte) ((val >> 8) & 0xFF);
        bytarray[pos + 1] = (byte) (val & 0xFF);
    }

    /**
     * Pack a string into a data buffer
     * 
     * @param str String to be packed into the buffer
     * @param bytarray Byte array to pack the string into
     * @param pos Position to start packing the string
     * @param nullterm true if the string should be null terminated, else false
     * @return The ending buffer position
     */
    public final static int putString(String str, byte[] bytarray, int pos, boolean nullterm)
    {

        // Get the string as a byte array

        byte[] byts = str.getBytes();

        // Pack the data bytes

        int bufpos = pos;

        for (int i = 0; i < byts.length; i++)
            bytarray[bufpos++] = byts[i];

        // Null terminate the string, if required

        if (nullterm == true)
            bytarray[bufpos++] = 0;

        // Return the next free buffer position

        return bufpos;
    }

    /**
     * Pack a string into a data buffer
     * 
     * @param str String to be packed into the buffer
     * @param fldLen Field length, will be space padded if short
     * @param bytarray Byte array to pack the string into
     * @param pos Position to start packing the string
     * @return The ending buffer position
     */
    public final static int putString(String str, int fldLen, byte[] bytarray, int pos)
    {

        // Get the string as a byte array

        byte[] byts = str.getBytes();

        // Pack the data bytes

        int bufpos = pos;
        int idx = 0;

        while (idx < fldLen)
        {
            if (idx < byts.length)
                bytarray[bufpos++] = byts[idx];
            else
                bytarray[bufpos++] = (byte) 0;
            idx++;
        }

        // Return the next free buffer position

        return bufpos;
    }

    /**
     * Pack a string into a data buffer. The string may be ASCII or Unicode.
     * 
     * @param str String to be packed into the buffer
     * @param bytarray Byte array to pack the string into
     * @param pos Position to start packing the string
     * @param nullterm true if the string should be null terminated, else false
     * @param isUni true if the string should be packed as Unicode, false to pack as ASCII
     * @return The ending buffer position
     */
    public final static int putString(String str, byte[] bytarray, int pos, boolean nullterm, boolean isUni)
    {

        // Pack the string

        int newpos = -1;

        if (isUni)
            newpos = putUnicodeString(str, bytarray, pos, nullterm);
        else
            newpos = putString(str, bytarray, pos, nullterm);

        // Return the end of string buffer position

        return newpos;
    }

    /**
     * Pack a Unicode string into a data buffer
     * 
     * @param str String to be packed into the buffer
     * @param bytarray Byte array to pack the string into
     * @param pos Position to start packing the string
     * @param nullterm true if the string should be null terminated, else false
     * @return The ending buffer position
     */
    public final static int putUnicodeString(String str, byte[] bytarray, int pos, boolean nullterm)
    {

        // Pack the data bytes

        int bufpos = pos;

        for (int i = 0; i < str.length(); i++)
        {

            // Get the current character from the string

            char ch = str.charAt(i);

            // Pack the unicode character

            bytarray[bufpos++] = (byte) (ch & 0xFF);
            bytarray[bufpos++] = (byte) ((ch & 0xFF00) >> 8);
        }

        // Null terminate the string, if required

        if (nullterm == true)
        {
            bytarray[bufpos++] = 0;
            bytarray[bufpos++] = 0;
        }

        // Return the next free buffer position

        return bufpos;
    }

    /**
     * Pack nulls into the buffer.
     * 
     * @param buf Buffer to pack data into.
     * @param pos Position to start packing.
     * @param cnt Number of nulls to pack.
     * @exception java.lang.ArrayIndexOutOfBoundsException If the buffer does not have enough space.
     */
    public final static void putZeros(byte[] buf, int pos, int cnt) throws java.lang.ArrayIndexOutOfBoundsException
    {

        // Check if the buffer is big enough

        if (buf.length < (pos + cnt))
            throw new java.lang.ArrayIndexOutOfBoundsException();

        // Pack the nulls

        for (int i = 0; i < cnt; i++)
            buf[pos + i] = 0;
    }

    /**
     * Align a buffer offset on a word boundary
     * 
     * @param pos int
     * @return int
     */
    public final static int wordAlign(int pos)
    {
        return (pos + 1) & 0xFFFFFFFE;
    }

    /**
     * Align a buffer offset on a longword boundary
     * 
     * @param pos int
     * @return int
     */
    public final static int longwordAlign(int pos)
    {
        return (pos + 3) & 0xFFFFFFFC;
    }

    /**
     * Calculate the string length in bytes
     * 
     * @param str String
     * @param uni boolean
     * @param nul boolean
     * @return int
     */
    public final static int getStringLength(String str, boolean uni, boolean nul)
    {

        // Calculate the string length in bytes

        int len = str.length();
        if (nul)
            len += 1;
        if (uni)
            len *= 2;

        return len;
    }

    /**
     * Calculate the new buffer position after the specified string and encoding (ASCII or Unicode)
     * 
     * @param pos int
     * @param str String
     * @param uni boolean
     * @param nul boolean
     * @return int
     */
    public final static int getBufferPosition(int pos, String str, boolean uni, boolean nul)
    {

        // Calculate the new buffer position

        int len = str.length();
        if (nul)
            len += 1;
        if (uni)
            len *= 2;

        return pos + len;
    }
}