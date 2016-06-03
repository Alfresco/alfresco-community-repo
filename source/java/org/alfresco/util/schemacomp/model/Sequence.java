package org.alfresco.util.schemacomp.model;

import org.alfresco.util.schemacomp.DbObjectVisitor;


/**
 * Represents a database sequence.
 * 
 * @author Matt Ward
 */
public class Sequence extends AbstractDbObject
{
    public Sequence(String name)
    {
        super(null, name);
    }
    
    public Sequence(DbObject parent, String name)
    {
        super(parent, name);
    }
    
    @Override
    public void accept(DbObjectVisitor visitor)
    {
        visitor.visit(this);
    }
}
