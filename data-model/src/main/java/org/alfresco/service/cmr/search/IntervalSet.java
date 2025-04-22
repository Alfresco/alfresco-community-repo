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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Parameters used for search Interval sets.
 */
@AlfrescoPublicApi
public class IntervalSet
{
    private final String start;
    private final String end;
    private String label;
    private final boolean startInclusive;
    private final boolean endInclusive;

    @JsonCreator
    public IntervalSet(
            @JsonProperty("start") String start,
            @JsonProperty("end") String end,
            @JsonProperty("label") String label,
            @JsonProperty("startInclusive") Boolean startInclusive,
            @JsonProperty("endInclusive") Boolean endInclusive)
    {
        this.start = start;
        this.end = end;
        this.label = label;
        this.startInclusive = startInclusive == null ? true : startInclusive;
        this.endInclusive = endInclusive == null ? true : endInclusive;
    }

    public String getStart()
    {
        return start;
    }

    public String getEnd()
    {
        return end;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public boolean isStartInclusive()
    {
        return startInclusive;
    }

    public boolean isEndInclusive()
    {
        return endInclusive;
    }

    public String toParam()
    {
        StringBuilder sb = new StringBuilder("{!afts");
        if (label != null && !label.isEmpty())
        {
            sb.append(" key=" + label);
        }
        sb.append("}")
                .append(toRange());
        return sb.toString();
    }

    public String toRange()
    {
        StringBuilder sb = new StringBuilder("");
        sb.append(startInclusive ? "[" : "(")
                .append(start)
                .append("," + end)
                .append(endInclusive ? "]" : ")");
        return sb.toString();
    }

    /**
     * Returns a valid AFTS query for this Interval Set
     * 
     * @return a query
     */
    public String toAFTSQuery()
    {
        StringBuilder sb = new StringBuilder("");
        sb.append(startInclusive ? "[" : "<")
                .append("\"").append(start).append("\"")
                .append(" TO ")
                .append("\"").append(end).append("\"")
                .append(endInclusive ? "]" : ">");
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return "IntervalSet{" +
                "start=" + start +
                ", end=" + end +
                ", label='" + label + '\'' +
                ", startInclusive=" + startInclusive +
                ", endInclusive=" + endInclusive +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        IntervalSet that = (IntervalSet) o;

        if (startInclusive != that.startInclusive)
            return false;
        if (endInclusive != that.endInclusive)
            return false;
        if (start != null ? !start.equals(that.start) : that.start != null)
            return false;
        if (end != null ? !end.equals(that.end) : that.end != null)
            return false;
        if (label != null ? !label.equals(that.label) : that.label != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        result = 31 * result + (startInclusive ? 1 : 0);
        result = 31 * result + (endInclusive ? 1 : 0);
        return result;
    }
}
