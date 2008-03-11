/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
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
