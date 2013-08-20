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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.processor.FieldProcessorRegistry;
import org.alfresco.repo.forms.processor.FormCreationData;
import org.alfresco.repo.forms.processor.node.ContentModelItemData;
import org.alfresco.repo.forms.processor.node.DefaultFieldBuilder;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
/**
 * A custom field-builder, which escapes multi-valued String-properties. The comma's in
 * the values are escaped using a '\' character. When the escape-chatacter is also used in
 * the value, it's escaped as '\\'.
 * </p>
 * @author Frederik Heremans
 */
public class ExtendedFieldBuilder extends DefaultFieldBuilder
{
    private ContentModelItemData<?> data;
    private ExtendedPropertyFieldProcessor extendedPropertyFieldProcessor;
    
    public ExtendedFieldBuilder(FormCreationData data, FieldProcessorRegistry registry,
                NamespaceService namespaceService, List<String> ignoredFields, ExtendedPropertyFieldProcessor extendedPropertyFieldProcessor)
    {
        super(data, registry, namespaceService, ignoredFields);
        this.data = (ContentModelItemData<?>) data.getItemData();
        this.extendedPropertyFieldProcessor = extendedPropertyFieldProcessor;
    }
    
    @Override
    public List<Field> buildDefaultPropertyFields()
    {
        return super.buildDefaultPropertyFields();
    }
    
    @Override
    public Field buildPropertyField(QName name)
    {
        if(extendedPropertyFieldProcessor.isApplicableForProperty(name)) 
        {
           return extendedPropertyFieldProcessor.generateField(name, data, false);
        }
        
        // Revert to "normal" field-building
        return super.buildPropertyField(name);
    }
    
   
    /**
     * @param escapedString the string containing the escaped, comma-seperated values.
     * @return the values split up and unescaped.
     */
    public static List<String> getUnescapedValues(String escapedString)
    {
        List<String> elements = new ArrayList<String>();
        StringBuffer currentElement = new StringBuffer();
        
        char currentChar;
        boolean isEscaped = false;
        for(int i = 0; i < escapedString.length(); i++)
        {
            currentChar = escapedString.charAt(i);
            
            if(isEscaped) 
            {
                isEscaped = false;
                currentElement.append(currentChar);
            }
            else if(currentChar == '\\')
            {
                // Escape character encountered
                isEscaped = true;
            }
            else if(currentChar == ',')
            {
                // New element encounterd
                elements.add(currentElement.toString());
                currentElement.delete(0, currentElement.length());
            }
            else
            {
                // Plain character, push to current value
                currentElement.append(currentChar);
            }
        }
        
        if(currentElement.length() > 0)
        {
            elements.add(currentElement.toString());
        }
        return elements;
    }
}
