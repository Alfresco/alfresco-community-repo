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

import org.alfresco.repo.forms.AssociationFieldDefinition;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.AssociationFieldDefinition.Direction;
import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.repo.forms.processor.node.TransientFieldProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.ASSOC_DATA_PREFIX;

/**
 * {@link FieldProcessor} for handling package contents when displaying Workflow and Task Forms.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class PackageItemsFieldProcessor extends TransientFieldProcessor
{
    /** Logger */
    private final static Log logger = LogFactory.getLog(PackageItemsFieldProcessor.class);

    public static final String KEY = "packageItems";

    private static final String MSG_LABEL = "form_service.package.items.label";
    private static final String MSG_DESCRIPTION = "form_service.package.items.description";

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.TransientFieldProcessor#makeTransientPropertyDefinition()
     */
    @Override
    protected FieldDefinition makeTransientFieldDefinition()
    {
        AssociationFieldDefinition fieldDef = new AssociationFieldDefinition(
                    KEY, KEY, Direction.TARGET);
        fieldDef.setLabel(I18NUtil.getMessage(MSG_LABEL));
        fieldDef.setDescription(I18NUtil.getMessage(MSG_DESCRIPTION));
        fieldDef.setProtectedField(false);
        fieldDef.setEndpointMandatory(false);
        fieldDef.setEndpointMany(true);

        // define the data key name and set
        String dataKey = ASSOC_DATA_PREFIX + KEY;
        fieldDef.setDataKeyName(dataKey);
        return fieldDef;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.AbstractFieldProcessor#getLogger()
     */
    @Override
    protected Log getLogger()
    {
        return logger;
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
