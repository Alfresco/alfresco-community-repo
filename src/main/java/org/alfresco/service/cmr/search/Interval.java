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

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Parameters used for a search Interval.
 */
public class Interval
{
    private final String field;
    private final String label;
    private final List<IntervalSet> sets;

    @JsonCreator
    public Interval(
                @JsonProperty("field") String field,
                @JsonProperty("label") String label,
                @JsonProperty("sets") List<IntervalSet> sets)
    {
        this.field = field;
        this.label = label;
        this.sets = sets == null ? new ArrayList<>() :sets;
    }

    public String getField()
    {
        return field;
    }

    public String getLabel()
    {
        return label;
    }

    public List<IntervalSet> getSets()
    {
        return sets;
    }

    @Override
    public String toString()
    {
        return "Interval{" +
                    "field='" + field + '\'' +
                    ", label='" + label + '\'' +
                    ", sets=" + sets +
                    '}';
    }
}
