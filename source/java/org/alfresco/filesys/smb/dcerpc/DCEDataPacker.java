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
