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

import java.util.ArrayList;
import java.util.Collections;
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
    private final String field;
    private final String start;
    private final String end;
    private final String gap;
    private final boolean hardend;
    private final List<String> other;
    private final List<String> include;
    private final String label;
    private final List<String> excludeFilters;
    
    /**
     * Constructor.
     * @param field
     * @param minCount
     * @param start
     * @param end
     * @param gap
     * @param hardend
     * @param other can have one of the following values: before,after,between,none
     * @param include can have one of the following values: lower,upper,edge,outer,all
     * @param label
     * @param excludeFilters
     */
    @JsonCreator
    public RangeParameters(@JsonProperty("field") String field,
                           @JsonProperty("start") String start, 
                           @JsonProperty("end") String end, 
                           @JsonProperty("gap") String gap,
                           @JsonProperty("hardend") boolean hardend, 
                           @JsonProperty("other")List<String> other,
                           @JsonProperty("include")List<String> include,
                           @JsonProperty("label") String label,
                           @JsonProperty("excludeFilters")List<String> excludeFilters)
    {
        super();
        this.field = field;
        this.start = start;
        this.end = end;
        this.gap = gap;
        this.hardend = hardend;
        this.other = other == null? Collections.emptyList():other;
        this.include = include == null? Collections.emptyList():include;
        this.label = label;
        this.excludeFilters = excludeFilters == null? Collections.emptyList():excludeFilters;
    }
    
    public String getField()
    {
        return field;
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
    public List<String> getOther()
    {
        return other;
    }
    public List<String> getInclude() 
    {
        return include;
    }
    public String getLabel()
    {
        return label;
    }

    public List<String> getExcludeFilters() 
    {
        return excludeFilters;
    }
    
    public String getRangeFirstBucketStartInclusive()
    {
        if(include != null && !include.isEmpty())
        {
            for(String startInc : include)
            {
                switch (startInc)
                {
                    case "before":
                        break;
                    case "edge":
                        return  "[";
                    case "upper":
                        return  "]";
                    case "lower":
                        break;
                    default:
                }
            }
        }
        return "[";
    }
    public String getRangeFirstBucketEndInclusive()
    {
        if(include != null && !include.isEmpty())
        {
            for(String startInc : include)
            {
                switch (startInc)
                {
                    case "before":
                        break;
                    case "edge":
                        return  "<";
                    case "outer":
                        return  "]";
                    case "lower":
                        return  "<";
                    case "upper":
                        return ">";
                    default:
                }
            }
        }
        return ">";
    }
    
    public boolean isRangeStartInclusive()
    {
        List<String> options = new ArrayList<String>();
        if(include != null && !include.isEmpty())
        {
            options.addAll(include);
        }
        if(other != null && !other.isEmpty())
        {
            options.addAll(other);
        }
        if(!options.isEmpty())
        {
            for(String startInc : options)
            {
                switch (startInc)
                {
                    case "before":
                        return  false;
                    case "upper":
                        return  false;
                    case "edge":
                        return  false;
                    default:
                        break;
                }
            }
        }
        return true;
    }
    
    public boolean isRangeEndInclusive()
    {
        List<String> options = new ArrayList<String>();
        if(include != null && !include.isEmpty())
        {
            options.addAll(include);
        }
        if(other != null && !other.isEmpty())
        {
            options.addAll(other);
        }
        if(!options.isEmpty())
        {
            for(String endInc : options)
            {
                switch (endInc)
                {
                case "edge":
                    return  true;
                case "upper":
                    return  true;
                case "all":
                    return  true;
                default:
                    break;
                }
            }
        }
        return false;
    }

    public String getRangeBucketStartInclusive()
    {
        if(include != null && !include.isEmpty())
        {
            for(String key : include)
            {
                switch (key)
                {
                case "lower":
                    return "[";
                default:
                    break;
                }
            }
        }
        return "]";
    }
    public String getRangeBucketEndInclusive()
    {
        if(include != null && !include.isEmpty())
        {
            for(String key : include)
            {
                switch (key)
                {
                case "upper":
                    return ">";
                default:
                    break;
                }
            }
        }
        return "<";
    }

    public String getRangeLastBucketStartInclusive()
    {
        if(include != null && !include.isEmpty())
        {
            for(String key : include)
            {
                switch (key)
                {
                    case "lower":
                        return "[";
                    case "edge":
                        return "]";
                    case "upper":
                        return "]";
                    default:
                        break;
                }
            }
        }
        return "<";
    }

    public String getRangeLastBucketEndInclusive()
    {
        if(include != null && !include.isEmpty())
        {
            for(String key : include)
            {
                switch (key)
                {
                    case "lower":
                        return ">";
                    case "edge":
                        return "]";
                    case "upper":
                        return "]";
                    default:
                        break;
                }
            }
        }
        return "<";
    }
}
