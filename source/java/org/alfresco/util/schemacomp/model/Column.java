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

import org.alfresco.util.schemacomp.Differences;
import org.alfresco.util.schemacomp.SchemaUtils;

/**
 * Represents a column in a database table.
 * 
 * @author Matt Ward
 */
public class Column extends AbstractDbObject
{
    private String type;
    private boolean nullable;
    
    
    /**
     * Construct a Column.
     * 
     * @param name
     * @param type
     * @param nullable
     */
    public Column(String name, String type, boolean nullable)
    {
        super(name);
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
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (this.nullable ? 1231 : 1237);
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
        if (this.nullable != other.nullable) return false;
        if (this.type == null)
        {
            if (other.type != null) return false;
        }
        else if (!this.type.equals(other.type)) return false;
        return true;
    }

    @Override
    protected void doDiff(DbObject right, Differences differences)
    {
        Column rightColumn = (Column) right;
        SchemaUtils.compareSimple(type, rightColumn.type, differences);
        SchemaUtils.compareSimple(nullable, rightColumn.nullable, differences);        
    }
}
