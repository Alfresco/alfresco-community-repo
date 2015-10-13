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
