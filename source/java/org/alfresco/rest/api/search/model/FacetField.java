/*-
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.search.model;

import org.alfresco.service.cmr.search.SearchParameters.FieldFacetMethod;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacetSort;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * POJO class representing the FacetField
 *
 * @author Gethin James
 */
public class FacetField
{
    private final String field;
    private final String label;
    private final String prefix;
    private final String sort; //actually an enum
    private final String method; //actually an enum
    private final Boolean missing;
    private final Integer limit;
    private final Integer offset;
    private final Integer mincount;
    private final Integer facetEnumCacheMinDf;

    @JsonCreator
    public FacetField(@JsonProperty("field") String field,
                @JsonProperty("label") String label,
                @JsonProperty("prefix") String prefix,
                @JsonProperty("sort") String sort,
                @JsonProperty("method") String method,
                @JsonProperty("missing") Boolean missing,
                @JsonProperty("limit") Integer limit,
                @JsonProperty("offset") Integer offset,
                @JsonProperty("mincount") Integer mincount,
                @JsonProperty("facetEnumCacheMinDf") Integer facetEnumCacheMinDf)
    {
        this.field = field;
        this.label = label;
        this.prefix = prefix;
        this.sort = sort;
        this.method = method;
        this.missing = missing == null?false:missing;
        this.limit = limit; //Can be null
        this.offset = offset == null?0:offset;
        this.mincount = mincount == null?0:mincount;
        this.facetEnumCacheMinDf = facetEnumCacheMinDf == null?0:facetEnumCacheMinDf;
    }
    /**

     "excludeFilters": [
                    "string"
                    ],

                    "contains": "string",
                    "containsIgnoreCase": true,
    **/

    public String getField()
    {
        return field;
    }

    public String getLabel()
    {
        return label;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public String getSort()
    {
        return sort;
    }

    public String getMethod()
    {
        return method;
    }

    public Boolean getMissing()
    {
        return missing;
    }

    public Integer getLimit()
    {
        return limit;
    }

    public Integer getOffset()
    {
        return offset;
    }

    public Integer getMincount()
    {
        return mincount;
    }

    public Integer getFacetEnumCacheMinDf()
    {
        return facetEnumCacheMinDf;
    }
}
