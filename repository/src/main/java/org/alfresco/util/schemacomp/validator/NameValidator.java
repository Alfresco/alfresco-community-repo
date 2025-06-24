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

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.ValidationResult;
import org.alfresco.util.schemacomp.model.DbObject;

/**
 * Validates the name of a DbObject using a regular expression. A regular expression can be supplied for each supported {@link Dialect database dialect}. In addition to dialect specific regular expressions, a default may be supplied - comparisons will fall back to the default if no specific dialect is matched.
 * 
 * @author Matt Ward
 */
public class NameValidator implements DbValidator
{
    private Pattern pattern;
    private final static Log log = LogFactory.getLog(NameValidator.class);

    @Override
    public void validate(DbObject reference, DbObject target, DiffContext ctx)
    {
        String name = target.getName();

        if (log.isDebugEnabled())
        {
            log.debug("Validating: pattern: [" + pattern + "], reference: " + reference + ", target: " + target);
        }
        if (pattern != null && !pattern.matcher(name).matches())
        {
            if (log.isDebugEnabled())
            {
                log.debug("Pattern [" + pattern + "] not matched.");
            }
            String message = I18NUtil.getMessage("system.schema_comp.name_validator", pattern);
            ValidationResult result = new ValidationResult(new DbProperty(target, "name"), message);
            ctx.getComparisonResults().add(result);
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Pattern [" + pattern + "] matched OK.");
            }
        }
    }

    public void setPattern(Pattern pattern)
    {
        this.pattern = pattern;
    }

    public Pattern getPattern()
    {
        return this.pattern;
    }

    @Override
    public void setProperty(String name, String value)
    {
        if (name.equals("pattern") && value != null)
        {
            Pattern pattern = Pattern.compile(value);
            setPattern(pattern);
        }
    }

    @Override
    public String getProperty(String name)
    {
        if (name.equals("pattern") && pattern != null)
        {
            return pattern.toString();
        }
        return null;
    }

    @Override
    public Set<String> getPropertyNames()
    {
        Set<String> props = new TreeSet<String>();
        props.add("pattern");
        return props;
    }

    @Override
    public boolean validates(String fieldName)
    {
        ParameterCheck.mandatoryString("fieldName", fieldName);
        return (fieldName.equals("name"));
    }

    @Override
    public boolean validatesFullObject()
    {
        return false;
    }
}
