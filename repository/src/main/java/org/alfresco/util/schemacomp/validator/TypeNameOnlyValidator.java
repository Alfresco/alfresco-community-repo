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
package org.alfresco.util.schemacomp.validator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.ValidationResult;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Column;
import org.springframework.extensions.surf.util.I18NUtil;


/**
 * Validates columns types without column size.
 * 
 * @author sergei.shcherbovich
 */
public class TypeNameOnlyValidator extends NameValidator
{
    private static String TYPE_SIZE_SPLITTER = "(";

    @Override
    public void validate(DbObject reference, DbObject target, DiffContext ctx)
    {
        if (!(target instanceof Column))
        {
            throw new AlfrescoRuntimeException("TypeNameOnlyValidator could be used only in context of column object but was: " + target.toString());
        }
        
        String referenceTypeName = ((Column)reference).getType();
        String targetTypeName = ((Column)target).getType();
        
        if (referenceTypeName.contains(TYPE_SIZE_SPLITTER))
        {
            referenceTypeName = referenceTypeName.substring(0, referenceTypeName.indexOf(TYPE_SIZE_SPLITTER));
        }
        if (targetTypeName.contains(TYPE_SIZE_SPLITTER))
        {
            targetTypeName = targetTypeName.substring(0, targetTypeName.indexOf(TYPE_SIZE_SPLITTER));
        }
        
        if (!referenceTypeName.equals(targetTypeName))
        {
            String message = I18NUtil.getMessage("system.schema_comp.column_names_validator", targetTypeName, referenceTypeName);
            ValidationResult result = new ValidationResult(new DbProperty(target, "type"), message);
            ctx.getComparisonResults().add(result);
        }
    }
    
    @Override
    public boolean validatesFullObject()
    {
        return true;
    }
    
}
