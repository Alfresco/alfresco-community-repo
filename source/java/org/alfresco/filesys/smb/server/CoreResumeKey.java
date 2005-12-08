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

import java.io.PrintStream;

import org.alfresco.filesys.util.DataPacker;

/**
 * Core protocol search resume key.
 */
class CoreResumeKey
{
    // Resume key offsets/lengths

    private static final int RESBITS = 0;
    private static final int FILENAME = 1;
    private static final int RESSERVER = 12;
    private static final int RESCONSUMER = 17;

    private static final int FILENAMELEN = 11;
    private static final int RESSRVLEN = 5;
    private static final int RESCONSUMLEN = 4;

    public static final int LENGTH = 21;

    /**
     * Dump the resume key to the specified output stream.
     * 
     * @param out java.io.PrintStream
     * @param buf byte[]
     * @param pos int
     */
    public final static void DumpKey(PrintStream out, byte[] buf, int pos)
    {

        // Output the various resume key fields

        out.print("[" + getReservedByte(buf, pos) + ", ");
        out.print(getFileName(buf, pos, false) + "]");
    }

    /**
     * Return the consumer area of the resume key.
     * 
     * @return byte[]
     */
    public static final byte[] getConsumerArea(byte[] buf, int pos)
    {
        byte[] conArea = new byte[RESCONSUMLEN];
        for (int i = 0; i < RESCONSUMLEN; i++)
            conArea[i] = buf[pos + RESCONSUMER + i];
        return conArea;
    }

    /**
     * Return the file name from the resume key.
     * 
     * @return java.lang.String
     */
    public static final String getFileName(byte[] buf, int pos, boolean dot)
    {

        // Check if we should return the file name in 8.3 format

        if (dot)
        {

            // Build the 8.3 file name

            StringBuffer name = new StringBuffer();
            name.append(new String(buf, pos + FILENAMELEN, 8).trim());
            name.append(".");
            name.append(new String(buf, pos + FILENAMELEN + 8, 3).trim());

            return name.toString();
        }

        // Return the raw string

        return new String(buf, pos + FILENAME, FILENAMELEN).trim();
    }

    /**
     * Return the reserved byte from the resume key.
     * 
     * @return byte
     */
    public static final byte getReservedByte(byte[] buf, int pos)
    {
        return buf[pos];
    }

    /**
     * Copy the resume key from the buffer to the user buffer.
     * 
     * @param buf byte[]
     * @param pos int
     * @param key byte[]
     */
    public final static void getResumeKey(byte[] buf, int pos, byte[] key)
    {

        // Copy the resume key bytes

        System.arraycopy(buf, pos, key, 0, LENGTH);
    }

    /**
     * Return the server area resume key value. This is the search context index in our case.
     * 
     * @return int Server resume key value ( search context index).
     */
    public static final int getServerArea(byte[] buf, int pos)
    {
        return DataPacker.getIntelInt(buf, pos + RESSERVER + 1);
    }

    /**
     * Generate a resume key with the specified filename and search context id.
     * 
     * @param buf byte[]
     * @param pos
     * @param fileName java.lang.String
     * @param ctxId int
     */
    public final static void putResumeKey(byte[] buf, int pos, String fileName, int ctxId)
    {

        // Clear the reserved area

        buf[pos + RESBITS] = 0x16;

        // Put the file name in resume key format

        setFileName(buf, pos, fileName);

        // Put the server side reserved area

        setServerArea(buf, pos, ctxId);
        // setServerArea( buf, pos, 0);
    }

    /**
     * Set the consumer reserved area value.
     * 
     * @param conArea byte[]
     */
    public static final void setConsumerArea(byte[] buf, int pos, byte[] conArea)
    {
        for (int i = 0; i < RESCONSUMLEN; i++)
            buf[pos + RESCONSUMER + i] = conArea[i];
    }

    /**
     * Set the resume key file name string.
     * 
     * @param name java.lang.String
     */
    public static final void setFileName(byte[] buf, int pos, String name)
    {

        // Split the file name string

        StringBuffer str = new StringBuffer();
        int dot = name.indexOf(".");
        if (dot != -1)
        {
            str.append(name.substring(0, dot));
            while (str.length() < 8)
                str.append(" ");
            str.append(name.substring(dot + 1, name.length()));
        }
        else
            str.append(name);

        // Space fill the file name to 11 characters

        while (str.length() < FILENAMELEN)
            str.append(" ");

        // Pack the file name string into the resume key

        DataPacker.putString(str.toString(), buf, pos + FILENAME, false);
    }

    /**
     * Set the resume key reserved byte value.
     * 
     * @param param byte
     */
    public static final void setReservedByte(byte[] buf, int pos, byte val)
    {
        buf[pos] = val;
    }

    /**
     * Set the resume key server area value. This is the search context index in our case.
     * 
     * @param srvVal int
     */
    public static final void setServerArea(byte[] buf, int pos, int srvVal)
    {
        buf[pos + RESSERVER] = 1;
        DataPacker.putIntelInt(srvVal, buf, pos + RESSERVER + 1);
    }
}