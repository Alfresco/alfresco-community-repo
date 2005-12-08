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
 * Memory Size Class
 * <p>
 * Convenience class to convert memory size value specified as 'nK' for kilobytes, 'nM' for
 * megabytes and 'nG' for gigabytes, to an absolute value.
 */
public class MemorySize
{
    // Convertor constants

    public static final long KILOBYTE = 1024L;
    public static final long MEGABYTE = 1024L * KILOBYTE;
    public static final long GIGABYTE = 1024L * MEGABYTE;
    public static final long TERABYTE = 1024L * GIGABYTE;

    /**
     * Convert a memory size to an integer byte value.
     * 
     * @param memSize String
     * @return int
     * @exception NumberFormatException
     */
    public static final int getByteValueInt(String memSize)
    {
        return (int) (getByteValue(memSize) & 0xFFFFFFFFL);
    }

    /**
     * Convert a memory size to a byte value
     * 
     * @param memSize String
     * @return long
     * @exception NumberFormatException
     */
    public static final long getByteValue(String memSize)
    {

        // Check if the string is valid

        if (memSize == null || memSize.length() == 0)
            return -1L;

        // Check for a kilobyte value

        String sizeStr = memSize.toUpperCase();
        long mult = 1;
        long val = 0;

        if (sizeStr.endsWith("K"))
        {

            // Use the kilobyte multiplier

            mult = KILOBYTE;
            val = getValue(sizeStr);
        }
        else if (sizeStr.endsWith("M"))
        {

            // Use the megabyte nultiplier

            mult = MEGABYTE;
            val = getValue(sizeStr);
        }
        else if (sizeStr.endsWith("G"))
        {

            // Use the gigabyte multiplier

            mult = GIGABYTE;
            val = getValue(sizeStr);
        }
        else if (sizeStr.endsWith("T"))
        {

            // Use the terabyte multiplier

            mult = TERABYTE;
            val = getValue(sizeStr);
        }
        else
        {

            // Convert a numeric byte value

            val = Long.valueOf(sizeStr).longValue();
        }

        // Apply the multiplier

        return val * mult;
    }

    /**
     * Get the size value from a string and return the numeric value
     * 
     * @param val String
     * @return long
     * @exception NumberFormatException
     */
    private final static long getValue(String val)
    {

        // Strip the trailing size indicator

        String sizStr = val.substring(0, val.length() - 1);
        return Long.valueOf(sizStr).longValue();
    }

    /**
     * Return a byte value as a kilobyte string
     * 
     * @param val long
     * @return String
     */
    public final static String asKilobyteString(long val)
    {

        // Calculate the kilobyte value

        long mbVal = val / KILOBYTE;
        return "" + mbVal + "Kb";
    }

    /**
     * Return a byte value as a megabyte string
     * 
     * @param val long
     * @return String
     */
    public final static String asMegabyteString(long val)
    {

        // Calculate the megabyte value

        long mbVal = val / MEGABYTE;
        return "" + mbVal + "Mb";
    }

    /**
     * Return a byte value as a gigabyte string
     * 
     * @param val long
     * @return String
     */
    public final static String asGigabyteString(long val)
    {

        // Calculate the gigabyte value

        long mbVal = val / GIGABYTE;
        return "" + mbVal + "Gb";
    }

    /**
     * Return a byte value as a terabyte string
     * 
     * @param val long
     * @return String
     */
    public final static String asTerabyteString(long val)
    {

        // Calculate the terabyte value

        long mbVal = val / TERABYTE;
        return "" + mbVal + "Tb";
    }

    /**
     * Return a byte value as a scaled string
     * 
     * @param val long
     * @return String
     */
    public final static String asScaledString(long val)
    {

        // Determine the scaling to apply

        String ret = null;

        if (val < (KILOBYTE * 2L))
            ret = Long.toString(val);
        else if (val < (MEGABYTE * 2L))
            ret = asKilobyteString(val);
        else if (val < (GIGABYTE * 2L))
            ret = asMegabyteString(val);
        else if (val < (TERABYTE * 2L))
            ret = asGigabyteString(val);
        else
            ret = asTerabyteString(val);

        return ret;
    }
}
