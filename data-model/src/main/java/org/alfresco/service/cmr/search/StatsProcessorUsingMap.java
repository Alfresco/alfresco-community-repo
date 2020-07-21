/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.search;

import java.util.Map;

/**
 * Post-Processors the results of a Stats query using a Map of values.
 * Looks up the value by Map.key and replaces it with Map.value
 * 
 * If its not found then returns the existing value.
 *
 * @author Gethin James
 * @since 5.0
 */
public class StatsProcessorUsingMap implements StatsProcessor
{
    Map<String, String> mapping;
    
    public StatsProcessorUsingMap()
    {
        super();
    }
    
    public StatsProcessorUsingMap(Map<String, String> mapping)
    {
        super();
        this.mapping = mapping;
    }

    @Override
    public StatsResultSet process(StatsResultSet input)
    {
        if (input == null || input.getStats() == null){ return null; }
        
        for (StatsResultStat aStat : input.getStats())
        {
            String processed = mapping.get(aStat.getName());
            if (processed != null) { aStat.setName(processed); }
        }
        return input;
    }

    public void setMapping(Map<String, String> mapping)
    {
        this.mapping = mapping;
    }


}
