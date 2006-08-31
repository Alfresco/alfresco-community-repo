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