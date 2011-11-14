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

import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.validator.DbValidator;
import org.alfresco.util.schemacomp.validator.NameValidator;
import org.alfresco.util.schemacomp.validator.NullValidator;

/**
 * Invokes the correct validator for a given DbObject.
 * 
 * @author Matt Ward
 */
public class ValidatingVisitor implements DbObjectVisitor
{
    private DiffContext ctx;
    protected NameValidator indexNameValidator = new NameValidator();
    protected NullValidator defaultValidator = new NullValidator();
    protected ComparisonUtils comparisonUtils = new DefaultComparisonUtils();

    public ValidatingVisitor(DiffContext ctx)
    {
        this.ctx = ctx;
    }
    
    
    protected DbValidator getValidatorFor(Class<? extends DbObject> c)
    {
        if (c.equals(Index.class))
        {
            return indexNameValidator;
        }
        else
        {
            return defaultValidator;
        }
    }

    @Override
    public void visit(DbObject referenceObj)
    {
        DbValidator validator = getValidatorFor(referenceObj.getClass());
        List<DbObject> matches = comparisonUtils.findEquivalentObjects(ctx.getTargetSchema(), referenceObj);
        
        // TODO: if matches.size() > 1 then warn of possible redundant database objects
        
        for (DbObject target : matches)
        {
            validator.validate(referenceObj, target, ctx);            
        }
    }
}
