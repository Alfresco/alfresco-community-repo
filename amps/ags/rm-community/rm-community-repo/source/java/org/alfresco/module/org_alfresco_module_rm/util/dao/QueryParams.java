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
package org.alfresco.module.org_alfresco_module_rm.util.dao;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.util.Pair;

/**
 * Options when listing something.
 *
 * @author Tom Page
 * @since 2.6
 */
@AlfrescoPublicApi
public class QueryParams<T extends QueryField>
{
    /** The ordered list of columns to sort on (and their sort direction). */
    private List<Pair<T, Boolean>> sortProps;
    /** The number of items to skip before creating the list. */
    private int skipCount = 0;
    /** The total number of items to return (assuming enough are available). */
    private int maxItems = 10;

    /**
     * Constructor that takes the sort order.
     *
     * @param sortProps A list of fields to sort on, and the direction to sort in.
     */
    public QueryParams(List<Pair<T, Boolean>> sortProps)
    {
        setSortProps(sortProps);
    }

    /** Sets the skip count required. */
    public QueryParams<T> withSkipCount(final int skipCount)
    {
        this.skipCount = skipCount;
        return this;
    }

    /** Sets the max items count required. */
    public QueryParams<T> withMaxItems(final int maxItems)
    {
        this.maxItems = maxItems;
        return this;
    }

    /**
     * Sets the sort properties required.
     *
     * @param sortProps A list of pairs of properties and sort directions.
     * @return The updated QueryParams object.
     */
    public QueryParams<T> withSortProps(List<Pair<T, Boolean>> sortProps)
    {
        this.setSortProps(sortProps);
        return this;
    }

    /**
     * Sets the sort properties required and validates the list.
     *
     * @param sortProps A list of pairs of properties and sort directions.
     */
    public void setSortProps(List<Pair<T, Boolean>> sortProps)
    {
        this.sortProps = ImmutableList.copyOf(sortProps);

        //validate the list
        for(Pair<T, Boolean> sortPair : sortProps)
        {
            if(sortPair == null || sortPair.getFirst() == null || sortPair.getSecond() == null)
            {
                throw new IllegalArgumentException("Unexpected null or null containing element in list: " + sortProps);
            }
        }
    }

    /** Get the ordered list of columns to sort on (and their sort direction). */
    public List<Pair<T, Boolean>> getSortProps()
    {
        return this.sortProps;
    }

    /** Get the number of items to skip before creating the list. */
    public int getSkipCount()
    {
        return this.skipCount;
    }

    /** Get the total number of items to return (assuming enough are available). */
    public int getMaxItems()
    {
        return this.maxItems;
    }
}
