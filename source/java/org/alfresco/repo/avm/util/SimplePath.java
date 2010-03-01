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
package org.alfresco.repo.avm.util;

import java.io.Serializable;

/**
 * Holds a simple path.
 * @author britt
 */
public class SimplePath implements Serializable
{
    private static final long serialVersionUID = 2696828491008988470L;

    /**
     * The names of the path's components.
     */
    private String [] fNames;

    /**
     * Construct a new one from a string.
     * @param path The String representation of the path.
     */
    public SimplePath(String path)
    {
        if (path.length() == 0)
        {
            fNames = new String[0];
            return;
        }
        while (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        while (path.endsWith("/"))
        {
            path = path.substring(0, path.length() - 1);
        }
        fNames = path.split("/+");
    }

    /**
     * Get the component name at index.
     * @param index The index of the component to get.
     * @return The name of the component.
     */
    public String get(int index)
    {
        return fNames[index];
    }

    /**
     * Get the number of components in this path.
     * @return The number of components.
     */
    public int size()
    {
        return fNames.length;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof SimplePath))
        {
            return false;
        }
        SimplePath o = (SimplePath)obj;
        if (fNames.length != o.fNames.length)
        {
            return false;
        }
        for (int i = 0; i < fNames.length; i++)
        {
            if (!fNames[i].equalsIgnoreCase(o.fNames[i]))
            {
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        int hash = 0;
        for (String name : fNames)
        {
            hash += name.toLowerCase().hashCode();
        }
        return hash;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (String name : fNames)
        {
            builder.append('/');
            builder.append(name);
        }
        return builder.toString();
    }
}
