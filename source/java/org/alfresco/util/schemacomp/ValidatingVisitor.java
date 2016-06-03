package org.alfresco.util.schemacomp;

import java.util.List;

import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.validator.DbValidator;

/**
 * Invokes the correct validator for a given DbObject.
 * 
 * @author Matt Ward
 */
public class ValidatingVisitor implements DbObjectVisitor
{
    private DiffContext ctx;
    private ComparisonUtils comparisonUtils = new DefaultComparisonUtils();

    public ValidatingVisitor(DiffContext ctx)
    {
        this.ctx = ctx;
    }
    

    @Override
    public void visit(DbObject referenceObj)
    {
        List<DbObject> matches = comparisonUtils.findEquivalentObjects(ctx.getTargetSchema(), referenceObj);
        
        if (matches.size() > 1)
        {
            ctx.getComparisonResults().add(new RedundantDbObject(referenceObj, matches));
        }
        
        // Validate each matching target object against the reference object
        // using each of the available validators for that reference object.
        for (DbValidator validator : referenceObj.getValidators())
        {
            for (DbObject target : matches)
            {
                validator.validate(referenceObj, target, ctx);            
            }
        }
    }


    /**
     * @param comparisonUtils the comparisonUtils to set
     */
    public void setComparisonUtils(ComparisonUtils comparisonUtils)
    {
        this.comparisonUtils = comparisonUtils;
    }
}
