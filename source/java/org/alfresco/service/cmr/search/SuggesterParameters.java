/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.service.cmr.search;

/**
 * This class defines suggester parameters
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SuggesterParameters
{
    private final String term;
    private final int limit;
    private final boolean termIsCaseSensitive;

    public SuggesterParameters(String term)
    {
        this(term, -1, false);
    }

    public SuggesterParameters(String term, int limit, boolean termIsCaseSensitive)
    {
        this.term = term;
        this.limit = limit;
        this.termIsCaseSensitive = termIsCaseSensitive;
    }

    /**
     * @return the term
     */
    public String getTerm()
    {
        return this.term;
    }

    /**
     * @return the limit
     */
    public int getLimit()
    {
        return this.limit;
    }

    /**
     * @return the termIsCaseSensitive
     */
    public boolean isTermIsCaseSensitive()
    {
        return this.termIsCaseSensitive;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(200);
        builder.append("SuggesterParameters [term=").append(this.term).append(", limit=").append(this.limit)
                    .append(", termIsCaseSensitive=").append(this.termIsCaseSensitive).append("]");
        return builder.toString();
    }
}
