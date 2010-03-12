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
package org.alfresco.repo.admin.registry;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Key for looking up registry metadata.
 * 
 * @author Derek Hulley
 */
public class RegistryKey implements Serializable
{
    private static final long serialVersionUID = 1137822242292626854L;

    static final String REGISTRY_1_0_URI = "http://www.alfresco.org/system/registry/1.0";
    
    private String namespaceUri;
    private String[] path;
    private String property;
    
    /**
     * Build a registry key from a given array of elements.
     */
    private static String buildPathString(String... elements)
    {
        if (elements.length == 0)
        {
            return "/";
        }
        StringBuilder sb = new StringBuilder();
        for (String element : elements)
        {
            if (element == null || element.length() == 0)
            {
                throw new IllegalArgumentException("Key elements may not be empty or null");
            }
            sb.append("/").append(element);
        }
        return sb.toString();
    }
    
    /**
     * For path /a/b/c and property 'x', put in <pre>"a", "b", "c", "x"</pre>
     * The property can also be <tt>null</tt> as in <pre>"a", "b", "c", null</pre>
     * 
     * @param namespaceUri the key namespace to use.  If left <tt>null</tt> then the
     *      {@link #REGISTRY_1_0_URI default} will be used.
     * @param key the path elements followed by the property name.
     */
    public RegistryKey(String namespaceUri, String... key)
    {
        if (namespaceUri == null)
        {
            namespaceUri = REGISTRY_1_0_URI;
        }
        this.namespaceUri = namespaceUri;
        // The last value is the property
        int length = key.length;
        if (length == 0)
        {
            throw new IllegalArgumentException("No value supplied for the RegistryKey property");
        }
        this.property = key[length - 1];
        this.path = new String[length - 1];
        System.arraycopy(key, 0, path, 0, length - 1);
    }
    
    /**
     * A constructor to specifically declare the path and property portions of the key.
     * 
     * @param namespaceUri the key namespace to use.  If left <tt>null</tt> then the
     *      {@link #REGISTRY_1_0_URI default} will be used.
     * @param path          the path part of the key
     * @param property      the property name for the key.  This may be <tt>null</tt>.
     */
    public RegistryKey(String namespaceUri, String[] path, String property)
    {
        if (namespaceUri == null)
        {
            namespaceUri = REGISTRY_1_0_URI;
        }
        this.namespaceUri = namespaceUri;
        if ((path == null || path.length == 0) && property == null)
        {
            throw new IllegalArgumentException("No path or property supplied for the RegistryKey");
        }
        this.property = property;
        this.path = path;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("RegistryKey")
          .append("[ ").append(RegistryKey.buildPathString(path)).append("/").append(property)
          .append(" ]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RegistryKey other = (RegistryKey) obj;
        if (namespaceUri == null)
        {
            if (other.namespaceUri != null)
                return false;
        }
        else if (!namespaceUri.equals(other.namespaceUri))
            return false;
        if (!Arrays.equals(path, other.path))
            return false;
        if (property == null)
        {
            if (other.property != null)
                return false;
        }
        else if (!property.equals(other.property))
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((namespaceUri == null) ? 0 : namespaceUri.hashCode());
        result = prime * result + Arrays.hashCode(path);
        result = prime * result + ((property == null) ? 0 : property.hashCode());
        return result;
    }

    public String getNamespaceUri()
    {
        return namespaceUri;
    }

    public String[] getPath()
    {
        return path;
    }

    public String getProperty()
    {
        return property;
    }

}
