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
import java.util.Collection;
import java.util.List;

import org.alfresco.util.schemacomp.DbObjectVisitor;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.Result.Strength;

/**
 * Instances of this class represent a database table.
 * 
 * @author Matt Ward
 */
public class Table extends AbstractDbObject
{
    private final List<Column> columns = new ArrayList<Column>();
    private PrimaryKey primaryKey;
    private final List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();
    private final List<Index> indexes = new ArrayList<Index>();
    
    
    public Table(String name)
    {
        super(null, name);
    }
    
    public Table(String name, Collection<Column> columns, PrimaryKey primaryKey, 
                Collection<ForeignKey> foreignKeys, Collection<Index> indexes)
    {
        this(null, name, columns, primaryKey, foreignKeys, indexes);
    }
    
    public Table(Schema parentSchema, String name, Collection<Column> columns, PrimaryKey primaryKey, 
                Collection<ForeignKey> foreignKeys, Collection<Index> indexes)
    {
        super(parentSchema, name);
        if (columns != null)
        {
            setColumns(columns);
        }
        primaryKey.setParent(this);
        this.primaryKey = primaryKey;
        if (foreignKeys != null)
        {
            setForeignKeys(foreignKeys);
        }
        if (indexes != null)
        {
            setIndexes(indexes);
        }
    }


    /**
     * @return the columns
     */
    public List<Column> getColumns()
    {
        return this.columns;
    }


    /**
     * @param columns the columns to set
     */
    public void setColumns(Collection<Column> columns)
    {
        this.columns.clear();
        this.columns.addAll(columns);

        for (Column column : columns)
        {
            column.setParent(this);
        }
    }


    /**
     * @return the primaryKey
     */
    public PrimaryKey getPrimaryKey()
    {
        return this.primaryKey;
    }


    /**
     * @param primaryKey the primaryKey to set
     */
    public void setPrimaryKey(PrimaryKey primaryKey)
    {
        primaryKey.setParent(this);
        this.primaryKey = primaryKey;
    }


    /**
     * @return the foreignKeys
     */
    public List<ForeignKey> getForeignKeys()
    {
        return this.foreignKeys;
    }


    /**
     * @param foreignKeys the foreignKeys to set
     */
    public void setForeignKeys(Collection<ForeignKey> foreignKeys)
    {
        this.foreignKeys.clear();
        this.foreignKeys.addAll(foreignKeys);
        
        for (ForeignKey fk : foreignKeys)
        {
            fk.setParent(this);
        }
    }


    /**
     * @return the indexes
     */
    public List<Index> getIndexes()
    {
        return this.indexes;
    }


    /**
     * @param indexes the indexes to set
     */
    public void setIndexes(Collection<Index> indexes)
    {
        this.indexes.clear();
        this.indexes.addAll(indexes);
        
        for (Index index : indexes)
        {
            index.setParent(this);
        }
    }


    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.columns == null) ? 0 : this.columns.hashCode());
        result = prime * result + ((this.foreignKeys == null) ? 0 : this.foreignKeys.hashCode());
        result = prime * result + ((this.indexes == null) ? 0 : this.indexes.hashCode());
        result = prime * result + ((this.primaryKey == null) ? 0 : this.primaryKey.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        Table other = (Table) obj;
        if (this.columns == null)
        {
            if (other.columns != null) return false;
        }
        else if (!this.columns.equals(other.columns)) return false;
        if (this.foreignKeys == null)
        {
            if (other.foreignKeys != null) return false;
        }
        else if (!this.foreignKeys.equals(other.foreignKeys)) return false;
        if (this.indexes == null)
        {
            if (other.indexes != null) return false;
        }
        else if (!this.indexes.equals(other.indexes)) return false;
        if (this.primaryKey == null)
        {
            if (other.primaryKey != null) return false;
        }
        else if (!this.primaryKey.equals(other.primaryKey)) return false;
        return true;
    }


    @Override
    protected void doDiff(DbObject other, DiffContext ctx, Strength strength)
    {
        Table rightTable = (Table) other; 
        comparisonUtils.compareCollections(columns, rightTable.columns, ctx);
        primaryKey.diff(rightTable.primaryKey, ctx, strength);
        comparisonUtils.compareCollections(foreignKeys, rightTable.foreignKeys, ctx);
        comparisonUtils.compareCollections(indexes, rightTable.indexes, ctx);
    }
    

    private List<DbObject> getChildren()
    {
        List<DbObject> children = new ArrayList<DbObject>();
        children.addAll(columns); 
        children.add(primaryKey);
        children.addAll(foreignKeys);
        children.addAll(indexes);
        return children;
    }


    @Override
    public void accept(DbObjectVisitor visitor)
    {
        visitor.visit(this);

        for (DbObject child : getChildren())
        {
            child.accept(visitor);
        }
    }
}
