/*
 * Copyright 2005 - 2020 Alfresco Software Limited.
 *
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of the paid license agreement will prevail.
 * Otherwise, the software is provided under the following open source license terms:
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
package org.alfresco.repo.web.scripts.search;

/**
 * Basic POJO to represent a term suggestion.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SearchSuggestionData
{
    private final String term;
    private final int weight;

    public SearchSuggestionData(String term, int weight)
    {
        this.term = term;
        this.weight = weight;
    }

    public String getTerm()
    {
        return this.term;
    }

    public int getWeight()
    {
        return this.weight;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(100);
        builder.append("SearchSuggestionData [term=").append(this.term).append(", weight=").append(this.weight)
                    .append("]");
        return builder.toString();
    }
}
