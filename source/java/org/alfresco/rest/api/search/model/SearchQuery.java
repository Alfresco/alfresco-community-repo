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

import org.alfresco.rest.framework.resource.parameters.Paging;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * POJO class representing the JSON body for a search request
 *
 * @author Gethin James
 */
public class SearchQuery
{
    private final Query query;
    private final Paging paging;

    public static final SearchQuery EMPTY = new SearchQuery(null, null);

    @JsonCreator
    public SearchQuery(@JsonProperty("query") Query query,
                       @JsonProperty("paging") Paging paging)
    {
        this.query = query;
        this.paging = paging;
    }

    public Query getQuery()
    {
        return query;
    }

    public Paging getPaging()
    {
        return paging;
    }
}
