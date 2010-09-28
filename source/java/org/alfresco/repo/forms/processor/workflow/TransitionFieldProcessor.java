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

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP_DATA_PREFIX;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.repo.forms.processor.node.TransientFieldProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * {@link FieldProcessor} for handling the transitions of a workflow task.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class TransitionFieldProcessor extends TransientFieldProcessor
{
    public static final String KEY = "transitions";
    public static final String DATA_TYPE = KEY;

    private static final String MSG_LABEL = "form_service.transitions.label";
    private static final String MSG_DESCRIPTION = "form_service.transitions.description";

    private static final Log LOGGER = LogFactory.getLog(TransitionFieldProcessor.class);
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.TransientFieldProcessor#makeTransientPropertyDefinition()
     */
    @Override
    protected FieldDefinition makeTransientFieldDefinition()
    {
        PropertyFieldDefinition fieldDef = new PropertyFieldDefinition(KEY, DATA_TYPE);
        fieldDef.setRepeating(false);
        fieldDef.setProtectedField(true);
        
        fieldDef.setLabel(I18NUtil.getMessage(MSG_LABEL));
        fieldDef.setDescription(I18NUtil.getMessage(MSG_DESCRIPTION));
        fieldDef.setDataKeyName(PROP_DATA_PREFIX + KEY);
        return fieldDef;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.AbstractFieldProcessor#getLogger()
     */
    @Override
    protected Log getLogger()
    {
        return LOGGER;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.AbstractFieldProcessor#getRegistryKey()
     */
    @Override
    protected String getRegistryKey()
    {
        return KEY;
    }

}
