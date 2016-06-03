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

import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.processor.AbstractFieldProcessor;
import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.repo.forms.processor.FormCreationData;

/**
 * Abstract base class for all transient {@link FieldProcessor}s.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public abstract class TransientFieldProcessor extends AbstractFieldProcessor<TransientValueGetter>
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.AbstractFieldProcessor#generateTypedField(java.lang.String, org.alfresco.repo.forms.processor.node.FormCreationData, java.lang.Object)
     */
    @Override
    protected Field generateTypedField(String fieldName, FormCreationData formData, TransientValueGetter typedData)
    {
        FieldDefinition transientPropDef = makeTransientFieldDefinition();
        Field fieldInfo = null;
        Object value = getValue(fieldName, typedData);
        if (transientPropDef != null)
        {
            fieldInfo = new ContentModelField(transientPropDef, value);
        }
        return fieldInfo;
    }

    protected Object getValue(String fieldName, TransientValueGetter data)
    {
        return data.getTransientValue(fieldName);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.alfresco.repo.forms.field.processor.AbstractFieldProcessor#
     * getExpectedDataType()
     */
    @Override
    protected Class<TransientValueGetter> getExpectedDataType()
    {
        return TransientValueGetter.class;
    }

    protected abstract FieldDefinition makeTransientFieldDefinition();
}