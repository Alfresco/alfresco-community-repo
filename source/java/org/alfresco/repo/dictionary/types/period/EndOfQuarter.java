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
package org.alfresco.repo.dictionary.types.period;

import java.util.Calendar;

/**
 * End of quarter
 * @author andyh
 *
 */
public class EndOfQuarter extends AbstractEndOfCalendarPeriodProvider
{
    /**
     * 
     */
    public static final String PERIOD_TYPE = "quarterend"; 
    
    @Override
    public void add(Calendar calendar, int value)
    { 
        // Add a milli to nudge roll over given a quarter end date
        if (value > 0)
        {
            calendar.add(Calendar.MILLISECOND, 1);
        }
        
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int monthInYear = month - getStartMonth();
        if(monthInYear < 0)
        {
            monthInYear += 12;
        }
        int residualMonths = monthInYear % 3;
        if(dayOfMonth < getStartDayOfMonth() && (residualMonths == 0))
        {
            calendar.add(Calendar.MONTH, (value-1)*3);
        
        }
        else
        {
            calendar.add(Calendar.MONTH, value*3);
        }
        
        calendar.add(Calendar.MONTH, -residualMonths);
        calendar.set(Calendar.DAY_OF_MONTH, getStartDayOfMonth());
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        
        // Set the time one minute to midnight
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59); 
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    public String getPeriodType()
    {
       return PERIOD_TYPE;
    }

}
