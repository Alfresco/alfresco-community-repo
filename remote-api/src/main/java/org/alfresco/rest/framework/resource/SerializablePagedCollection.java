/*
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

package org.alfresco.rest.framework.resource;

import org.alfresco.rest.api.search.context.SearchContext;
import org.alfresco.rest.framework.resource.parameters.Paging;

import java.util.Collection;

/**
 * A specialist representation of a Collection that can be serialized to json with paging information
 *
 * @author Gethin James.
 */
public interface SerializablePagedCollection<T>
{
    /**
     * Returns the Collection object
     * @return Collection
     */
    Collection<T> getCollection();

    /**
     * Indicates if the returned collection has more items after the current returned list.
     */
    boolean hasMoreItems();

    /**
     * Indicates the total number of items available.
     *
     * Can be greater than the number of items returned in the list.
     *
     */
    Integer getTotalItems();

    /**
     * The parent/source entity responsible for the collection
     */
    Object getSourceEntity();

    /**
     * The requested paging parameters set by the client
     */
    Paging getPaging();

    /**
     * The search context for the collection
     */
    SearchContext getContext();
}
