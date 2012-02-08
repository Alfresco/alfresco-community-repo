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

import java.util.Set;
import java.util.TreeSet;

import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.model.DbObject;

/**
 * Allows a complete DbObject to be ignored during differencing. In other
 * words if an object that has this validator applied to it is found to be
 * missing from or unexpectedly in the database then that will not be reported
 * as a difference.
 * 
 * @author Matt Ward
 */
public class IgnoreObjectValidator implements DbValidator
{

    @Override
    public void validate(DbObject reference, DbObject target, DiffContext ctx)
    {
        // Do nothing
    }

    @Override
    public void setProperty(String name, String value)
    {
        // No validation properties
        throw new UnsupportedOperationException("Properties are not supported by this validator.");
    }

    @Override
    public String getProperty(String name)
    {
        throw new IllegalArgumentException("There are no properties for a " + getClass().getName());
    }

    @Override
    public Set<String> getPropertyNames()
    {
        return new TreeSet<String>();
    }

    @Override
    public boolean validates(String fieldName)
    {
        return false;
    }

    @Override
    public boolean validatesFullObject()
    {
        // This is the important part of this implementation - to ignore an object
        // we need to say this validator takes full responsibility for it.
        return true;
    }
}
