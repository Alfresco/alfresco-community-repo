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

import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.util.schemacomp.model.Schema;

/**
 * A context made available to schema differencing and validation operations. It supplies information about the {@link Dialect database dialect} that should be used when validating database properties and the {@link Results} object that should be populated with schema differences and validation errors.
 * 
 * @author Matt Ward
 */
public class DiffContext
{
    private final Dialect dialect;
    private final Results results;
    private final Schema referenceSchema;
    private final Schema targetSchema;

    /**
     * Constructor.
     * 
     * @param dialect
     *            Dialect
     * @param results
     *            Results
     * @param referenceSchema
     *            Schema
     * @param targetSchema
     *            Schema
     */
    public DiffContext(Dialect dialect, Results results, Schema referenceSchema, Schema targetSchema)
    {
        this.dialect = dialect;
        this.results = results;
        this.referenceSchema = referenceSchema;
        this.targetSchema = targetSchema;
    }

    /**
     * Constructor.
     * 
     * @param dialect
     *            Dialect
     * @param referenceSchema
     *            Schema
     * @param targetSchema
     *            Schema
     */
    public DiffContext(Dialect dialect, Schema referenceSchema, Schema targetSchema)
    {
        this(dialect, new Results(), referenceSchema, targetSchema);
    }

    /**
     * @return the dialect
     */
    public Dialect getDialect()
    {
        return this.dialect;
    }

    /**
     * @return the results of schema comparison: validation failures, differences etc.
     */
    public Results getComparisonResults()
    {
        return this.results;
    }

    /**
     * @return the referenceSchema
     */
    public Schema getReferenceSchema()
    {
        return this.referenceSchema;
    }

    /**
     * @return the targetSchema
     */
    public Schema getTargetSchema()
    {
        return this.targetSchema;
    }
}
