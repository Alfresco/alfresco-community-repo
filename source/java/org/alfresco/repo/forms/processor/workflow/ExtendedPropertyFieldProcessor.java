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

package org.alfresco.repo.forms.processor.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.forms.processor.node.ContentModelItemData;
import org.alfresco.repo.forms.processor.node.FormFieldConstants;
import org.alfresco.repo.forms.processor.node.PropertyFieldProcessor;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.namespace.QName;
import org.springframework.util.StringUtils;

/**
 * {@link PropertyFieldProcessor} that allows certain properties to have their values escaped,
 * prior to joining them using comma's to use as form-field data.
 *
 * @author Frederik Heremans
 */
public class ExtendedPropertyFieldProcessor extends PropertyFieldProcessor
{
    private Set<QName> escapedPropertyNames = new HashSet<QName>();
    private Set<String> escapedFieldNames = new HashSet<String>();
    
    @Override
    public Object getValue(QName name, ContentModelItemData<?> data)
    {
        Serializable value = data.getPropertyValue(name);
        if (value != null && value instanceof List<?>)
        {
            List<?> list = (List<?>) value;
            if(!list.isEmpty() && list.get(0) instanceof String) 
            {
                List<String> escapedValues = new ArrayList<String>(list.size());
                for(Object listValue : list)
                {
                   escapedValues.add(escape((String)listValue));
                }
                return StringUtils.collectionToCommaDelimitedString(escapedValues);
            }
        }
        return super.getValue(name, data);
    }    
        
    public boolean isApplicableForProperty(QName propName)
    {
        return escapedPropertyNames != null && escapedPropertyNames.contains(propName);
    }
    
    public boolean isApplicableForField(String fieldName)
    {
        return escapedFieldNames != null && escapedFieldNames.contains(fieldName);
    }
    
    public void addEscapedPropertyName(QName name) 
    {
        escapedPropertyNames.add(name);
        escapedFieldNames.add(name.toPrefixString());
    }
    
    protected QName getFullName(String name)
    {
        String[] parts = name.split(FormFieldConstants.FIELD_NAME_SEPARATOR);
        
        if(parts.length == 2)
        {
            String prefix = parts[0];
            String localName = parts[1];
            return QName.createQName(prefix, localName, namespaceService);
        }
        else
        {
            String prefix = parts[1];
            String localName = parts[2];
            return QName.createQName(prefix, localName, namespaceService);
        }
    }
    
    protected String escape(String listValue)
    {
        if(listValue.indexOf('\\') > 0)
        {
            listValue = listValue.replace("\\", "\\\\");
        }
        if(listValue.indexOf(',') > 0)
        {
            listValue = listValue.replace(",", "\\,");
        }
        return listValue;
    }
    
    public static void main(String[] args)
    {
        ExtendedPropertyFieldProcessor processor = new ExtendedPropertyFieldProcessor();
        processor.addEscapedPropertyName(WorkflowModel.PROP_COMMENT);
        
        System.out.println(processor.getFullName("prop:cm:content"));
    }
}
