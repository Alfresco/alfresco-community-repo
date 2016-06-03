package org.alfresco.util.schemacomp;

import org.alfresco.util.schemacomp.model.Schema;
import org.hibernate.dialect.Dialect;

/**
 * A context made available to schema differencing and validation operations. It supplies information
 * about the {@link Dialect database dialect} that should be used when validating database properties
 * and the {@link Results} object that should be populated with schema differences and validation errors.
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
     * @param dialect Dialect
     * @param results Results
     * @param referenceSchema Schema
     * @param targetSchema Schema
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
     * @param dialect Dialect
     * @param referenceSchema Schema
     * @param targetSchema Schema
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
