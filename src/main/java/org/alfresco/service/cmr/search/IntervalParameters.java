/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.cmr.search;

import org.alfresco.api.AlfrescoPublicApi;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;
import java.util.Set;

/**
 * Parameters used for search Intervals.
 */
@AlfrescoPublicApi
public class IntervalParameters
{
    private final Set<IntervalSet> sets;
    private final List<Interval> intervals;

    @JsonCreator
    public IntervalParameters(
                @JsonProperty("sets") Set<IntervalSet> sets,
                @JsonProperty("intervals") List<Interval> intervals)
    {
        this.sets = sets;
        this.intervals = intervals;
    }

    public Set<IntervalSet> getSets()
    {
        return sets;
    }

    public List<Interval> getIntervals()
    {
        return intervals;
    }

    @Override
    public String toString()
    {
        return "IntervalParameters{" +
                    "sets=" + sets +
                    ", intervals=" + intervals +
                    '}';
    }
}
