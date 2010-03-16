/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.domain.propval;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.alfresco.util.Pair;

/**
 * Entity bean for <b>alf_prop_date_value</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyDateValueEntity
{
    /**
     * Converts the given date (with arbitrary time values) to a date-only (no time or milliseconds)
     * 
     * @param value                 the Java date, possibly containing hours, minutes, seconds and milliseconds
     * @return                      the Java date truncated to day-accuracy in GMT
     */
    public static Date truncateDate(java.util.Date value)
    {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(value.getTime());
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);

        Date dayOnlyDate = cal.getTime();
        // Done
        return dayOnlyDate;
    }
    
    private long dateValue;
    private short fullYear;
    private short halfOfYear;
    private short quarterOfYear;
    private short monthOfYear;
    private short weekOfYear;
    private short weekOfMonth;
    private short dayOfYear;
    private short dayOfMonth;
    private short dayOfWeek;
    
    public PropertyDateValueEntity()
    {
    }
    
    @Override
    public int hashCode()
    {
        return (int) dateValue; 
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj != null && obj instanceof PropertyDateValueEntity)
        {
            PropertyDateValueEntity that = (PropertyDateValueEntity) obj;
            return this.dateValue == that.dateValue;
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertyDateValueEntity")
          .append("[ value=").append(dateValue)
          .append(" (").append(ISO8601DateFormat.format(new Date(dateValue))).append(")")
          .append("]");
        return sb.toString();
    }
    
    /**
     * @return          Returns the ID-value pair
     */
    public Pair<Long, Date> getEntityPair()
    {
        return new Pair<Long, Date>(dateValue, new Date(dateValue));
    }
    
    public void setValue(Date value)
    {
        long valueInMs = value.getTime();
        this.dateValue = valueInMs;
        
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(valueInMs);
        
        // We need month_of_year for further calculations
        this.monthOfYear = (short) cal.get(Calendar.MONTH);
        
        this.fullYear = (short) cal.get(Calendar.YEAR);
        this.halfOfYear = (short) monthOfYear < Calendar.JUNE ? (short)0 : (short)1;
        this.quarterOfYear = (short) (monthOfYear / (short)3);
        this.weekOfYear = (short) cal.get(Calendar.WEEK_OF_YEAR);
        this.weekOfMonth = (short) cal.get(Calendar.WEEK_OF_MONTH);
        this.dayOfYear = (short) cal.get(Calendar.DAY_OF_YEAR);
        this.dayOfMonth = (short) cal.get(Calendar.DAY_OF_MONTH);
        this.dayOfWeek = (short) cal.get(Calendar.DAY_OF_MONTH);
    }
    
    public long getDateValue()
    {
        return dateValue;
    }

    public void setDateValue(long dateValue)
    {
        this.dateValue = dateValue;
    }

    public short getFullYear()
    {
        return fullYear;
    }

    public void setFullYear(short fullYear)
    {
        this.fullYear = fullYear;
    }

    public short getHalfOfYear()
    {
        return halfOfYear;
    }

    public void setHalfOfYear(short halfOfYear)
    {
        this.halfOfYear = halfOfYear;
    }

    public short getQuarterOfYear()
    {
        return quarterOfYear;
    }

    public void setQuarterOfYear(short quarterOfYear)
    {
        this.quarterOfYear = quarterOfYear;
    }

    public short getMonthOfYear()
    {
        return monthOfYear;
    }

    public void setMonthOfYear(short monthOfYear)
    {
        this.monthOfYear = monthOfYear;
    }

    public short getWeekOfYear()
    {
        return weekOfYear;
    }

    public void setWeekOfYear(short weekOfYear)
    {
        this.weekOfYear = weekOfYear;
    }

    public short getWeekOfMonth()
    {
        return weekOfMonth;
    }

    public void setWeekOfMonth(short weekOfMonth)
    {
        this.weekOfMonth = weekOfMonth;
    }

    public short getDayOfYear()
    {
        return dayOfYear;
    }

    public void setDayOfYear(short dayOfYear)
    {
        this.dayOfYear = dayOfYear;
    }

    public short getDayOfMonth()
    {
        return dayOfMonth;
    }

    public void setDayOfMonth(short dayOfMonth)
    {
        this.dayOfMonth = dayOfMonth;
    }

    public short getDayOfWeek()
    {
        return dayOfWeek;
    }

    public void setDayOfWeek(short dayOfWeek)
    {
        this.dayOfWeek = dayOfWeek;
    }
}
