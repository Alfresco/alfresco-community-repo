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
package org.alfresco.rest.api.search.context;

/**
 * The results of a facet query
 */
public class FacetQueryContext
{
    private final String label;
    private final String filterQuery;
    private final int count;

    public FacetQueryContext(String label, String filterQuery, int count)
    {
        this.label = label;
        this.filterQuery = filterQuery;
        this.count = count;
    }

    public String getFilterQuery()
    {
        return filterQuery;
    }

    public String getLabel()
    {
        return label;
    }

    public int getCount()
    {
        return count;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FacetQueryContext that = (FacetQueryContext) o;

        if (count != that.count)
            return false;
        if (label != null ? !label.equals(that.label) : that.label != null)
            return false;
        if (filterQuery != null ? !filterQuery.equals(that.filterQuery) : that.filterQuery != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (filterQuery != null ? filterQuery.hashCode() : 0);
        result = 31 * result + count;
        return result;
    }

    @Override
    public String toString()
    {
        return "FacetQueryContext{" +
                    "label='" + label + '\'' +
                    ", filterQuery='" + filterQuery + '\'' +
                    ", count=" + count +
                    '}';
    }
}
