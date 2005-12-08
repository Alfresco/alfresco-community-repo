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

import java.io.PrintStream;

/**
 * Hex dump class.
 */
public final class HexDump
{

    /**
     * Hex dump a byte array
     * 
     * @param byt Byte array to dump
     * @param len Length of data to dump
     * @param offset Offset to start data dump
     */

    public static final void Dump(byte[] byt, int len, int offset)
    {
        Dump(byt, len, offset, System.out);
    }

    /**
     * Hex dump a byte array
     * 
     * @param byt Byte array to dump
     * @param len Length of data to dump
     * @param offset Offset to start data dump
     * @param stream Output stream to dump the output to.
     */

    public static final void Dump(byte[] byt, int len, int offset, PrintStream stream)
    {

        // Create buffers for the ASCII and Hex output

        StringBuffer ascBuf = new StringBuffer();
        StringBuffer hexBuf = new StringBuffer();

        // Dump 16 byte blocks from the array until the length has been
        // reached

        int dlen = 0;
        int doff = offset;
        String posStr = null;

        while (dlen < len)
        {

            // Reset the ASCII/Hex buffers

            ascBuf.setLength(0);
            hexBuf.setLength(0);

            posStr = generatePositionString(doff);

            // Dump a block of data, update the data offset

            doff = generateLine(byt, doff, ascBuf, hexBuf);

            // Output the current record

            stream.print(posStr);
            stream.print(hexBuf.toString());
            stream.println(ascBuf.toString());

            // Update the dump length

            dlen += 16;
        }
    }

    /**
     * Generate a hex string for the specified string
     * 
     * @param str String
     * @return String
     */
    public static final String hexString(String str)
    {
        if (str != null)
            return hexString(str.getBytes());
        return "";
    }

    /**
     * Generate a hex string for the specified string
     * 
     * @param str String
     * @param gap String
     * @return String
     */
    public static final String hexString(String str, String gap)
    {
        if (str != null)
            return hexString(str.getBytes(), gap);
        return "";
    }

    /**
     * Generate a hex string for the specified bytes
     * 
     * @param buf byte[]
     * @return String
     */
    public static final String hexString(byte[] buf)
    {
        return hexString(buf, buf.length, null);
    }

    /**
     * Generate a hex string for the specified bytes
     * 
     * @param buf byte[]
     * @param gap String
     * @return String
     */
    public static final String hexString(byte[] buf, String gap)
    {
        return hexString(buf, buf.length, gap);
    }

    /**
     * Generate a hex string for the specified bytes
     * 
     * @param buf byte[]
     * @param len int
     * @param gap String
     * @return String
     */
    public static final String hexString(byte[] buf, int len, String gap)
    {

        // Check if the buffer is valid

        if (buf == null)
            return "";

        // Create a string buffer for the hex string

        int buflen = buf.length * 2;
        if (gap != null)
            buflen += buf.length * gap.length();

        StringBuffer hex = new StringBuffer(buflen);

        // Convert the bytes to hex-ASCII

        for (int i = 0; i < len; i++)
        {

            // Get the current byte

            int curbyt = (int) (buf[i] & 0x00FF);

            // Output the hex string

            hex.append(Integer.toHexString((curbyt & 0xF0) >> 4));
            hex.append(Integer.toHexString(curbyt & 0x0F));

            // Add the gap string, if specified

            if (gap != null && i < (len - 1))
                hex.append(gap);
        }

        // Return the hex-ASCII string

        return hex.toString();
    }

    /**
     * Generate a buffer position string
     * 
     * @param off int
     * @return String
     */
    private static final String generatePositionString(int off)
    {

        // Create a buffer position string

        StringBuffer posStr = new StringBuffer("" + off + " - ");
        while (posStr.length() < 8)
            posStr.insert(0, " ");

        // Return the string

        return posStr.toString();
    }

    /**
     * Output a single line of the hex dump to a debug device
     * 
     * @param byt Byte array to dump
     * @param off Offset to start data dump
     * @param ascBuf Buffer for ASCII output
     * @param hexBuf Buffer for Hex output
     * @return New offset value
     */

    private static final int generateLine(byte[] byt, int off, StringBuffer ascBuf, StringBuffer hexBuf)
    {

        // Check if there is enough buffer space to dump 16 bytes

        int dumplen = byt.length - off;
        if (dumplen > 16)
            dumplen = 16;

        // Dump a 16 byte block of data

        for (int i = 0; i < dumplen; i++)
        {

            // Get the current byte

            int curbyt = (int) (byt[off++] & 0x00FF);

            // Output the hex string

            hexBuf.append(Integer.toHexString((curbyt & 0xF0) >> 4));
            hexBuf.append(Integer.toHexString(curbyt & 0x0F));
            hexBuf.append(" ");

            // Output the character equivalent, if printable

            if (Character.isLetterOrDigit((char) curbyt) || Character.getType((char) curbyt) != Character.CONTROL)
                ascBuf.append((char) curbyt);
            else
                ascBuf.append(".");
        }

        // Output the hex dump line

        hexBuf.append("  - ");

        // Return the new data offset

        return off;
    }
}