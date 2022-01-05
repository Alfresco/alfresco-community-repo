/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.core.search;

import java.util.List;

import org.alfresco.rest.search.Pagination;
import org.alfresco.rest.search.RestRequestQueryModel;
import org.alfresco.rest.search.SearchRequest;

/**
 * Builder class for creating a search api request
 */
public class SearchRequestBuilder extends SearchRequest
{
    /**
     * Constructor for Search API Request
     */
    public SearchRequestBuilder()
    {
        new SearchRequest();
    }
    /**
     * Set the sql statement for the SearchRequest
     *
     * @param query sql statement
     * @return search  request
     */
    public SearchRequestBuilder setQueryBuilder(RestRequestQueryModel query)
    {
        super.setQuery(query);
        return this;
    }

    /**
     * Set the paging statement for the SearchRequest
     *
     * @param paging pagination requested
     * @return search request
     */
    public SearchRequestBuilder setPagingBuilder(Pagination paging)
    {
        super.setPaging(paging);
        return this;
    }

    /**
     * Set the pagination properties
     */
    public Pagination setPagination(Integer maxItems, Integer skipCount)
    {
        Pagination pagination = new Pagination();
        pagination.setMaxItems(maxItems);
        pagination.setSkipCount(skipCount);
        return pagination;
    }

    /**
     * Set the requested fields for the SearchRequest
     *
     * @param fields requested fields
     * @return search request
     */
    public SearchRequestBuilder setFieldsBuilder(List<String> fields)
    {
        super.setFields(fields);
        return this;
    }

}
