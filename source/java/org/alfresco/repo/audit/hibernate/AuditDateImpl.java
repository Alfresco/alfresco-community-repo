/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.audit.hibernate;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.alfresco.util.EqualsHelper;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Hibernate persistence for a date dimension.
 * 
 * @author Andy Hind
 */
public class AuditDateImpl implements AuditDate
{
    /**
     * Surrogate key
     */
    private Long id;

    /**
     * The date
     */
    private Date date;

    /**
     * The day
     */
    private int dayOfYear;

    /**
     * Day of month.
     */
    private int dayOfMonth;

    /**
     * The day of the week
     */
    private int dayOfWeek;

    /**
     * The week in the year
     */
    private int weekOfYear;

    /**
     * The week of the month
     */
    private int weekOfMonth;

    /**
     * The month in the year
     */
    private int month;

    /**
     * The quarter in the year
     */
    private int quarter;

    /**
     * The half year in the year
     */
    private int halfYear;

    /**
     * The year
     */
    private int fullYear;

    protected AuditDateImpl()
    {
        super();
    }
    
    public AuditDateImpl(Date date)
    {
        super();
        setDate(date);
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        this.date = cal.getTime();
        
        this.setDayOfYear(cal.get(Calendar.DAY_OF_YEAR));
        this.setDayOfMonth(cal.get(Calendar.DAY_OF_MONTH));
        this.setDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));
        this.setMonth(cal.get(Calendar.MONTH));
        this.setHalfYear(getMonth() <= Calendar.JUNE ? 0 : 1);
        this.setQuarter((getMonth()/3));
        this.setWeekOfMonth(cal.get(Calendar.WEEK_OF_MONTH));
        this.setWeekOfYear(cal.get(Calendar.WEEK_OF_YEAR));
        this.setFullYear(cal.get(Calendar.YEAR));
    }

    public int getDayOfYear()
    {
        return dayOfYear;
    }

    protected void setDayOfYear(int dayOfYear)
    {
        this.dayOfYear = dayOfYear;
    }

    public int getDayOfMonth()
    {
        return dayOfMonth;
    }

    protected void setDayOfMonth(int dayOfMonth)
    {
        this.dayOfMonth = dayOfMonth;
    }

    public int getDayOfWeek()
    {
        return dayOfWeek;
    }

    protected void setDayOfWeek(int dayOfWeek)
    {
        this.dayOfWeek = dayOfWeek;
    }

    public int getHalfYear()
    {
        return halfYear;
    }

    protected void setHalfYear(int halfYear)
    {
        this.halfYear = halfYear;
    }

    public Long getId()
    {
        return id;
    }

    protected void setId(Long id)
    {
        this.id = id;
    }

    public int getMonth()
    {
        return month;
    }

    protected void setMonth(int month)
    {
        this.month = month;
    }

    public int getQuarter()
    {
        return quarter;
    }

    protected void setQuarter(int quarter)
    {
        this.quarter = quarter;
    }

    public int getWeekOfMonth()
    {
        return weekOfMonth;
    }

    protected void setWeekOfMonth(int weekOfMonth)
    {
        this.weekOfMonth = weekOfMonth;
    }

    public int getWeekOfYear()
    {
        return weekOfYear;
    }

    protected void setWeekOfYear(int weekOfYear)
    {
        this.weekOfYear = weekOfYear;
    }

    public int getFullYear()
    {
        return fullYear;
    }

    protected void setFullYear(int year)
    {
        this.fullYear = year;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof AuditDateImpl))
        {
            return false;
        }
        AuditDateImpl that = (AuditDateImpl)o;
        return EqualsHelper.nullSafeEquals(this.date, that.date);
    }

    @Override
    public int hashCode()
    {
       return this.date.hashCode();
    }
    
}
