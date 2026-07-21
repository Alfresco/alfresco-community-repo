/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.rest.api.search;

import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * POST /search/versions/1/deep-search cursor-based deep pagination (Elasticsearch search_after). Same body as {@link SearchApiWebscript} plus a {@code searchAfter} cursor and the next cursor is returned in {@code list.context.nextCursor}.
 */
public class DeepSearchApiWebscript extends AbstractSearchApiWebscript
{
    @Override
    protected void applyPaginationContract(SearchParameters searchParams, SearchQuery searchQuery)
    {
        Paging paging = searchQuery.getPaging();
        if (paging != null && paging.getSkipCount() > 0)
        {
            throw new InvalidArgumentException(
                    "skipCount (offset paging) is not supported by deep-search; page using searchAfter instead.");
        }

        // No cursor means the first page keep it non-null so every page uses search_after with the same sort
        String searchAfter = searchQuery.getSearchAfter();
        searchParams.setSearchAfter(searchAfter == null ? "" : searchAfter);
    }
}
