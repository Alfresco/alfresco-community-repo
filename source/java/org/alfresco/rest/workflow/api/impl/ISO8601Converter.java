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
                result =  ISO8601DateFormat.parse((String) value);
            }
            else if(clazz.equals(Calendar.class))
            {
                result = Calendar.getInstance();
                ((Calendar) result).setTime(ISO8601DateFormat.parse((String) value));
            }
            else 
            {
                throw new IllegalArgumentException("Unsupported type: " + clazz.getName());
            }
        }
        return result;
    }
}
