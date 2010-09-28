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

package org.alfresco.repo.forms.processor.node;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP_DATA_PREFIX;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * {@link FieldProcessor} implementation representing the <code>size</code> transient field.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class SizeFieldProcessor extends TransientFieldProcessor 
{
    private static final Log logger = LogFactory.getLog(SizeFieldProcessor.class);
    
    public static final String KEY = "size";
    
    private static final String MSG_SIZE_LABEL = "form_service.size.label";
    private static final String MSG_SIZE_DESC = "form_service.size.description";

    @Override
    protected Log getLogger() 
    {
        return logger;
    }

    @Override
    protected FieldDefinition makeTransientFieldDefinition() 
    {
        String dataKeyName = PROP_DATA_PREFIX + KEY;
        PropertyFieldDefinition sizeField = new PropertyFieldDefinition(KEY, 
                    DataTypeDefinition.LONG.getLocalName());
        sizeField.setLabel(I18NUtil.getMessage(MSG_SIZE_LABEL));
        sizeField.setDescription(I18NUtil.getMessage(MSG_SIZE_DESC));
        sizeField.setDataKeyName(dataKeyName);
        sizeField.setProtectedField(true);
        return sizeField;
    }

    @Override
    protected String getRegistryKey() 
    {
        return KEY;
    }
}