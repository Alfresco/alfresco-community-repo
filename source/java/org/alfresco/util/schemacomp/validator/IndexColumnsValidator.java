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

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.ValidationResult;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Index;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;


/**
 * Validates columns names in a Index using a regular expression pattern.
 * 
 * @author pavel.yurkevich
 */
public class IndexColumnsValidator extends NameValidator
{
    private final static Log log = LogFactory.getLog(IndexColumnsValidator.class);

    @Override
    public void validate(DbObject reference, DbObject target, DiffContext ctx)
    {
        if (!(target instanceof Index))
        {
            throw new AlfrescoRuntimeException("IndexColumnsValidator could be used only in context of index object but was: " + target.toString());
        }

        List<String> referenceColumnNames = ((Index)reference).getColumnNames();
        List<String> targetColumnNames = ((Index)target).getColumnNames();

        for (int i = 0; i < targetColumnNames.size(); i++)
        {
            String columnName = targetColumnNames.get(i);

            if (getPattern() != null && !getPattern().matcher(columnName).matches())
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Pattern [" + getPattern() + "] not matched.");
                }
                String message = I18NUtil.getMessage("system.schema_comp.name_validator", getPattern());
                ValidationResult result = new ValidationResult(new DbProperty(target, "columnNames", i), message);
                ctx.getComparisonResults().add(result);
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Pattern [" + getPattern() + "] matched OK.");
                }
            }
        }
        
        if (targetColumnNames.size() != referenceColumnNames.size())
        {
            if (log.isDebugEnabled())
            {
                log.debug("Number of columns in index " + target.getName() + "doesn't match expected result");
            }
            String message = I18NUtil.getMessage("system.schema_comp.index_columns_validator", targetColumnNames.size(), referenceColumnNames.size());
            ValidationResult result = new ValidationResult(new DbProperty(target, "columnNames"), message);
            ctx.getComparisonResults().add(result);
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Number of columns is equivalent.");
            }
        }
    }
    
    @Override
    public boolean validates(String fieldName)
    {
        ParameterCheck.mandatoryString("fieldName", fieldName);
        return (fieldName.equals("columnNames"));
    }
}
