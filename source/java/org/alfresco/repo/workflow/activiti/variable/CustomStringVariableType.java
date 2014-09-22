/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.workflow.activiti.variable;

import org.activiti.engine.impl.persistence.entity.ByteArrayEntity;
import org.activiti.engine.impl.variable.StringType;
import org.activiti.engine.impl.variable.ValueFields;

/**
 * Custom implementation of the Activiti {@link StringType}, which allows string-values
 * to be larger than the database-restriction of the TEXT_ column, by using binary storage in case
 * the string value exceeds the size.
 * 
 * @author Frederik Heremans
 * @since 4.2
 */
public class CustomStringVariableType extends StringType 
{
    protected static final int MAX_TEXT_LENGTH = 4000;
    
    public CustomStringVariableType()
    {
        super(MAX_TEXT_LENGTH);
        
    }
    
    public CustomStringVariableType(int length)
    {
        super(length);
    }

    @Override
    public void setValue(Object value, ValueFields valueFields) 
    {
        if(value != null && ((String) value).length() > MAX_TEXT_LENGTH) 
        {
            ByteArrayEntity byteArray = valueFields.getByteArrayValue();
            byte[] bytes = ((String) value).getBytes();
            if (byteArray==null) 
            {
                valueFields.setByteArrayValue(bytes);
            } 
            else 
            {
                // Reuse the existing byte-array entity on an update instead of creating a new one each time
                byteArray.setBytes(bytes);
            }
        } 
        else {
            // Make sure NO byte-array is present anymore in case this variable exceeded the 
            // length before this update, but is shorter now
            valueFields.setByteArrayValue(null);
            
            // Revert to storing regular string
            super.setValue(value, valueFields);
        }
    }
    
    @Override
    public Object getValue(ValueFields valueFields) 
    {
        // In case the string is stored as a byte-array, create a string from the stored bytes
        // using platform encoding and return this instead of the text-value
        if(valueFields.getByteArrayValueId() != null && valueFields.getByteArrayValue() != null) {
            return new String(valueFields.getByteArrayValue().getBytes());
        }
        return super.getValue(valueFields);
    }
}
