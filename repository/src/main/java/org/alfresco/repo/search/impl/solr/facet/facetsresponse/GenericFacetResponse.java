/*-
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.solr.facet.facetsresponse;

import java.util.List;

/**
 * A Generic facet response covering range, interval, pivot etc.
 */
public class GenericFacetResponse
{
    public static enum FACET_TYPE {query, field, range, interval, pivot, stats};
    private final FACET_TYPE type;
    private final String label;
    private final List<GenericBucket> buckets;

    public static final String LABEL = "label";
    public static final String COUNT = "count";
    public static final String START = "start";
    public static final String END = "end";
    public static final String START_INC = "startInclusive";
    public static final String END_INC = "endInclusive";

    public GenericFacetResponse(FACET_TYPE type, String label, List<GenericBucket> buckets)
    {
        this.type = type;
        this.label = label;
        this.buckets = buckets;
    }

    public String getLabel()
    {
        return label;
    }

    public List<GenericBucket> getBuckets()
    {
        return buckets;
    }

    public FACET_TYPE getType()
    {
        return type;
    }

}
