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
package org.alfresco.repo.dictionary.types.period;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Support for calendar based periods
 * @author andyh
 *
 */
public abstract class AbstractCalendarPeriodProvider extends AbstractPeriodProvider
{
    /** Logger */
    private static Log logger = LogFactory.getLog(AbstractCalendarPeriodProvider.class);
    
    public String getDefaultExpression()
    {
        return "1";
    }

    public ExpressionMutiplicity getExpressionMutiplicity()
    {
        return ExpressionMutiplicity.OPTIONAL;
    }

    public Date getNextDate(Date date, String expression)
    {
        int value = 1;
        try
        {
            value = Integer.parseInt(expression);
        }
        catch (NumberFormatException nfe)
        {
            // default to 1 and log warning
            value = 1;
            
            if (logger.isWarnEnabled())
                logger.warn("\"" + expression + "\" is not a valid period expression!");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        add(calendar, value);
        Date next = calendar.getTime();
        return next;
    }
    
    /**
     * Implementation add
     * @param calendar
     * @param value
     */
    public abstract void add(Calendar calendar, int value);

    public QName getExpressionDataType()
    {
        return DataTypeDefinition.INT;
    }
}
