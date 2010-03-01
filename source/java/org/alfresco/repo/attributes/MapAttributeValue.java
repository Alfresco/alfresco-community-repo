/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.attributes;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Value based implementation of a map attribute.
 * @author britt
 */
public class MapAttributeValue extends AttributeValue implements MapAttribute
{
    private static final long serialVersionUID = -5090943744202078113L;

    private Map<String, Attribute> fData;
    
    public MapAttributeValue()
    {
        fData = new HashMap<String, Attribute>();
    }
    
    public MapAttributeValue(MapAttribute attr)
    {
        super(attr.getAcl());
        fData = new HashMap<String, Attribute>();
        for (Map.Entry<String, Attribute> entry : attr.entrySet())
        {
            String key = entry.getKey();
            Attribute value = entry.getValue();
            // Use the object's factory for AttributeValue
            Attribute newAttr = value.getAttributeValue();
            // Put it into the map
            fData.put(key, newAttr);
        }
    }
    
    public Type getType()
    {
        return Type.MAP;
    }

    public Serializable getRawValue()
    {
        return (Serializable) fData;
    }

    @Override
    public void clear()
    {
        fData.clear();
    }

    @Override
    public Set<Entry<String, Attribute>> entrySet()
    {
        return fData.entrySet();
    }

    @Override
    public Attribute get(String key)
    {
        return fData.get(key);
    }

    @Override
    public Set<String> keySet()
    {
        return fData.keySet();
    }

    @Override
    public void put(String key, Attribute value)
    {
        fData.put(key, value);
    }

    @Override
    public void remove(String key)
    {
        fData.remove(key);
    }

    @Override
    public Collection<Attribute> values()
    {
        return fData.values();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        for (Map.Entry<String, Attribute> entry : fData.entrySet())
        {
            builder.append(entry.getKey());
            builder.append('=');
            builder.append(entry.getValue().toString());
            builder.append(' ');
        }
        builder.append('}');
        return builder.toString();
    }

    @Override
    public int size()
    {
        return fData.size();
    }
}
