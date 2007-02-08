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
package org.alfresco.filesys.ftp;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * FTP Date Utility Class
 * 
 * @author GKSpencer
 */
public class FTPDate
{

    // Constants
    //
    // Six months in ticks

    protected final static long SIX_MONTHS = 183L * 24L * 60L * 60L * 1000L;

    // Month names

    protected final static String[] _months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
            "Nov", "Dec" };

    // Machine listing date/time formatters
    
    protected final static SimpleDateFormat _mlstFormat     = new SimpleDateFormat( "yyyyMMddHHmmss");
    protected final static SimpleDateFormat _mlstFormatLong = new SimpleDateFormat( "yyyyMMddHHmmss.SSS");
    
    /**
     * Pack a date string in Unix format The format is 'Mmm dd hh:mm' if the file is less than six
     * months old, else the format is 'Mmm dd yyyy'.
     * 
     * @param buf StringBuilder
     * @param dt Date
     */
    public final static void packUnixDate(StringBuilder buf, Date dt)
    {

        // Check if the date is valid

        if (dt == null)
        {
            buf.append("------------");
            return;
        }

        // Get the time raw value

        long timeVal = dt.getTime();
        if (timeVal < 0)
        {
            buf.append("------------");
            return;
        }

        // Add the month name and date parts to the string

        Calendar cal = new GregorianCalendar();
        cal.setTime(dt);
        buf.append(_months[cal.get(Calendar.MONTH)]);
        buf.append(" ");

        int dayOfMonth = cal.get(Calendar.DATE);
        if (dayOfMonth < 10)
            buf.append(" ");
        buf.append(dayOfMonth);
        buf.append(" ");

        // If the file is less than six months old we append the file time, else we append the year

        long timeNow = System.currentTimeMillis();
        if (Math.abs(timeNow - timeVal) > SIX_MONTHS)
        {

            // Append the year

            buf.append(cal.get(Calendar.YEAR));
        }
        else
        {

            // Append the file time as hh:mm

            int hr = cal.get(Calendar.HOUR_OF_DAY);
            if (hr < 10)
                buf.append("0");
            buf.append(hr);
            buf.append(":");

            int min = cal.get(Calendar.MINUTE);
            if (min < 10)
                buf.append("0");
            buf.append(min);
        }
    }

    /**
     * Return a machine listing date/time, in the format 'YYYYMMDDHHSS'.
     * 
     * @param dateTime long
     * @return String
     */
    public final static String packMlstDateTime( long dateTime)
    {
    	return _mlstFormat.format( new Date( dateTime));
    }

    /**
     * Return a machine listing date/time, in the format 'YYYYMMDDHHSS.sss'.
     * 
     * @param dateTime long
     * @return String
     */
    public final static String packMlstDateTimeLong( long dateTime)
    {
    	return _mlstFormatLong.format( new Date( dateTime));
    }
}
