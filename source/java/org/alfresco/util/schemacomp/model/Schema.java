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
package org.alfresco.util.schemacomp.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.util.schemacomp.DbObjectVisitor;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.Results;
import org.alfresco.util.schemacomp.Result.Strength;

/**
 * Instances of this class represent a database schema.
 * 
 * @author Matt Ward
 */
public class Schema extends AbstractDbObject implements Iterable<DbObject>
{
    protected final List<DbObject> objects = new ArrayList<DbObject>();

    /**
     * Construct a schema with the given name.
     * 
     * @param name
     */
    public Schema(String name)
    {
        super(null, name);
    }

    /**
     * Add an object to this schema - this method will set this schema
     * as the object's parent.
     * 
     * @param dbObject
     */
    public void add(DbObject dbObject)
    {
        dbObject.setParent(this);
        objects.add(dbObject);
    }
    
    @Override
    public Iterator<DbObject> iterator()
    {
        return objects.iterator();
    }

    /**
     * @param identifier
     * @return
     */
    public boolean contains(DbObject object)
    {
        return objects.contains(object);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.objects == null) ? 0 : this.objects.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        Schema other = (Schema) obj;
        if (this.objects == null)
        {
            if (other.objects != null) return false;
        }
        else if (!this.objects.equals(other.objects)) return false;
        return true;
    }
    
    
    @Override
    protected void doDiff(DbObject right, DiffContext ctx, Strength strength)
    {
        Schema rightSchema = (Schema) right;
        comparisonUtils.compareCollections(objects, rightSchema.objects, ctx);
    }


    @Override
    public void accept(DbObjectVisitor visitor)
    {
        for (DbObject child : objects)
        {
            child.accept(visitor);
        }
        
        visitor.visit(this);
    }
}
