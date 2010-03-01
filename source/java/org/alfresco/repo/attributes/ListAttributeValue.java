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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Value based implementation of a list attribute.
 * @author britt
 */
public class ListAttributeValue extends AttributeValue implements ListAttribute
{
    private static final long serialVersionUID = 791121577967727000L;

    private List<Attribute> fData;
    
    public ListAttributeValue()
    {
        fData = new ArrayList<Attribute>();
    }
    
    public ListAttributeValue(ListAttribute attr)
    {
        this();
        for (Attribute entry : attr)
        {
            // Use the type's factory for AttributeValue
            Attribute newAttr = entry.getAttributeValue();
            fData.add(newAttr);
        }
    }
    
    public Attribute get(int index)
    {
        return fData.get(index);
    }

    public Type getType()
    {
        return Type.LIST;
    }

    public Serializable getRawValue()
    {
        return (Serializable) fData;
    }

    @Override
    public void add(Attribute attr)
    {
        fData.add(attr);
    }

    @Override
    public void add(int index, Attribute attr)
    {
        fData.add(index, attr);
    }

    @Override
    public Iterator<Attribute> iterator()
    {
        return fData.iterator();
    }

    @Override
    public int size()
    {
        return fData.size();
    }

    @Override
    public void remove(int index)
    {
        fData.remove(index);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (Attribute child : fData)
        {
            builder.append(child.toString());
            builder.append(' ');
        }
        builder.append(']');
        return builder.toString();
    }

    @Override
    public void set(int index, Attribute value)
    {
        fData.set(index, value);
    }
}
