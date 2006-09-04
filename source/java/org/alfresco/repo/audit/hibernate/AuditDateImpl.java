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
    private long id;

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
    private int year;

    protected AuditDateImpl()
    {
        super();
    }
    
    public AuditDateImpl(Date date)
    {
        super();
        setDate(date);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.hibernate.AuditDate#getDate()
     */
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
        this.setYear(cal.get(Calendar.YEAR));
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.hibernate.AuditDate#getDayOfYear()
     */
    public int getDayOfYear()
    {
        return dayOfYear;
    }

    protected void setDayOfYear(int dayOfYear)
    {
        this.dayOfYear = dayOfYear;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.hibernate.AuditDate#getDayOfMonth()
     */
    public int getDayOfMonth()
    {
        return dayOfMonth;
    }

    protected void setDayOfMonth(int dayOfMonth)
    {
        this.dayOfMonth = dayOfMonth;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.hibernate.AuditDate#getDayOfWeek()
     */
    public int getDayOfWeek()
    {
        return dayOfWeek;
    }

    protected void setDayOfWeek(int dayOfWeek)
    {
        this.dayOfWeek = dayOfWeek;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.hibernate.AuditDate#getHalfYear()
     */
    public int getHalfYear()
    {
        return halfYear;
    }

    protected void setHalfYear(int halfYear)
    {
        this.halfYear = halfYear;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.hibernate.AuditDate#getId()
     */
    public long getId()
    {
        return id;
    }

    protected void setId(long id)
    {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.hibernate.AuditDate#getMonth()
     */
    public int getMonth()
    {
        return month;
    }

    protected void setMonth(int month)
    {
        this.month = month;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.hibernate.AuditDate#getQuarter()
     */
    public int getQuarter()
    {
        return quarter;
    }

    protected void setQuarter(int quarter)
    {
        this.quarter = quarter;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.hibernate.AuditDate#getWeekOfMonth()
     */
    public int getWeekOfMonth()
    {
        return weekOfMonth;
    }

    protected void setWeekOfMonth(int weekOfMonth)
    {
        this.weekOfMonth = weekOfMonth;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.hibernate.AuditDate#getWeekOfYear()
     */
    public int getWeekOfYear()
    {
        return weekOfYear;
    }

    protected void setWeekOfYear(int weekOfYear)
    {
        this.weekOfYear = weekOfYear;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.hibernate.AuditDate#getYear()
     */
    public int getYear()
    {
        return year;
    }

    protected void setYear(int year)
    {
        this.year = year;
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

    /**
     * Helper method to get the latest audit date
     */
    public static AuditDate getLatestDate(Session session)
    {
        Query query = session.getNamedQuery(HibernateAuditDAO.QUERY_LAST_AUDIT_DATE);
        return (AuditDate) query.uniqueResult();
    }
    
}
