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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.smb;

import java.util.Date;

/**
 * NT 64bit Time Conversion Class
 * <p>
 * Convert an NT 64bit time value to a Java Date value and vice versa.
 */
public class NTTime
{
    // NT time value indicating infinite time

    public static final long InfiniteTime = 0x7FFFFFFFFFFFFFFFL;

    // Time conversion constant, difference between NT 64bit base date of 1-1-1601 00:00:00 and
    // Java base date of 1-1-1970 00:00:00. In 100ns units.

    private static final long TIME_CONVERSION = 116444736000000000L;

    /**
     * Convert a Java Date value to an NT 64bit time
     * 
     * @param jdate Date
     * @return long
     */
    public final static long toNTTime(Date jdate)
    {

        // Add the conversion constant to the Java date raw value, convert the Java milliseconds to
        // 100ns units

        long ntDate = (jdate.getTime() * 10000L) + TIME_CONVERSION;
        return ntDate;
    }

    /**
     * Convert a Java Date value to an NT 64bit time
     * 
     * @param jdate long
     * @return long
     */
    public final static long toNTTime(long jdate)
    {

        // Add the conversion constant to the Java date raw value, convert the Java milliseconds to
        // 100ns units

        long ntDate = (jdate * 10000L) + TIME_CONVERSION;
        return ntDate;
    }

    /**
     * Convert an NT 64bit time value to a Java date value
     * 
     * @param ntDate long
     * @return SMBDate
     */
    public final static SMBDate toSMBDate(long ntDate)
    {

        // Convert the NT 64bit 100ns time value to a Java milliseconds value

        long jDate = (ntDate - TIME_CONVERSION) / 10000L;
        return new SMBDate(jDate);
    }

    /**
     * Convert an NT 64bit time value to a Java date value
     * 
     * @param ntDate long
     * @return long
     */
    public final static long toJavaDate(long ntDate)
    {

        // Convert the NT 64bit 100ns time value to a Java milliseconds value

        long jDate = (ntDate - TIME_CONVERSION) / 10000L;
        return jDate;
    }
}
