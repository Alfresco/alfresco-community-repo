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
package org.alfresco.util.schemacomp;

import org.apache.xml.serialize.LineSeparator;

import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.util.schemacomp.model.Schema;

/**
 * Compares two abstract database schemas, a reference schema and a target schema (the schema to check for validity). The reference schema is the primary schema and all objects in the right schema are judged relative to it.
 * 
 * @author Matt Ward
 */
public class SchemaComparator
{
    public static final String CHAR_SET = "UTF-8";
    public static final String LINE_SEPARATOR = LineSeparator.Windows;
    private final Schema referenceSchema;
    private final Schema targetSchema;
    private final DiffContext ctx;

    /**
     * Construct a comparator to compare a target schema against a reference schema. Validators supplied by the reference schema will be used to validate the target schema.
     * 
     * @param referenceSchema
     *            Schema
     * @param targetSchema
     *            Schema
     * @param dialect
     *            Dialect
     */
    public SchemaComparator(Schema referenceSchema, Schema targetSchema, Dialect dialect)
    {
        this.referenceSchema = referenceSchema;
        this.targetSchema = targetSchema;
        this.ctx = new DiffContext(dialect, referenceSchema, targetSchema);
    }

    public void validateAndCompare()
    {
        validateTargetSchema();
        compare();
    }

    /**
     * Check the left schema against the right schema and record any differences.
     */
    private void compare()
    {
        referenceSchema.diff(targetSchema, ctx);
    }

    /**
     * Validate the target schema against the reference schema using the reference schema's validators.
     */
    private void validateTargetSchema()
    {
        ValidatingVisitor validatingVisitor = new ValidatingVisitor(ctx);
        referenceSchema.accept(validatingVisitor);
    }

    /**
     * @return the schema comparison results.
     */
    public Results getComparisonResults()
    {
        return ctx.getComparisonResults();
    }
}
