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

import java.util.Date;

/**
 * Hibernate date dimension for audit roll ups
 * 
 * @author Andy Hind
 */
public interface AuditDate
{
    /**
     * @return      the date object
     */
    public abstract Date getDate();

    /**
     * @return      the day of the year
     */
    public abstract int getDayOfYear();

    /**
     * @return      the day of the month
     */
    public abstract int getDayOfMonth();

    /**
     * @return      the day of the week
     */
    public abstract int getDayOfWeek();

    /**
     * @return      the half year
     */
    public abstract int getHalfYear();

    
    /**
     * @return      the surrogate key
     */
    public abstract Long getId();

    /**
     * @return      the month of the year
     */
    public abstract int getMonth();

    /**
     * @return      the quarter in the year
     */
    public abstract int getQuarter();

    /**
     * @return      the week of the month
     */
    public abstract int getWeekOfMonth();

    /**
     * @return      the week of the year
     */
    public abstract int getWeekOfYear();

    /** 
     * @return      the full year
     */
    public abstract int getFullYear();
}