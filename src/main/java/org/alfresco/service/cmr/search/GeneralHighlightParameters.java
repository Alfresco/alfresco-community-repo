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

import java.util.List;

/**
 * Parameters used for search hightlighting that apply to all fields
 */

@AlfrescoPublicApi
public class GeneralHighlightParameters extends HighlightParameters
{
    private final Integer maxAnalyzedChars;
    private final Boolean usePhraseHighlighter;

    private final List<FieldHighlightParameters> fields;

    @JsonCreator
    public GeneralHighlightParameters(
                @JsonProperty("snippetCount") Integer snippetCount,
                @JsonProperty("fragmentSize") Integer fragmentSize,
                @JsonProperty("mergeContiguous") Boolean mergeContiguous,
                @JsonProperty("prefix") String prefix,
                @JsonProperty("postfix") String postfix,
                @JsonProperty("maxAnalyzedChars") Integer maxAnalyzedChars,
                @JsonProperty("usePhraseHighlighter") Boolean usePhraseHighlighter,
                @JsonProperty("fields") List<FieldHighlightParameters> fields)
    {
        super(snippetCount, fragmentSize, mergeContiguous, prefix, postfix);
        this.maxAnalyzedChars = maxAnalyzedChars;
        this.usePhraseHighlighter = usePhraseHighlighter;
        this.fields = fields;
    }

    @Override
    public String toString()
    {
        return "GeneralHighlightParameters{" +
                    "snippetCount=" + snippetCount +
                    ", fragmentSize=" + fragmentSize +
                    ", mergeContiguous=" + mergeContiguous +
                    ", prefix='" + prefix + '\'' +
                    ", postfix='" + postfix + '\'' +
                    ", maxAnalyzedChars=" + maxAnalyzedChars +
                    ", usePhraseHighlighter=" + usePhraseHighlighter +
                    ", fields=" + fields +
                    '}';
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

        GeneralHighlightParameters that = (GeneralHighlightParameters) o;

        if (getMaxAnalyzedChars() != null ? !getMaxAnalyzedChars().equals(that.getMaxAnalyzedChars()) : that.getMaxAnalyzedChars() != null)
            return false;
        if (getUsePhraseHighlighter() != null ?
                    !getUsePhraseHighlighter().equals(that.getUsePhraseHighlighter()) :
                    that.getUsePhraseHighlighter() != null)
            return false;
        if (getFields() != null ? !getFields().equals(that.getFields()) : that.getFields() != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (getMaxAnalyzedChars() != null ? getMaxAnalyzedChars().hashCode() : 0);
        result = 31 * result + (getUsePhraseHighlighter() != null ? getUsePhraseHighlighter().hashCode() : 0);
        result = 31 * result + (getFields() != null ? getFields().hashCode() : 0);
        return result;
    }

    public Integer getMaxAnalyzedChars()
    {
        return maxAnalyzedChars;
    }

    public Boolean getUsePhraseHighlighter()
    {
        return usePhraseHighlighter;
    }

    public List<FieldHighlightParameters> getFields()
    {
        return fields;
    }

}
