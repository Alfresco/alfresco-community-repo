/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

import org.alfresco.query.PagingResults;
import org.alfresco.rest.framework.resource.SerializablePagedCollection;
import org.alfresco.util.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * List page with paging information.
 *
 * @param <E> - list element type
 */
public interface ListPage<E> extends List<E>, PagingResults<E>, SerializablePagedCollection<E>
{

    default List<E> getPage()
    {
        return this;
    }

    default Pair<Integer, Integer> getTotalResultCount()
    {
        return new Pair<>(this.getTotalItems(), this.getTotalItems());
    }

    default Collection<E> getCollection()
    {
        return this;
    }

    /**
     * Builds a collection with paging information.
     *
     * @param list - the list that needs to be paged
     * @param paging - paging request info
     * @return list page in {@link CollectionWithPagingInfo}
     * @param <E> - list element type
     */
    static <E> CollectionWithPagingInfo<E> of(final List<? extends E> list, final Paging paging)
    {
        if (list == null)
        {
            return CollectionWithPagingInfo.asPaged(paging, Collections.emptyList());
        }

        return CollectionWithPagingInfo.from(new ArrayListPage<>(list, paging));
    }
}
