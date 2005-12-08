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
package org.alfresco.filesys.smb;

import java.util.Calendar;
import java.util.Date;

/**
 * SMB date/time class.
 */
public final class SMBDate extends Date
{
    private static final long serialVersionUID = 3258407335553806902L;

    // Constants
    //
    // Bit masks for extracting the date/time fields from an SMB encoded date/time.
    //

    private static final int Days = 0x001F;
    private static final int Month = 0x01E0;
    private static final int Year = 0xFE00;

    private static final int TwoSeconds = 0x001F;
    private static final int Minutes = 0x07E0;
    private static final int Hours = 0xF800;

    /**
     * Construct the SMBDate using a seconds since 1-Jan-1970 00:00:00 value.
     * 
     * @param secs Seconds since base date/time 1970 value
     */

    public SMBDate(int secs)
    {
        super((long) (secs & 0x7FFFFFFF));
    }

    /**
     * Construct the SMBDate using the SMB encoded date/time values.
     * 
     * @param dat SMB encoded date value
     * @param tim SMB encoded time value
     */

    public SMBDate(int dat, int tim)
    {

        // Extract the date from the SMB encoded value

        int days = dat & Days;
        int months = (dat & Month) >> 5;
        int year = (dat & Year) >> 9;

        // Extract the time from the SMB encoded value

        int secs = (tim & TwoSeconds) * 2;
        int mins = (tim & Minutes) >> 5;
        int hours = (tim & Hours) >> 11;

        // Use a calendar object to create the date/time value

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year + 1980, months - 1, days, hours, mins, secs);

        // Initialize this dates raw value

        this.setTime(cal.getTime().getTime());
    }

    /**
     * Create a new SMBDate using the long time value.
     * 
     * @param dattim long
     */
    public SMBDate(long dattim)
    {
        super(dattim);
    }

    /**
     * Return this date as an SMB encoded date.
     * 
     * @return SMB encoded date value.
     */

    public final int asSMBDate()
    {

        // Use a calendar object to get the day, month and year values

        Calendar cal = Calendar.getInstance();
        cal.setTime(this);

        // Build the SMB encoded date value

        int smbDate = cal.get(Calendar.DAY_OF_MONTH);
        smbDate += (cal.get(Calendar.MONTH) + 1) << 5;
        smbDate += (cal.get(Calendar.YEAR) - 1980) << 9;

        // Return the SMB encoded date value

        return smbDate;
    }

    /**
     * Return this time as an SMB encoded time.
     * 
     * @return SMB encoded time value.
     */

    public final int asSMBTime()
    {

        // Use a calendar object to get the hour, minutes and seconds values

        Calendar cal = Calendar.getInstance();
        cal.setTime(this);

        // Build the SMB encoded time value

        int smbTime = cal.get(Calendar.SECOND) / 2;
        smbTime += cal.get(Calendar.MINUTE) << 5;
        smbTime += cal.get(Calendar.HOUR_OF_DAY) << 11;

        // Return the SMB encoded time value

        return smbTime;
    }
}