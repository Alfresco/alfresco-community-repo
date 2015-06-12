/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.classification;

import static java.util.Arrays.asList;
import static org.alfresco.model.ContentModel.PROP_FIRSTNAME;
import static org.alfresco.model.ContentModel.PROP_LASTNAME;
import static org.alfresco.model.ContentModel.PROP_USERNAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;

/**
 * Configurable options to be used when querying for users by {@link SecurityClearance}.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public final class UserQueryParams
{
    /** Required parameter. No default value. This is the username fragment. */
    private final String searchTerm;

    // These configurable parameters have default values.
    private List<QName>                filterProps = asList(PROP_USERNAME, PROP_FIRSTNAME, PROP_LASTNAME);
    private List<Pair<QName, Boolean>> sortProps   = asList(new Pair<>(PROP_USERNAME, true));
    private int                        skipCount = 0;
    private int                        maxItems  = 10;

    /**
     * Create a new object for searching for people.
     *
     * @param searchTerm The unescaped string to search for in the people service.
     */
    public UserQueryParams(final String searchTerm)
    {
        ParameterCheck.mandatory("searchTerm", searchTerm);
        // Escape backslashes before using in the query. (The person service does not do this for us)
        this.searchTerm = searchTerm.replace("\\", "\\\\");
    }

    /** Sets the skip count required for the query. */
    public UserQueryParams withSkipCount(final int skipCount)
    {
        this.skipCount = skipCount;
        return this;
    }

    /** Sets the max items count required for the query. */
    public UserQueryParams withMaxItems(final int maxItems)
    {
        this.maxItems = maxItems;
        return this;
    }

    /** Sets the filter properties required for the query. */
    public UserQueryParams withFilterProps(QName firstFilterProp, QName... otherFilterProps)
    {
        this.filterProps = Collections.unmodifiableList(toList(firstFilterProp, otherFilterProps));
        return this;
    }

    /** Sets the sort properties required for the query. */
    @SuppressWarnings("unchecked")
    public UserQueryParams withSortProps(Pair<QName, Boolean> firstSortProp, Pair<QName, Boolean>... otherSortProps)
    {
        this.sortProps = Collections.unmodifiableList(toList(firstSortProp, otherSortProps));
        return this;
    }

    public String                     getSearchTerm()  { return this.searchTerm; }
    public List<QName>                getFilterProps() { return this.filterProps; }
    public List<Pair<QName, Boolean>> getSortProps()   { return this.sortProps; }
    public int                        getSkipCount()   { return this.skipCount; }
    public int                        getMaxItems()    { return this.maxItems; }

    /** Helper method to turn a varargs into a List, ensuring at least one element is present. */
    @SuppressWarnings("unchecked")
    private <T> List<T> toList(T firstElem, T... otherElems)
    {
        // At least one element is required.
        ParameterCheck.mandatory("firstElem", firstElem);

        List<T> elementList = new ArrayList<>();
        elementList.add(firstElem);

        if (otherElems != null)
        {
            final List<T> tList = asList(otherElems);
            final int firstNull = tList.indexOf(null);
            if (firstNull != -1)
            {
                // "+ 2" so that position 1 points to 'firstElem' and so on through otherElems.
                throw new IllegalArgumentException("Unexpected null element at position " + firstNull + 2);
            }
            elementList.addAll(tList);
        }

        return elementList;
    }
}
