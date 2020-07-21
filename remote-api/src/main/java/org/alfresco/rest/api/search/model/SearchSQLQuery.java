/*
 * #%L
 * Alfresco Repository
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

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO class representing the JSON body for a sql search request
 *
 * @author Michael Suzuki
 */
public class SearchSQLQuery
{
    private final String stmt;
    private Integer itemLimit;
    private final String format;
    private List<String> locales;
    private boolean includeMetadata;
    private String timezone;
    private List<String> filterQueries;
    
    public SearchSQLQuery(@JsonProperty("stmt") String stmt,
                          @JsonProperty("format") String format,
                          @JsonProperty("locales") List<String> locales,
                          @JsonProperty("limit") Integer itemLimit,
                          @JsonProperty("includeMetadata") boolean includeMetadata,
                          @JsonProperty("timezone") String timezone,
                          @JsonProperty("filterQueries") List<String> filterQueries)
    {
        this.stmt = stmt;
        this.format = format != null ? format : "default";
        this.locales = locales != null ? locales : Collections.emptyList();
        this.itemLimit = itemLimit == null || itemLimit < 1 ? new Integer(1000) : itemLimit;
        this.includeMetadata = includeMetadata;
        this.timezone = timezone;
        this.filterQueries = filterQueries != null ? filterQueries : Collections.emptyList();
    }

    public String getStmt()
    {
        return stmt;
    }

    public Integer getItemLimit()
    {
        return itemLimit;
    }

    public String getFormat()
    {
        return format;
    }

    public List<String> getLocales()
    {
        return locales;
    }

    public boolean isIncludeMetadata()
    {
        return includeMetadata;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public List<String> getFilterQueries()
    {
        return filterQueries;
    }
}
