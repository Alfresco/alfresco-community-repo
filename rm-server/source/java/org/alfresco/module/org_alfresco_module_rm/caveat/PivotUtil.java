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
package org.alfresco.module.org_alfresco_module_rm.caveat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* package scope */ class PivotUtil
{
    static Map<String, List<String>> getPivot(Map<String, List<String>> source)
    {
    
        Map<String, List<String>> pivot = new HashMap<String, List<String>>();
    
        for(String authority : source.keySet())
        {
            List<String>values = source.get(authority);
            for(String value : values)
            {
                if(pivot.containsKey(value))
                {
                    // already exists
                    List<String> list = pivot.get(value);
                    list.add(authority);
                }
                else
                {
                    // New value
                    List<String> list = new ArrayList<String>();
                    list.add(authority);
                    pivot.put(value, list);
                }
            }
        }
        return pivot;
    }
}
