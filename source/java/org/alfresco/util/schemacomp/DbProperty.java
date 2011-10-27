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
package org.alfresco.util.schemacomp;

import org.alfresco.util.schemacomp.model.DbObject;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * A pointer to a specific DbObject property and its value (at time of creating the DbProperty object).
 * 
 * @author Matt Ward
 */
public class DbProperty
{
    private final DbObject dbObject;
    private final String propertyName;
    private final Object propertyValue;
    
    /**
     * Full constructor allowing control over whether the property name should be indexed (e.g. colours[3]),
     * whether the current value of the property should be retrieved automatically or whether to use the
     * supplied value (useful when performing comparisons - construct one with a particular/expected value and
     * construct another with the current value by reflection).
     * <p>
     * The public constructors provide a more usable API with select sets of arguments.
     * 
     * @param dbObject
     * @param propertyName
     * @param propertyValue
     */
    protected DbProperty(DbObject dbObject, String propertyName, int index, boolean useSuppliedValue, Object propertyValue)
    {
        if (dbObject == null)
        {
            throw new IllegalArgumentException("dbObject cannot be null.");
        }
        this.dbObject = dbObject;
        
        if (propertyName == null)
        {
            if (index > -1 || useSuppliedValue)
            {
                throw new IllegalArgumentException("propertyName cannot be null.");
            }
        }
        if (index > -1)
        {
            this.propertyName = propertyName+"["+index+"]";
        }
        else
        {
            this.propertyName = propertyName;
        }

        // Unfortunetely, this boolean is required, since we may want to set the property value to null.
        if (useSuppliedValue)
        {
            this.propertyValue = propertyValue;
        }
        else if (propertyName != null)
        {
            try
            {
                this.propertyValue = PropertyUtils.getProperty(dbObject, this.propertyName);
            }
            catch (Throwable error)
            {
                throw new IllegalArgumentException("Cannot get value for property named \"" + propertyName + "\"", error);
            }
        }
        else
        {
            // No property is being referred to by this object.
            this.propertyValue = null;
        }
    }
    
    /**
     * Construct a pointer to a database object only (no property within).
     * 
     * @param dbObject
     */
    public DbProperty(DbObject dbObject)
    {
        this(dbObject, null, -1, false, null);
    }
    
    /**
     * Create a DbProperty by supplying the DbObject and the property name. The
     * value at time of creation will be populate automatically.
     * 
     * @param dbObject
     * @param propertyName
     */
    public DbProperty(DbObject dbObject, String propertyName)
    {
        this(dbObject, propertyName, -1, false, null);
    }
    
    
    /**
     * Create a DbProperty with an indexed value, e.g. for propertyName "myCollection" and
     * index 4, the propertyName will be converted to "myCollection[4]" and the propertValue
     * will be populated with the value at index 4 of myCollection.
     * 
     * @param dbObject
     * @param propertyName
     * @param index
     */
    public DbProperty(DbObject dbObject, String propertyName, int index)
    {
        this(dbObject, propertyName, index, false, null);
    }
    
    /**
     * @return the dbObject
     */
    public DbObject getDbObject()
    {
        return this.dbObject;
    }

    /**
     * @return the propertyName
     */
    public String getPropertyName()
    {
        return this.propertyName;
    }

    /**
     * @return the propertyValue
     */
    public Object getPropertyValue()
    {
        return this.propertyValue;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.dbObject == null) ? 0 : this.dbObject.hashCode());
        result = prime * result + ((this.propertyName == null) ? 0 : this.propertyName.hashCode());
        result = prime * result
                    + ((this.propertyValue == null) ? 0 : this.propertyValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DbProperty other = (DbProperty) obj;
        if (this.dbObject == null)
        {
            if (other.dbObject != null) return false;
        }
        else if (!this.dbObject.equals(other.dbObject)) return false;
        if (this.propertyName == null)
        {
            if (other.propertyName != null) return false;
        }
        else if (!this.propertyName.equals(other.propertyName)) return false;
        if (this.propertyValue == null)
        {
            if (other.propertyValue != null) return false;
        }
        else if (!this.propertyValue.equals(other.propertyValue)) return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "DbProperty [dbObject=" + this.dbObject + ", propertyName=" + this.propertyName
                    + ", propertyValue=" + this.propertyValue + "]";
    }


    /**
     * Work backwards from this DbProperty's DbObject to the root object to create a path in the
     * following format:
     * <p>
     * root.child.grandchild[...].property
     * <p>
     * e.g. myschema.person.age.nullable
     * <p>
     * This isn't exactly the same as a FQ database object name, for example the property name could be indexed:
     * <p>
     * e.g. myschema.person.pk_person.columnNames[2]
     * <p>
     * to reflect the third column name in the primary key named "pk_person" on the person table.
     * 
     * @return String path
     */
    public String getPath()
    {
        StringBuffer sb = new StringBuffer();
        
        if (getPropertyName() != null)
        {
            sb.append(".");
            sb.append(getPropertyName());
        }
        
        for (DbObject pathElement = dbObject; pathElement != null; pathElement = pathElement.getParent())
        {
            sb.insert(0, pathElement.getName());
            if (pathElement.getParent() != null)
            {
                sb.insert(0, ".");
            }
        }
        
        return sb.toString();
    }
    
    
}
