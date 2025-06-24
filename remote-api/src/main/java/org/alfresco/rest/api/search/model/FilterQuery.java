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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO class representing a FilterQuery
 *
 * @author Gethin James
 */
public class FilterQuery
{
    private final List<String> queries;
    private final String query;
    private final List<String> tags;

    @JsonCreator
    public FilterQuery(@JsonProperty("query") String query, @JsonProperty("tags") List<String> tags, @JsonProperty("queries") List<String> queries)
    {
        this.query = query;
        this.tags = tags;
        this.queries = queries;
    }

    public List<String> getQueries()
    {
        return queries;
    }

    public String getQuery()
    {
        return query;
    }

    public List<String> getTags()
    {
        return tags;
    }
}
