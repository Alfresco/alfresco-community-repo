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
package org.alfresco.filesys.smb.dcerpc;

import org.alfresco.filesys.util.DataPacker;

/**
 * DCE Data Packer Class
 */
public class DCEDataPacker
{

    /**
     * Unpack a DCE string from the buffer
     * 
     * @param buf byte[]
     * @param off int
     * @return String
     * @exception java.lang.IndexOutOfBoundsException If there is not enough data in the buffer.
     */
    public final static String getDCEString(byte[] buf, int off) throws IndexOutOfBoundsException
    {

        // Check if the buffer is big enough to hold the String header

        if (buf.length < off + 12)
            throw new IndexOutOfBoundsException();

        // Get the maximum and actual string length

        int maxLen = DataPacker.getIntelInt(buf, off);
        int strLen = DataPacker.getIntelInt(buf, off + 8);

        // Read the Unicode string

        return DataPacker.getUnicodeString(buf, off + 12, strLen);
    }

    /**
     * Pack a DCE string into the buffer
     * 
     * @param buf byte[]
     * @param off int
     * @param str String
     * @param incNul boolean
     * @return int
     */
    public final static int putDCEString(byte[] buf, int off, String str, boolean incNul)
    {

        // Pack the string header

        DataPacker.putIntelInt(str.length() + 1, buf, off);
        DataPacker.putZeros(buf, off + 4, 4);

        if (incNul == false)
            DataPacker.putIntelInt(str.length(), buf, off + 8);
        else
            DataPacker.putIntelInt(str.length() + 1, buf, off + 8);

        // Pack the string

        return DataPacker.putUnicodeString(str, buf, off + 12, incNul);
    }

    /**
     * Align a buffer offset on a longword boundary
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
}
