/*
 * Copyright (C) 2005 Alfresco, Inc.
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
     * Get the date 
     * 
     * @return
     */
    public abstract Date getDate();

    /**
     * Get the day of the year.
     * 
     * @return
     */
    public abstract int getDayOfYear();

    /**
     * Get the day of the month.
     * 
     * @return
     */
    public abstract int getDayOfMonth();

    /**
     * Get the day of the week
     * 
     * @return
     */
    public abstract int getDayOfWeek();

    /**
     * Get the half year;
     * 
     * @return
     */
    public abstract int getHalfYear();

    
    /**
     * Get the surrogate key
     * 
     * @return
     */
    public abstract long getId();

    /**
     * Get the month
     * 
     * @return
     */
    public abstract int getMonth();

    /**
     * Get the quarter
     * 
     * @return
     */
    public abstract int getQuarter();

    /**
     * Get the week of the month. 
     * 
     * @return
     */
    public abstract int getWeekOfMonth();

    /**
     * Get the week of the year.
     * 
     * @return
     */
    public abstract int getWeekOfYear();

    /** 
     * Get the year.
     * @return
     */
    public abstract int getYear();

}