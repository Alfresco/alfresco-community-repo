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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Instances of this class will represent a database schema.
 * 
 * @author Matt Ward
 */
public class 
Schema extends AbstractDbObject implements Iterable<DbObject>
{
    private final Map<Object, DbObject> objects = new LinkedHashMap<Object, DbObject>();

    /**
     * @param key
     * @return
     */
    public DbObject get(Object key)
    {
        return this.objects.get(key);
    }

    /**
     * @param table
     */
    public void put(DbObject dbObject)
    {
        objects.put(dbObject.getIdentifier(), dbObject);
    }

    @Override
    public Iterator<DbObject> iterator()
    {
        return objects.values().iterator();
    }

    /**
     * @param identifier
     * @return
     */
    public boolean contains(Object identifier)
    {
        return objects.containsKey(identifier);
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
}
