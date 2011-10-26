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
    protected NullValidator nullValidator = new NullValidator();

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
            return nullValidator;
        }
    }

    @Override
    public void visit(DbObject dbObject)
    {
        DbValidator validator = getValidatorFor(dbObject.getClass());
        validator.validate(dbObject, ctx);
    }
}
