/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.cmr.search;

import org.alfresco.api.AlfrescoPublicApi;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Parameters used for search hightlighting that are Field Specific
 */

@AlfrescoPublicApi
public class FieldHighlightParameters extends HighlightParameters
{
    private final String field;

    @JsonCreator
    public FieldHighlightParameters(
                @JsonProperty("field") String field,
                @JsonProperty("snippetCount") Integer snippetCount,
                @JsonProperty("fragmentSize") Integer fragmentSize,
                @JsonProperty("mergeContiguous") Boolean mergeContiguous,
                @JsonProperty("prefix") String prefix,
                @JsonProperty("postfix") String postfix)
    {
        super(snippetCount, fragmentSize, mergeContiguous, prefix, postfix);
        this.field = field;
    }

    @Override
    public String toString()
    {
        return "FieldHighlightParameters{" +
                    "snippetCount=" + snippetCount +
                    ", fragmentSize=" + fragmentSize +
                    ", mergeContiguous=" + mergeContiguous +
                    ", prefix='" + prefix + '\'' +
                    ", postfix='" + postfix + '\'' +
                    ", field='" + field + '\'' +
                    '}';
    }

    public String getField()
    {
        return field;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        FieldHighlightParameters that = (FieldHighlightParameters) o;

        if (field != null ? !field.equals(that.field) : that.field != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (field != null ? field.hashCode() : 0);
        return result;
    }
}
