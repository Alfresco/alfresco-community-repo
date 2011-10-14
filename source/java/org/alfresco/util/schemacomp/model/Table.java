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

/**
 * Instances of this class will represent a database table.
 * 
 * @author Matt Ward
 */
public class Table extends AbstractDbObject
{
    private List<Column> columns = new ArrayList<Column>();
    private PrimaryKey primaryKey;
    private ForeignKey foreignKey;
    private List<Index> indexes = new ArrayList<Index>();
    
    
    public Table(String name, Collection<Column> columns, PrimaryKey primaryKey, 
                ForeignKey foreignKey, Collection<Index> indexes)
    {
        super(name);
        if (columns != null)
        {
            columns.addAll(columns);
        }
        this.primaryKey = primaryKey;
        this.foreignKey = foreignKey;
        if (indexes != null)
        {
            indexes.addAll(indexes);
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
    public void setColumns(List<Column> columns)
    {
        this.columns = columns;
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
        this.primaryKey = primaryKey;
    }


    /**
     * @return the foreignKey
     */
    public ForeignKey getForeignKey()
    {
        return this.foreignKey;
    }


    /**
     * @param foreignKey the foreignKey to set
     */
    public void setForeignKey(ForeignKey foreignKey)
    {
        this.foreignKey = foreignKey;
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
    public void setIndexes(List<Index> indexes)
    {
        this.indexes = indexes;
    }


    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.columns == null) ? 0 : this.columns.hashCode());
        result = prime * result + ((this.foreignKey == null) ? 0 : this.foreignKey.hashCode());
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
        if (this.foreignKey == null)
        {
            if (other.foreignKey != null) return false;
        }
        else if (!this.foreignKey.equals(other.foreignKey)) return false;
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
}
