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
package org.alfresco.repo.dictionary.types.period;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Support for calendar based periods
 * @author andyh
 *
 */
public abstract class AbstractCalendarPeriodProvider extends AbstractPeriodProvider
{

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
        int value = Integer.parseInt(expression);

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
