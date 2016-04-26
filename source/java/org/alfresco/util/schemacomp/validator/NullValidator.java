package org.alfresco.util.schemacomp.validator;

import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.model.DbObject;

/**
 * A 'do nothing' validator
 * 
 * @author Matt Ward
 */
public class NullValidator extends AbstractDbValidator
{
    @Override
    public void validate(DbObject reference, DbObject target, DiffContext ctx)
    {
        // Do nothing
    }
}
