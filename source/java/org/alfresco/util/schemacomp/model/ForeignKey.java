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

import org.alfresco.util.schemacomp.DbObjectVisitor;
import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.Result.Strength;


/**
 * Represents a foreign key on a database table (<code>localColumn</code>) that references
 * <code>targetTable.targetColumn</code>
 * 
 * @author Matt Ward
 */
public class ForeignKey extends AbstractDbObject
{
    private String localColumn;
    private String targetTable;
    private String targetColumn;
    
    
    public ForeignKey(String name)
    {
        super(null, name);
    }
    
    /**
     * Constructor.
     * 
     * @param table the parent table
     * @param fkName
     * @param localColumn
     * @param targetTable
     * @param targetColumn
     */
    public ForeignKey(Table table, String fkName, String localColumn, String targetTable, String targetColumn)
    {
        super(table, fkName);
        this.localColumn = localColumn;
        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
    }
    
    /**
     * @return the localColumn
     */
    public String getLocalColumn()
    {
        return this.localColumn;
    }
    
    /**
     * @param localColumn the localColumn to set
     */
    public void setLocalColumn(String localColumn)
    {
        this.localColumn = localColumn;
    }
    
    /**
     * @return the targetTable
     */
    public String getTargetTable()
    {
        return this.targetTable;
    }
    
    /**
     * @param targetTable the targetTable to set
     */
    public void setTargetTable(String targetTable)
    {
        this.targetTable = targetTable;
    }
    
    /**
     * @return the targetColumn
     */
    public String getTargetColumn()
    {
        return this.targetColumn;
    }
    
    /**
     * @param targetColumn the targetColumn to set
     */
    public void setTargetColumn(String targetColumn)
    {
        this.targetColumn = targetColumn;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.localColumn == null) ? 0 : this.localColumn.hashCode());
        result = prime * result + ((this.targetColumn == null) ? 0 : this.targetColumn.hashCode());
        result = prime * result + ((this.targetTable == null) ? 0 : this.targetTable.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        ForeignKey other = (ForeignKey) obj;
        if (this.localColumn == null)
        {
            if (other.localColumn != null) return false;
        }
        else if (!this.localColumn.equals(other.localColumn)) return false;
        if (this.targetColumn == null)
        {
            if (other.targetColumn != null) return false;
        }
        else if (!this.targetColumn.equals(other.targetColumn)) return false;
        if (this.targetTable == null)
        {
            if (other.targetTable != null) return false;
        }
        else if (!this.targetTable.equals(other.targetTable)) return false;
        return true;
    }
    
    
    @Override
    protected void doDiff(DbObject right, DiffContext ctx, Strength strength)
    {
        ForeignKey thatFK = (ForeignKey) right;
        comparisonUtils.compareSimple(
                    new DbProperty(this, "localColumn"),
                    new DbProperty(thatFK, "localColumn"),
                    ctx);
        comparisonUtils.compareSimple(
                    new DbProperty(this, "targetTable"),
                    new DbProperty(thatFK, "targetTable"),
                    ctx);
        comparisonUtils.compareSimple(
                    new DbProperty(this, "targetColumn"),
                    new DbProperty(thatFK, "targetColumn"),
                    ctx);        
    }

    @Override
    public void accept(DbObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String getTypeName()
    {
        return "foreign key";
    }
}
