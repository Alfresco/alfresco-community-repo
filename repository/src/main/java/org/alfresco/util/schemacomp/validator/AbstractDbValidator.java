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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Base class providing DbValidator support.
 * 
 * @author Matt Ward
 */
public abstract class AbstractDbValidator implements DbValidator
{
    private final Map<String, String> properties = new HashMap<String, String>();
    private final Set<String> fieldsToValidate = new TreeSet<String>();

    @Override
    public void setProperty(String name, String value)
    {
        properties.put(name, value);
    }

    @Override
    public String getProperty(String name)
    {
        return properties.get(name);
    }

    @Override
    public Set<String> getPropertyNames()
    {
        return properties.keySet();
    }

    @Override
    public boolean validates(String fieldName)
    {
        return fieldsToValidate.contains(fieldName);
    }

    @Override
    public boolean validatesFullObject()
    {
        return false;
    }

    protected void setFieldsToValidate(Set<String> fieldsToValidate)
    {
        this.fieldsToValidate.clear();
        this.fieldsToValidate.addAll(fieldsToValidate);
    }

    protected void addFieldToValidate(String fieldName)
    {
        fieldsToValidate.add(fieldName);
    }
}
