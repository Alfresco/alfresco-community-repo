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
package org.alfresco.service.cmr.repository;

import java.util.Date;

import org.alfresco.service.namespace.QName;

/**
 * Provider API for period implementations
 * @author andyh
 *
 */
public interface PeriodProvider
{
    /**
     * Period expression multiplicity
     * @author andyh
     *
     */
    public enum ExpressionMutiplicity
    {
        /**
         * There is no expression
         */
        NONE,
        /**
         * An expression is optional
         */
        OPTIONAL,
        /**
         * An expression is mandatory 
         */
        MANDATORY
    }
    
    /**
     * Get the name for the period.
     * @return - period name
     */
    public String getPeriodType();
    
    /**
     * Gets the display label for the period.
     * @return display label
     */
    public String getDisplayLabel();
    
    /**
     * Get the next date - the provided date + period
     * @param date
     * @param expression
     * @return the next date in the period
     */
    public Date getNextDate(Date date, String expression);
    
    /**
     * Is the expression required etc ...
     * @return the multiplicity
     */
    public ExpressionMutiplicity getExpressionMutiplicity();
    
    /**
     * Get the default expression - this could be null
     * @return - the default expression.
     */
    public String getDefaultExpression();
    
    /**
     * Return the Alfresco data type QName to which the string value of the expression will be converted.   
     * @return the alfresco data type or null if an expression is not allowed.
     */
    public QName getExpressionDataType();
}
