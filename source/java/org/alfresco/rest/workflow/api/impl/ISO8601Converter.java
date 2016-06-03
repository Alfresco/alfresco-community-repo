/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.workflow.api.impl;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.beanutils.Converter;

public class ISO8601Converter implements Converter
{
    @Override
    @SuppressWarnings("rawtypes")
    public Object convert(Class clazz, Object value)
    {
        Object result = false;
        
        if(value != null)
        {
            if(clazz.equals(Date.class))
            {
            	if(value instanceof Date)
            	{
            		result = value;
            	}
            	else if(value instanceof String)
            	{
            		result = ISO8601DateFormat.parse((String) value);
            	}
            	else if (value instanceof Long) 
            	{
            		Long longObj = (Long)value;
            		result =  new Date(longObj);
            	}
            	else if (value instanceof java.sql.Date) 
            	{
            		java.sql.Date valueX = (java.sql.Date)value;
            		result = new Date(valueX.getTime());        
                }
            	else if (value instanceof java.sql.Time) 
            	{
            		java.sql.Time valueX = (java.sql.Time)value;
            		result = new Date(valueX.getTime());        
                }
            	else if(value instanceof Calendar)
            	{
            		result = ((Calendar)value).getTime();
            	}
            	else
            	{
            		throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
            	}
            }
            else if(clazz.equals(Calendar.class))
            {
            	if(value instanceof Calendar)
            	{
            		result = value;
            	}
            	else if(value instanceof String)
            	{
            		result = Calendar.getInstance();
            		((Calendar) result).setTime(ISO8601DateFormat.parse((String) value));
            	}
            	else if(value instanceof Date)
            	{
            		result = Calendar.getInstance();
            		((Calendar) result).setTime((Date)value);
            	}
            	else if (value instanceof Long) 
            	{
            		result = Calendar.getInstance();
            		Long longObj = (Long)value;
            		((Calendar) result).setTime(new Date(longObj));
            	}
            	else if (value instanceof java.sql.Date) 
            	{
            		result = Calendar.getInstance();
            		java.sql.Date longObj = (java.sql.Date)value;
            		((Calendar) result).setTime(new Date(longObj.getTime()));
                }
            	else if (value instanceof java.sql.Time) 
            	{
            		result = Calendar.getInstance();
            		java.sql.Time longObj = (java.sql.Time)value;
            		((Calendar) result).setTime(new Date(longObj.getTime()));
                }
            	else
            	{
            		throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
            	}
            }
            else 
            {
                throw new IllegalArgumentException("Unsupported type: " + clazz.getName());
            }
        }
        return result;
    }
}
