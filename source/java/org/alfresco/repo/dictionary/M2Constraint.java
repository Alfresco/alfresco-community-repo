/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Property Constraint.
 * 
 * @author Derek Hulley
 */
public class M2Constraint
{
    private String name;
    private String ref;
    private String type;
    private String description;
    private List<M2NamedValue> parameters = new ArrayList<M2NamedValue>(2);

    /*package*/ M2Constraint()
    {
    }
    
    @Override
    public String toString()
    {
        return this.name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getRef()
    {
        return ref;
    }

    public String getType()
    {
        return type;
    }

    public String getDescription()
    {
        return description;
    }
    
    public List<M2NamedValue> getParameters()
    {
        return parameters;
    }
}
