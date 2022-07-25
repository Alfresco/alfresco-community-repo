/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
/*
 * Copyright (C) 2018 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.rest.search;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.utility.model.TestModel;

/**
 * Search SQL Query object.
 * 
 * @author Meenal Bhave
 * 
 * Request POST
 * End point: /sql
 * PostBody:
 * {
 *   "stmt":"Select SITE from alfresco where SITE = 'swsdp' limit 2",
 *   "locales":["en-Uk"],
 *   "timezone":"Israel",
 *   "includeMetadata":true,
 *   "format":"solr",
 *   "limit":100,
 *   "filterQueries":["-ASPECT:ASPECT_CLASSIFIED"]
 * }
 */
public class SearchSqlRequest extends TestModel
{
    @JsonProperty(required = true)
    String stmt;

    String[] locales;
    String timezone;
    Boolean includeMetadata;
    String format;
    Integer limit;
    String[] filterQueries;

    public String getSql()
    {
        return stmt;
    }

    public void setSql(String sql)
    {
        this.stmt = sql;
    }

    public String[] getLocales()
    {
        return locales;
    }

    public void setLocales(String[] locales)
    {
        this.locales = locales;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }

    public Boolean getIncludeMetadata()
    {
        return includeMetadata;
    }

    public void setIncludeMetadata(Boolean includeMetadata)
    {
        this.includeMetadata = includeMetadata;
    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    public Integer getLimit()
    {
        return limit;
    }

    public void setLimit(Integer limit)
    {
        this.limit = limit;
    }

    public String[] getFilterQueries()
    {
        return filterQueries;
    }

    public void setFilterQuery(String[] filterQueries)
    {
        this.filterQueries = filterQueries;
    }

    public SearchSqlRequest()
    {
    }
}
