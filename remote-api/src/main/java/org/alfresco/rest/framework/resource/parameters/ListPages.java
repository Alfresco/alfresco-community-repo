/*
 * #%L
 * Alfresco Remote API
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.framework.resource.parameters;

import org.alfresco.query.ListBackedPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.rest.api.impl.Util;
import org.alfresco.rest.api.search.context.SearchContext;
import org.alfresco.service.Experimental;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Experimental
public class ListPages<T> extends CollectionWithPagingInfo<T>
{
    /**
     * Constructs a new CollectionWithPagingInfo.
     *
     * @param collection   - The collection that needs to be paged
     * @param paging       - Paging request info
     * @param hasMoreItems - Are there more items after this Collection?
     * @param totalItems   - The total number of items available
     * @param sourceEntity - The parent/source entity responsible for the collection
     * @param context      - The search context
     */
    protected ListPages(Collection<T> collection, Paging paging, boolean hasMoreItems, Integer totalItems, Object sourceEntity, SearchContext context)
    {
        super(collection, paging, hasMoreItems, totalItems, sourceEntity, context);
    }

    /**
     * Constructs a new CollectionWithPagingInfo.
     *
     * @param list - The list that needs to be paged
     * @param paging - Paging request info
     * @return ListPages which extends {@link CollectionWithPagingInfo}
     * @param <T> List element type
     * @param <L> List type
     */
    public static <T, L extends List<T>> ListPages<T> createPage(final L list, final Paging paging)
    {
        if (list == null) {
            return new ListPages<>(Collections.emptyList(), paging, false, 0, null, null);
        }

        final PagingRequest pagingRequest = Util.getPagingRequest(paging);
        final PagingResults<T> pagingResults = new ListBackedPagingResults<>(list, pagingRequest);

        return new ListPages<>(pagingResults.getPage(), paging, pagingResults.hasMoreItems(), pagingResults.getTotalResultCount().getFirst(), null, null);
    }
}
