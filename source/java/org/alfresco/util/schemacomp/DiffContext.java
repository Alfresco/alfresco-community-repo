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
package org.alfresco.util.schemacomp;

import java.util.List;

import org.hibernate.dialect.Dialect;

/**
 * A context made available to schema differencing and validation operations. It supplies information
 * about the {@link Dialect database dialect} that should be used when validating database properties
 * and the {@link Differences} object that should be populated with schema differences and validation errors.
 * 
 * @author Matt Ward
 */
public class DiffContext
{
    private final Dialect dialect;
    private final Differences differences;
    private final List<ValidationResult> validationResults;
    
    /**
     * @param dialect
     * @param differences
     */
    public DiffContext(Dialect dialect, Differences differences, List<ValidationResult> validationResults)
    {
        this.dialect = dialect;
        this.differences = differences;
        this.validationResults = validationResults;
    }

    /**
     * @return the dialect
     */
    public Dialect getDialect()
    {
        return this.dialect;
    }

    /**
     * @return the differences
     */
    public Differences getDifferences()
    {
        return this.differences;
    }

    /**
     * @return the validationResults
     */
    public List<ValidationResult> getValidationResults()
    {
        return this.validationResults;
    }
    
    
}
