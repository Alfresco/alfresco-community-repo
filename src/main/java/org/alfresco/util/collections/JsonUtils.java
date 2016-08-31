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

package org.alfresco.util.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class JsonUtils
{
    
    @SuppressWarnings("unchecked")
    public static <F, T> List<T> transform(JSONArray values, Function<F, ? extends T> transformer)
    {
        if(values == null || values.length()<1)
        {
           return Collections.emptyList(); 
        }
        ArrayList<T> results = new ArrayList<T>(values.length());
        for (int i = 0; i < values.length(); i++)
        {
            T result = transformer.apply((F)values.opt(i));
            if(result != null)
            {
                results.add(result);
            }
        }
        return results;
    }

    public static List<String> toListOfStrings(JSONArray values)
    {
        return transform(values, CollectionUtils.TO_STRING_TRANSFORMER);
    }
}
