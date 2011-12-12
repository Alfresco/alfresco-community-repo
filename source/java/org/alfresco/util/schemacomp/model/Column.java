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
 * Represents a column in a database table.
 * 
 * @author Matt Ward
 */
public class Column extends AbstractDbObject
{
    private String type;
    private boolean nullable;
    private boolean autoIncrement;
    private int order;
    
    
    public Column(String name)
    {
        super(null, name);
    }
    
    /**
     * Construct a Column.
     * 
     * @table the parent table
     * @param name
     * @param type
     * @param nullable
     */
    public Column(Table table, String name, String type, boolean nullable)
    {
        super(table, name);
        this.type = type;
        this.nullable = nullable;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return this.type;
    }
    
    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }
    
    /**
     * @return the nullable
     */
    public boolean isNullable()
    {
        return this.nullable;
    }
    
    /**
     * @param nullable the nullable to set
     */
    public void setNullable(boolean nullable)
    {
        this.nullable = nullable;
    }
    
    
    /**
     * @return the order
     */
    public int getOrder()
    {
        return this.order;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(int order)
    {
        this.order = order;
    }

    
    /**
     * @return whether the column has an auto-increment flag set.
     */
    public boolean isAutoIncrement()
    {
        return this.autoIncrement;
    }

    /**
     * @param autoIncrement whether this column has the auto-increment flag set.
     */
    public void setAutoIncrement(boolean autoIncrement)
    {
        this.autoIncrement = autoIncrement;
    }


    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (this.autoIncrement ? 1231 : 1237);
        result = prime * result + (this.nullable ? 1231 : 1237);
        result = prime * result + this.order;
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        Column other = (Column) obj;
        if (this.autoIncrement != other.autoIncrement) return false;
        if (this.nullable != other.nullable) return false;
        if (this.order != other.order) return false;
        if (this.type == null)
        {
            if (other.type != null) return false;
        }
        else if (!this.type.equals(other.type)) return false;
        return true;
    }

    @Override
    protected void doDiff(DbObject right, DiffContext ctx, Strength strength)
    {
        DbProperty thisTypeProp = new DbProperty(this, "type");
        DbProperty thisNullableProp = new DbProperty(this, "nullable");
        DbProperty thisOrderProp = new DbProperty(this, "order");
        DbProperty thisAutoIncProp = new DbProperty(this, "autoIncrement");
        
        Column thatColumn = (Column) right;
        DbProperty thatTypeProp = new DbProperty(thatColumn, "type");
        DbProperty thatNullableProp = new DbProperty(thatColumn, "nullable");
        DbProperty thatOrderProp = new DbProperty(thatColumn, "order");
        DbProperty thatAutoIncProp = new DbProperty(thatColumn, "autoIncrement");
        
        comparisonUtils.compareSimple(thisTypeProp, thatTypeProp, ctx);
        comparisonUtils.compareSimple(thisNullableProp, thatNullableProp, ctx);        
        comparisonUtils.compareSimple(thisOrderProp, thatOrderProp, ctx);        
        comparisonUtils.compareSimple(thisAutoIncProp, thatAutoIncProp, ctx);        
    }

    @Override
    public void accept(DbObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public boolean sameAs(DbObject other)
    {
        if (other == null)
        {
            return false;
        }
        if (this == other)
        {
            return true;
        }
        if (getClass().equals(other.getClass()))
        {
            if (getName() != null && other.getName() != null)
            {
                if (getParent() != null && other.getParent() != null && getParent().sameAs(other.getParent()))
                {
                    return getName().equals(other.getName());
                }
            }
        }
        return false;
    }
}
