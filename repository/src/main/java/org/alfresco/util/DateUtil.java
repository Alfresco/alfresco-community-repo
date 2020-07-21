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

package org.alfresco.util;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * A utility class for working with dates.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class DateUtil
{
    private DateUtil()
    {
        //Private constructor to suppress default constructor for non-instantiability
    }

    /**
     * Calculate the number of days between start and end dates based on the <b>default</b> timezone.
     * If the end date is before the start date, the returned value is negative.
     *
     * @param startMs start date in milliseconds
     * @param endMs   end date in milliseconds
     * @return number days between
     */
    public static int calculateDays(long startMs, long endMs)
    {
        DateTime startDateTime = new DateTime(startMs).withTimeAtStartOfDay();
        DateTime endDateTime = new DateTime(endMs).withTimeAtStartOfDay();

        int days;
        if (endDateTime.isBefore(startDateTime))
        {
            Interval interval = new Interval(endDateTime, startDateTime);
            Period period = interval.toPeriod(PeriodType.days());
            days = 0 - period.getDays();
        }
        else
        {
            Interval interval = new Interval(startDateTime, endDateTime);
            Period period = interval.toPeriod(PeriodType.days());
            days = period.getDays();
        }
        return days;
    }
}
