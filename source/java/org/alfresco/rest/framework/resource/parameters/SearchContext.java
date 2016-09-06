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
package org.alfresco.rest.framework.resource.parameters;

import java.util.List;
import java.util.Map;

/**
 * The contextual results of a Search
 */
public class SearchContext
{
    private final Consistency consistency;
    private final List<FacetQueryResult> facetQueries;

    public SearchContext(long lastTxId, List<FacetQueryResult> facetQueries)
    {
        if (lastTxId > 0)
        {
            consistency = new Consistency(lastTxId);
        }
        else
        {
            consistency = null;
        }
        this.facetQueries = facetQueries;
    }

    public Consistency getConsistency()
    {
        return consistency;
    }

    public List<FacetQueryResult> getFacetQueries()
    {
        return facetQueries;
    }

    public static class FacetQueryResult
    {
        private final String label;
        private final int count;

        public FacetQueryResult(String label, int count)
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

    public class Consistency
    {
        private final long lastTxId;

        public Consistency(long lastTxId)
        {
            this.lastTxId = lastTxId;
        }

        public long getlastTxId()
        {
            return lastTxId;
        }
    }
}
