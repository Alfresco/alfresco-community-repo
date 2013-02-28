/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.action.parameter;

import java.util.Calendar;
import java.util.Locale;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Date parameter processor.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class DateParameterProcessor extends ParameterProcessor
{
    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private static final String SHORT = "short";
    private static final String LONG = "long";
    
    /**
     * @see org.alfresco.repo.action.parameter.ParameterProcessor#process(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String process(String value, NodeRef actionedUponNodeRef)
    {
        // the default position is to return the value un-changed
        String result = value;
        
        // strip the processor name from the value
        value = stripName(value);             
        if (value.isEmpty() == false)
        {
            String[] values = value.split("\\.", 2);            
            Calendar calendar = Calendar.getInstance();      
            int field = getField(values);
            if (Calendar.YEAR == field)
            {
                result = Integer.toString(calendar.get(field));
            }
            else
            {
                result = calendar.getDisplayName(field, getStyle(values), Locale.getDefault());
            }
        }
        
        return result;
    }
    
    private int getField(String[] values)
    {
        int result = 0;
        String field = values[0];
        
        if (MONTH.equals(field) == true)
        {
            result = Calendar.MONTH;
        }
        else if (YEAR.equals(field) == true)
        {
            result = Calendar.YEAR;
        }
        else
        {
            throw new AlfrescoRuntimeException("Date component " + field + " is not supported by parameter substitution.");
        }
        
        return result;
    }
    
    private int getStyle(String[] values)
    {
        int result = Calendar.SHORT;
        
        if (values.length == 2)
        {
            String style = values[1];
            if (LONG.equals(style) == true)
            {
                result = Calendar.LONG;
            }
            else if (SHORT.equals(style) == true)
            {
                result = Calendar.SHORT;
            }
            else
            {
                throw new AlfrescoRuntimeException("Style component " + style + " is not supported by parameter substitution.");
            }
        }
        
        return result;
    }
}
