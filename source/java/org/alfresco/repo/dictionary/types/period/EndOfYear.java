/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.dictionary.types.period;

import java.util.Calendar;

/**
 * End of year
 * @author andyh
 *
 */
public class EndOfYear extends AbstractEndOfCalendarPeriodProvider
{
    /**
     * 
     */
    public static final String PERIOD_TYPE = "yearend";

    @Override
    public void add(Calendar calendar, int value)
    {
        // Add a milli to nudge roll over given a year end date
        if (value > 0)
        {
            calendar.add(Calendar.MILLISECOND, 1);
        }
        
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        if ((month < getStartMonth()) || ((dayOfMonth < getStartDayOfMonth()) && (month == getStartMonth())))
        {
            calendar.add(Calendar.YEAR, value - 1);
        }
        else
        {
            calendar.add(Calendar.YEAR, value);
        }

        calendar.set(Calendar.MONTH, getStartMonth());
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
