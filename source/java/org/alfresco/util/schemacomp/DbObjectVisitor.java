package org.alfresco.util.schemacomp;

import org.alfresco.util.schemacomp.model.DbObject;

/**
 * Defines a visitor that can operate on a DbObject.
 * 
 * @author Matt Ward
 */
public interface DbObjectVisitor
{
    void visit(DbObject dbObject);
}
