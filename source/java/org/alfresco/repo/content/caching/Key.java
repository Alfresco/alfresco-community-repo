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
package org.alfresco.repo.content.caching;

import java.io.File;
import java.io.Serializable;

/**  
 * Multipurpose key so that data can be cached either by content URL or cache file path.
 * 
 * @author Matt Ward
 */
public class Key implements Serializable
{
    private static final long serialVersionUID = 1L;
    private enum Type { CONTENT_URL, CACHE_FILE_PATH };
    private final Type type;
    private final String value;
    
    private Key(Type type, String value)
    {
        this.type = type;
        this.value = value;
    }
    
    public static Key forUrl(String url)
    {
        return new Key(Type.CONTENT_URL, url);
    }
    
    public static Key forCacheFile(String path)
    {
        return new Key(Type.CACHE_FILE_PATH, path);
    }
    
    public static Key forCacheFile(File file)
    {
        return forCacheFile(file.getAbsolutePath());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Key other = (Key) obj;
        if (this.type != other.type) return false;
        if (this.value == null)
        {
            if (other.value != null) return false;
        }
        else if (!this.value.equals(other.value)) return false;
        return true;
    }
}
