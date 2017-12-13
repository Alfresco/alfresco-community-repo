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
 * Parameters used for search hightlighting.
 */

@AlfrescoPublicApi
public abstract class HighlightParameters
{
    final Integer snippetCount;
    final Integer fragmentSize;

    final Boolean mergeContiguous;

    final String prefix;
    final String postfix;

    public HighlightParameters(Integer snippetCount, Integer fragmentSize,
                               Boolean mergeContiguous, String prefix, String postfix)
    {
        this.snippetCount = snippetCount;
        this.fragmentSize = fragmentSize;
        this.mergeContiguous = mergeContiguous;
        this.prefix = prefix;
        this.postfix = postfix;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        HighlightParameters that = (HighlightParameters) o;

        if (snippetCount != null ? !snippetCount.equals(that.snippetCount) : that.snippetCount != null)
            return false;
        if (fragmentSize != null ? !fragmentSize.equals(that.fragmentSize) : that.fragmentSize != null)
            return false;
        if (mergeContiguous != null ? !mergeContiguous.equals(that.mergeContiguous) : that.mergeContiguous != null)
            return false;
        if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null)
            return false;
        if (postfix != null ? !postfix.equals(that.postfix) : that.postfix != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = snippetCount != null ? snippetCount.hashCode() : 0;
        result = 31 * result + (fragmentSize != null ? fragmentSize.hashCode() : 0);
        result = 31 * result + (mergeContiguous != null ? mergeContiguous.hashCode() : 0);
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        result = 31 * result + (postfix != null ? postfix.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "HighlightParameters{" +
                    "snippetCount=" + snippetCount +
                    ", fragmentSize=" + fragmentSize +
                    ", mergeContiguous=" + mergeContiguous +
                    ", prefix='" + prefix + '\'' +
                    ", postfix='" + postfix + '\'' +
                    '}';
    }

    public Integer getSnippetCount()
    {
        return snippetCount;
    }

    public Integer getFragmentSize()
    {
        return fragmentSize;
    }

    public Boolean getMergeContiguous()
    {
        return mergeContiguous;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public String getPostfix()
    {
        return postfix;
    }
}
