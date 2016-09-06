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

/**
 * The results of a Field Facetting
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
        private final String label;
        private final int count;

        public Bucket(String label, int count)
        {
            this.label = label;
            this.count = count;
        }

        public String getLabel()
        {
            return label;
        }

        public int getCount()
        {
            return count;
        }
    }
}
