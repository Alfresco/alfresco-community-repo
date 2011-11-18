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
