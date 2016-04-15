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
 * Support for calendar based "end of" periods with month and day offsets for fiscal year support
 * @author andyh
 *
 */
public abstract class AbstractEndOfCalendarPeriodProvider extends AbstractCalendarPeriodProvider
{
    private int startDayOfMonth = 1;
    
    private int startMonth = Calendar.JANUARY;

    /**
     * Get the start day of the month (as defined by Calendar)
     * @return - the start day of the month
     */
    public int getStartDayOfMonth()
    {
        return startDayOfMonth;
    }

    /**
     * Set the start day of the month (as defined by Calendar)
     * @param startDayOfMonth int
     */
    public void setStartDayOfMonth(int startDayOfMonth)
    {
        this.startDayOfMonth = startDayOfMonth;
    }

    /**
     * Get the start month (as defined by Calendar)
     * @return - the start month
     */
    public int getStartMonth()
    {
        return startMonth;
    }

    /**
     * Set the start month (as defined by Calendar)
     * @param startMonth int
     */
    public void setStartMonth(int startMonth)
    {
        this.startMonth = startMonth;
    }
}
