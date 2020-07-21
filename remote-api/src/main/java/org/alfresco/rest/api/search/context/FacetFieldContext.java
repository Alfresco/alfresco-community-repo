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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericBucket;

/**
 * The results of a Field Faceting
 */
public class FacetFieldContext
{
    private final String label;
    private final List<Bucket> buckets;

    public FacetFieldContext(String label, List<Bucket> buckets)
    {
        this.label = label;
        this.buckets = buckets;
    }

    public String getLabel()
    {
        return label;
    }

    public List<Bucket> getBuckets()
    {
        return buckets;
    }

    public static class Bucket
    {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private final String label;
        private final String filterQuery;
        private final int count;
        private final Object display;

        public Bucket(String label, String filterQuery, int count, Object display)
        {
            this.label = label;
            this.filterQuery = filterQuery;
            this.count = count;
            this.display = display;
        }

        public String getFilterQuery()
        {
            return filterQuery;
        }

        public Object getDisplay()
        {
            return display;
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

            Bucket bucket = (Bucket) o;

            if (count != bucket.count)
                return false;
            if (label != null ? !label.equals(bucket.label) : bucket.label != null)
                return false;
            if (filterQuery != null ? !filterQuery.equals(bucket.filterQuery) : bucket.filterQuery != null)
                return false;
            if (display != null ? !display.equals(bucket.display) : bucket.display != null)
                return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = label != null ? label.hashCode() : 0;
            result = 31 * result + (filterQuery != null ? filterQuery.hashCode() : 0);
            result = 31 * result + count;
            result = 31 * result + (display != null ? display.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return "Bucket{" +
                        "label='" + label + '\'' +
                        ", filterQuery='" + filterQuery + '\'' +
                        ", count=" + count +
                        ", display=" + display +
                        '}';
        }
    }
}
