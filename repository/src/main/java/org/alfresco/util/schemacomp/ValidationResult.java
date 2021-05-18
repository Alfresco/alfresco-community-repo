/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.util.schemacomp;

import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Results of a validation operation.
 * 
 * @author Matt Ward
 */
public class ValidationResult extends Result
{
    private DbProperty dbProperty;
    private String message;

    
    public ValidationResult(DbProperty dbProperty, String message)
    {
        this.dbProperty = dbProperty;
        this.message = message;
    }

    
    /**
     * @return the dbProperty that was rejected.
     */
    public DbProperty getDbProperty()
    {
        return this.dbProperty;
    }

    /**
     * @param dbProperty the dbProperty to set
     */
    public void setDbProperty(DbProperty dbProperty)
    {
        this.dbProperty = dbProperty;
    }

    
    @Override
    public String describe()
    {
        return doDescribe(I18NUtil.getLocale());
    }

    @Override
    public String describe(Locale locale)
    {
        return doDescribe(locale);
    }

    private String doDescribe(Locale locale)
    {
        return I18NUtil.getMessage(
                    "system.schema_comp.validation",
                    locale,
                    getDbProperty().getDbObject().getTypeName(),
                    getDbProperty().getPath(),
                    getValue(),
                    message);
    }

    /**
     * @return the value that was rejected.
     */
    public Object getValue()
    {
        return this.dbProperty.getPropertyValue();
    }
}
