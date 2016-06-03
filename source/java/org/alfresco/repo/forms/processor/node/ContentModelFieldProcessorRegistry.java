/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.forms.processor.node;

import org.alfresco.repo.forms.processor.FieldProcessorRegistry;

/**
 * FieldProcessorRegistry that exclusively handles content model based field processors.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class ContentModelFieldProcessorRegistry extends FieldProcessorRegistry
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FieldProcessorRegistry#getKey(java.lang.String)
     */
    @Override
    protected String getKey(String fieldName)
    {
        String[] parts = fieldName.split(FormFieldConstants.FIELD_NAME_SEPARATOR);
        if (parts.length > 0)
        {
            return parts[0];
        }
        else 
        {
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FieldProcessorRegistry#useDefaultProcessor(java.lang.String)
     */
    @Override
    protected boolean useDefaultProcessor(String fieldName)
    {
        // Only use default if the fieldName follows the format
        // prefix:localname
        String[] parts = fieldName.split(FormFieldConstants.FIELD_NAME_SEPARATOR);
        return parts.length == 2;
    }
}
