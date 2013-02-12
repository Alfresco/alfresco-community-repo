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
import java.util.regex.Pattern;

import org.alfresco.util.ParameterCheck;
import org.alfresco.util.schemacomp.DbObjectVisitor;
import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.validator.NameValidator;
import org.alfresco.util.schemacomp.validator.SchemaVersionValidator;

/**
 * Instances of this class represent a database schema.
 * 
 * @author Matt Ward
 */
public class Schema extends AbstractDbObject implements Iterable<DbObject>
{
    protected final List<DbObject> objects = new ArrayList<DbObject>();
    protected final String dbPrefix;
    protected final int version;
    protected final boolean checkTableColumnOrder;
    
    /**
     * Construct a schema with the given name and no database prefix.
     * 
     * @param name
     */
    public Schema(String name)
    {
        this(name, "", 0, true);
    }
    
    /**
     * Construct a schema with the given name and database prefix. The database
     * prefix specifies what filtering applies to the high-level (tables and sequences)
     * objects in the schema. If for example dbPrefix is "alf_" then only tables and sequences
     * whose names begin with "alf_" will be represented by this schema. Therefore any comparisons
     * should be performed against another similarly filtered Schema object.
     * 
     * @param name
     * @param dbPrefix
     */
    public Schema(String name, String dbPrefix, int schemaVersion, boolean checkTableColumnOrder)
    {
        super(null, name);
        ParameterCheck.mandatory("dbPrefix", dbPrefix);
        this.dbPrefix = dbPrefix;
        this.version = schemaVersion;
        this.checkTableColumnOrder = checkTableColumnOrder;
        
        addDefaultValidators();
    }

    /**
     * Add a set of validators that should be present by default on Schema objects.
     */
    private void addDefaultValidators()
    {
        // We expect the user's database to have a different schema name to the reference schema.
        NameValidator nameValidator = new NameValidator();
        nameValidator.setPattern(Pattern.compile(".*"));
        getValidators().add(nameValidator);
        
        // The schema version shouldn't have to match exactly.
        SchemaVersionValidator versionValidator = new SchemaVersionValidator();
        getValidators().add(versionValidator);
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

    /**
     * @return the dbPrefix
     */
    public String getDbPrefix()
    {
        return this.dbPrefix;
    }

    /**
     * @return the version
     */
    public int getVersion()
    {
        return this.version;
    }

    /**
     * @return the checkTableColumnOrder
     */
    public boolean isCheckTableColumnOrder()
    {
        return this.checkTableColumnOrder;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.dbPrefix == null) ? 0 : this.dbPrefix.hashCode());
        result = prime * result + ((this.objects == null) ? 0 : this.objects.hashCode());
        result = prime * result + this.version;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        Schema other = (Schema) obj;
        if (this.dbPrefix == null)
        {
            if (other.dbPrefix != null) return false;
        }
        else if (!this.dbPrefix.equals(other.dbPrefix)) return false;
        if (this.objects == null)
        {
            if (other.objects != null) return false;
        }
        else if (!this.objects.equals(other.objects)) return false;
        if (this.version != other.version) return false;
        return true;
    }

    @Override
    protected void doDiff(DbObject right, DiffContext ctx)
    {
        Schema rightSchema = (Schema) right;        
        comparisonUtils.compareSimple(
                    new DbProperty(this, "version"), 
                    new DbProperty(rightSchema, "version"), ctx);
        comparisonUtils.compareCollections(objects, rightSchema.objects, ctx);
    }


    @Override
    public void accept(DbObjectVisitor visitor)
    {        
        visitor.visit(this);
        
        for (DbObject child : objects)
        {
            child.accept(visitor);
        }
    }

    @Override
    public boolean sameAs(DbObject other)
    {
        if (other == null || (!other.getClass().equals(getClass())))
        {
            return false;
        }
        return true;
    }    

    /*
     * ALF-14129 fix, checks whether the schema already contains object with provided name. 
     * (this method is case insensitive to object's name)
     */
    public boolean containsByName(String name)
    {
        Iterator<DbObject> iterator = iterator();
        while (iterator.hasNext())
        {
            if (iterator.next().getName().equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }
}
