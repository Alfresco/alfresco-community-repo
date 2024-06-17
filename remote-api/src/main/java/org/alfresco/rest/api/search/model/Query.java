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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO class representing the query element of the JSON body
 **/
public class Query implements Serializable
{
    private static final long serialVersionUID = 8443756988747629114L;
    private final String language;
    private final String query;
    private final String userQuery;

    @JsonCreator
    public Query(@JsonProperty("language")  String language,
                 @JsonProperty("query")     String query,
                 @JsonProperty("userQuery") String userQuery)
    {
        this.language = language;
        this.query = query;
        this.userQuery = userQuery;
    }

    public String getLanguage()
    {
        return language;
    }

    public String getQuery()
    {
        return query;
    }

    public String getUserQuery()
    {
        return userQuery;
    }

}
