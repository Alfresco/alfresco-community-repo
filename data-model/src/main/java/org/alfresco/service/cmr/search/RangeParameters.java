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

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.api.AlfrescoPublicApi;

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
     * 
     * @param field
     * @param minCount
     * @param start
     * @param end
     * @param gap
     * @param hardend
     * @param other
     *            can have one of the following values: before,after,between,none
     * @param include
     *            can have one of the following values: lower,upper,edge,outer,all
     * @param label
     * @param excludeFilters
     */
    @JsonCreator
    public RangeParameters(@JsonProperty("field") String field,
            @JsonProperty("start") String start,
            @JsonProperty("end") String end,
            @JsonProperty("gap") String gap,
            @JsonProperty("hardend") boolean hardend,
            @JsonProperty("other") List<String> other,
            @JsonProperty("include") List<String> include,
            @JsonProperty("label") String label,
            @JsonProperty("excludeFilters") List<String> excludeFilters)
    {
        super();
        this.field = field;
        this.start = start;
        this.end = end;
        this.gap = gap;
        this.hardend = hardend;
        this.other = other == null ? Collections.emptyList() : other;
        this.include = include == null ? Collections.emptyList() : include;
        this.label = label;
        this.excludeFilters = excludeFilters == null ? Collections.emptyList() : excludeFilters;
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
        if (include != null && !include.isEmpty())
        {
            for (String startInc : include)
            {
                switch (startInc)
                {
                case "before":
                    break;
                case "edge":
                    return "[";
                case "lower":
                    return "[";
                default:
                }
            }
            return "<";
        }
        return "[";
    }

    public String getRangeFirstBucketEndInclusive()
    {
        if (include != null && !include.isEmpty())
        {
            for (String startInc : include)
            {
                switch (startInc)
                {
                case "upper":
                    return "]";
                case "outer":
                    if (other != null && (other.contains("before") || other.contains("after")))
                    {
                        return "]";
                    }
                    break;
                default:
                    break;
                }
            }
        }
        return ">";
    }

    public String getRangeBucketStartInclusive()
    {
        if (include != null && !include.isEmpty())
        {
            for (String key : include)
            {
                switch (key)
                {
                case "lower":
                    return "[";
                default:
                    break;
                }
            }
            return "<";
        }
        return "[";
    }

    public String getRangeBucketEndInclusive()
    {
        if (include != null && !include.isEmpty())
        {
            for (String key : include)
            {
                switch (key)
                {
                case "upper":
                    return "]";
                default:
                    break;
                }
            }
        }
        return ">";
    }

    public String getRangeLastBucketEndInclusive()
    {
        if (include != null && !include.isEmpty())
        {
            for (String key : include)
            {
                switch (key)
                {
                case "edge":
                    return "]";
                case "upper":
                    return "]";
                default:
                    break;
                }
            }
            return ">";
        }
        return "]";
    }
}
