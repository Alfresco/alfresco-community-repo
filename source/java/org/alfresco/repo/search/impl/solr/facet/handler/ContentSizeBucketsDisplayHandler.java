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

package org.alfresco.repo.search.impl.solr.facet.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import org.alfresco.repo.search.impl.solr.facet.FacetQueryProvider;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetConfigException;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * A simple handler to get the appropriate display label for the content size buckets.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class ContentSizeBucketsDisplayHandler extends AbstractFacetLabelDisplayHandler implements FacetQueryProvider
{
    private static final Pattern SIZE_RANGE_PATTERN = Pattern.compile("(\\[\\d+\\sTO\\s(\\d+|MAX)\\])");

    private final Map<String, FacetLabel> facetLabelMap;
    private final Map<String, List<String>> facetQueriesMap;

    public ContentSizeBucketsDisplayHandler(Set<String> facetQueryFields, LinkedHashMap<String, String> sizeBucketsMap)
    {
        ParameterCheck.mandatory("facetQueryFields", facetQueryFields);
        ParameterCheck.mandatory("sizeBucketsMap", sizeBucketsMap);

        this.supportedFieldFacets = Collections.unmodifiableSet(facetQueryFields);

        facetLabelMap = new HashMap<>(sizeBucketsMap.size());
        Map<String, List<String>> facetQueries = new LinkedHashMap<>(facetQueryFields.size());

        for (String facetQueryField : facetQueryFields)
        {
            List<String> queries = new ArrayList<>();
            int index = 0;
            for (Entry<String, String> bucket : sizeBucketsMap.entrySet())
            {
                String sizeRange = bucket.getKey().trim();
                Matcher matcher = SIZE_RANGE_PATTERN.matcher(sizeRange);
                if (!matcher.find())
                {
                    throw new SolrFacetConfigException(
                                "Invalid size range. Example of a valid size range is: [0 TO 1024]");
                }
                // build the facet query. e.g. {http://www.alfresco.org/model/content/1.0}content.size:[0 TO 1024]
                String facetQuery = facetQueryField + ':' + sizeRange;
                queries.add(facetQuery);

                // indexOf('[') => 1
                String sizeRangeQuery = sizeRange.substring(1, sizeRange.length() - 1);
                sizeRangeQuery = sizeRangeQuery.replaceFirst("\\sTO\\s", "\"..\"");
                facetLabelMap.put(facetQuery, new FacetLabel(sizeRangeQuery, bucket.getValue(), index++));
            }
            facetQueries.put(facetQueryField, queries);
        }
        this.facetQueriesMap = Collections.unmodifiableMap(facetQueries);
    }

    @Override
    public FacetLabel getDisplayLabel(String value)
    {
        FacetLabel facetLabel = facetLabelMap.get(value);
        return (facetLabel == null) ? new FacetLabel(value, value, -1) : facetLabel;
    }

    @Override
    public Map<String, List<String>> getFacetQueries()
    {
        return this.facetQueriesMap;
    }
}
