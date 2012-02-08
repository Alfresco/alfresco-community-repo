/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.util.schemacomp.validator;

import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.ValidationResult;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Schema;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Validate a schema's version number with respect to a reference schema.
 * <p/>
 * For the target schema to pass validation its version number must be
 * greater than or equal to the reference schema's version number.
 * 
 * @author Matt Ward
 */
public class SchemaVersionValidator extends AbstractDbValidator
{
    public SchemaVersionValidator()
    {
        addFieldToValidate("version");
    }
    
    @Override
    public void validate(DbObject referenceObj, DbObject targetObj, DiffContext ctx)
    {
        Schema reference = (Schema) referenceObj;
        Schema target = (Schema) targetObj;
        
        if (target.getVersion() < reference.getVersion())
        {
            DbProperty targetProperty = new DbProperty(target, "version");
            String message = I18NUtil.getMessage(
                        "system.schema_comp.schema_version_validator",
                        reference.getVersion());
            ctx.getComparisonResults().add(new ValidationResult(targetProperty, message));
        }
    }
}
