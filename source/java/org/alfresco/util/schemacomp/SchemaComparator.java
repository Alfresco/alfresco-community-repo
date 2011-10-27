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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.util.schemacomp.Result.Strength;
import org.alfresco.util.schemacomp.model.Schema;
import org.hibernate.dialect.Dialect;

/**
 * Compares two abstract database schemas, a 'left' schema and a 'right' schema. The left schema is the primary
 * schema and all objects in the right schema are judged relative to it.
 * 
 * @author Matt Ward
 */
public class SchemaComparator
{
    private final Schema leftSchema;
    private final Schema rightSchema;
    private final DiffContext ctx;
    
    /**
     * Construct a comparator to compare schemas left and right.
     * 
     * @param left
     * @param right
     */
    public SchemaComparator(Schema left, Schema right, Dialect dialect)
    {
        this.leftSchema = left;
        this.rightSchema = right;
        this.ctx = new DiffContext(dialect, new Results(), new ArrayList<ValidationResult>());
    }
    
    
    public void validateAndCompare()
    {
        validate();
        compare();
    }


    /**
     * Check the left schema against the right schema and record any differences.
     */
    private void compare()
    {
        leftSchema.diff(rightSchema, ctx, Strength.ERROR);
    }


    /**
     * Validate both schemas.
     */
    private void validate()
    {
        ValidatingVisitor validatingVisitor = new ValidatingVisitor(ctx);
        leftSchema.accept(validatingVisitor);
        rightSchema.accept(validatingVisitor);
    }


    /**
     * @return the differences
     */
    public Results getDifferences()
    {
        return ctx.getDifferences();
    }


    /**
     * @return the validation results.
     */
    public List<ValidationResult> getValidationResults()
    {
        return ctx.getValidationResults();
    }    
}
