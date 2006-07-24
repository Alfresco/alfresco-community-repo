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
package org.alfresco.filesys.ftp;

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

    /**
     * Pack a date string in Unix format The format is 'Mmm dd hh:mm' if the file is less than six
     * months old, else the format is 'Mmm dd yyyy'.
     * 
     * @param buf StringBuffer
     * @param dt Date
     */
    public final static void packUnixDate(StringBuffer buf, Date dt)
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
}
