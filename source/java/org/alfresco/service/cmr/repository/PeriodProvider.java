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
