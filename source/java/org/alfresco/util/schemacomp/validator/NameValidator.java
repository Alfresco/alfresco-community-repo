/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.ValidationResult;
import org.alfresco.util.schemacomp.model.DbObject;
import org.hibernate.dialect.Dialect;

/**
 * Validates the name of a DbObject using a regular expression. A regular expression
 * can be supplied for each supported {@link Dialect database dialect}. In addition to
 * dialect specific regular expressions, a default may be supplied - comparisons will fall
 * back to the default if no specific dialect is matched.
 * 
 * @author Matt Ward
 */
public class NameValidator implements DbValidator
{
    private Map<Class<? extends Dialect>, Pattern> namePatterns = new HashMap<Class<? extends Dialect>, Pattern>();
    private Pattern defaultNamePattern;
    
    @Override
    public void validate(DbObject dbo, DiffContext ctx)
    {
        String name = dbo.getName();
        
        Pattern pattern = namePatterns.get(ctx.getDialect().getClass());
        
        ValidationResult result = new ValidationResult(name);
        
        if (pattern != null && !pattern.matcher(name).matches())
        {
            ctx.getValidationResults().add(result);
        }
        else if (defaultNamePattern != null && !defaultNamePattern.matcher(name).matches())
        {   
            ctx.getValidationResults().add(result);
        }
    }


    /**
     * Specify the set of mappings of database dialect to acceptable name patterns.
     * 
     * @param namePatterns
     */
    public void setNamePatterns(Map<Class<? extends Dialect>, Pattern> namePatterns)
    {
        this.namePatterns = namePatterns; 
    }
    
    /**
     * If during validation, there is no specific name validation pattern for the supplied {@link Dialect}
     * then the defaultNamePattern property will be used - if not null.
     * <p>
     * If defaultNamePattern is null then a validation failure will be produced.
     * 
     * @param defaultNamePattern
     */
    public void setDefaultNamePattern(Pattern defaultNamePattern)
    {
        this.defaultNamePattern = defaultNamePattern;
    }
}
