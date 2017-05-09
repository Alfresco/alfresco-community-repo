/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Parameters used for search range.
 */
@AlfrescoPublicApi
public class RangeParameters
{
    private String field;
    private int minCount;
    private String start;
    private String end;
    private String gap;
    private boolean hardend;
    private String other;
    private String include;
    private List<String> tags;
    private List<String> excludeFilters;

    public RangeParameters() {}
    
    @JsonCreator
    public RangeParameters( @JsonProperty("field") String field,
                            @JsonProperty("facet.range.start") String start, 
                            @JsonProperty("facet.range.end") String end, 
                            @JsonProperty("facet.range.gap") String gap)
    {
        super();
        this.field = field;
        this.start = start;
        this.end = end;
        this.gap = gap;
        this.hardend = false;
    }
    /**
     * Constructor.
     * @param field
     * @param minCount
     * @param start
     * @param end
     * @param gap
     * @param hardend
     * @param other
     * @param include
     * @param tags
     * @param excludeFilters
     */
    public RangeParameters(String field,
                           int minCount,
                           String start, 
                           String end, 
                           String gap, 
                           boolean hardend, 
                           String other,
                           String include, 
                           List<String> tags, 
                           List<String> excludeFilters)
    {
        super();
        this.field = field;
        this.minCount = minCount;
        this.start = start;
        this.end = end;
        this.gap = gap;
        this.hardend = hardend;
        this.other = other;
        this.include = include;
        this.tags = tags;
        this.excludeFilters = excludeFilters;
    }
    
    public String getField()
    {
        return field;
    }
    public int getMinCount() 
    {
        return minCount;
    }
    public String getStart()
    {
        return start;
    }
    public String getEnd() 
    {
        return end;
    }
    public String getGap() 
    {
        return gap;
    }
    public boolean isHardend() 
    {
        return hardend;
    }
    public String getOther()
    {
        return other;
    }
    public String getInclude() 
    {
        return include;
    }
    public List<String> getTags() 
    {
        return tags;
    }
    public List<String> getExcludeFilters() 
    {
        return excludeFilters;
    }
}
