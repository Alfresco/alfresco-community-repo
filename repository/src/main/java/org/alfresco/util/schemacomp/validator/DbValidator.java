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

import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.model.DbObject;

/**
 * DbObject validators must implement this interface. DbValidator instances are used by the ValidatingVisitor class.
 * 
 * @author Matt Ward
 */
public interface DbValidator
{
    /**
     * Validate the target database object (against the reference object if necessary). Store the validation results on the DiffContext.
     * 
     * @param reference
     *            DbObject
     * @param target
     *            DbObject
     * @param ctx
     *            DiffContext
     */
    void validate(DbObject reference, DbObject target, DiffContext ctx);

    /**
     * Set a property used by this validator. Validator properties provided in the schema reference XML files will be set on the validator using this method.
     * 
     * @param name
     *            String
     * @param value
     *            String
     */
    void setProperty(String name, String value);

    /**
     * Get the current value of a validator property, as set using {@link #setProperty(String, String)}.
     * 
     * @param name
     *            String
     * @return String
     */
    String getProperty(String name);

    /**
     * Get the complete set of validator properties in use.
     * 
     */
    Set<String> getPropertyNames();

    /**
     * Ask whether the database object's validator is responsible for validating the specified field name. This only applies to simple properties - not DbObject instances which should provide their own validators.
     * 
     * @param fieldName
     *            String
     * @return boolean
     */
    boolean validates(String fieldName);

    /**
     * Asks whether the database object's validator is responsible for validating the entire DbObject. If true, then differences are not reported (e.g. table missing from database) as it is the validator's role to worry about presence. If validation and differences are required then report false - even if the validator works at the full object (rather than property) level.
     * 
     * @return true if missing or unexpected database objects should not be reported by differencing logic.
     */
    boolean validatesFullObject();
}
