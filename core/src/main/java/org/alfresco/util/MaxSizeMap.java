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
package org.alfresco.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map that ejects the last recently accessed or inserted element(s) to keep the size to a specified maximum.
 * 
 * @param <K>
 *            Key
 * @param <V>
 *            Value
 */
public class MaxSizeMap<K, V> extends LinkedHashMap<K, V>
{
    private static final long serialVersionUID = 3753219027867262507L;

    private final int maxSize;

    /**
     * @param maxSize maximum size of the map.
     * @param accessOrder <tt>true</tt> for access-order, <tt>false</tt> for insertion-order.
     */
    public MaxSizeMap(int maxSize, boolean accessOrder)
    {
        super(maxSize * 2, 0.75f, accessOrder);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
    {
        return super.size() > this.maxSize;
    }
}
