/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.resultset;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.DoubleTermsBucket;
import org.opensearch.client.opensearch._types.aggregations.LongTermsBucket;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch._types.aggregations.TermsBucketBase;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.util.CollectionUtils;

import org.alfresco.util.Pair;

class AggregationHandler
{
    Aggregation handle(SearchResponse<?> searchResponse, Map<String, String> bucketsTranslator, Map<String, Pair<String, String>> complementaryBucketsTranslator)
    {
        Map<String, List<Pair<String, Integer>>> fieldFacets = new HashMap<>();
        Map<String, Integer> facetQueries = new HashMap<>();
        if (!CollectionUtils.isEmpty(searchResponse.aggregations()))
        {
            Map<String, Aggregate> responseAggregations = searchResponse.aggregations();
            Set<String> complementaryBucketKeys = complementaryBucketsTranslator.keySet();

            responseAggregations
                    .entrySet()
                    .forEach(aggregation -> {
                        String aggregationKey = aggregation.getKey();
                        if (complementaryBucketKeys.contains(aggregationKey))
                        {
                            return; // handled below as a virtual bucket
                        }
                        if (aggregation.getValue()._kind().jsonValue().equals("filter"))
                        {
                            ofNullable(aggregation.getValue())
                                    .map(Aggregate.class::cast)
                                    .ifPresent(filters -> facetQueries.put(AggregationNameUtil.decode(aggregation.getKey()), (int) filters.filter()
                                            .docCount()));
                        }
                        else
                        {
                            List<? extends TermsBucketBase> buckets = getBuckets(aggregation.getValue());
                            fieldFacets.put(AggregationNameUtil.decode(aggregation.getKey()), new ArrayList<>(buckets.stream()
                                    .map(bucket -> new Pair<>(translateBucket(bucketsTranslator, extractBucketKey(bucket)), (int) bucket.docCount()))
                                    .toList()));
                        }
                    });
            addComplementaryBuckets(responseAggregations, complementaryBucketsTranslator, fieldFacets);
        }
        return new Aggregation(facetQueries, fieldFacets);
    }

    /**
     * Translates a bucket key using {@code bucketsTranslator} (e.g. a site node UUID to its short name or {@code _SHARED_FILES_}). Keys not present in the map are returned unchanged.
     */
    private String translateBucket(Map<String, String> bucketsTranslator, String key)
    {
        return bucketsTranslator.getOrDefault(key, key);
    }

    /**
     * Adds complementary aggregation results as virtual buckets in the owning terms facet
     */
    private void addComplementaryBuckets(Map<String, Aggregate> responseAggregations, Map<String, Pair<String, String>> complementaryBucketsTranslator, Map<String, List<Pair<String, Integer>>> fieldFacets)
    {
        // Inject complementary aggregation results as virtual buckets in the owning terms facet.
        // Skip buckets with docCount == 0 to match SOLR's default mincount=1 behaviour.
        complementaryBucketsTranslator.forEach((complementaryName, pair) -> {
            Aggregate agg = responseAggregations.get(complementaryName);
            if (agg != null && agg.isFilter())
            {
                long docCount = agg.filter().docCount();
                if (docCount > 0)
                {
                    String termsAggName = pair.getFirst();
                    String displayLabel = pair.getSecond();
                    fieldFacets.computeIfAbsent(termsAggName, k -> new ArrayList<>()).add(new Pair<>(displayLabel, (int) docCount));
                }
            }
        });
    }

    private List<? extends TermsBucketBase> getBuckets(Aggregate agg)
    {
        return ofNullable(agg)
                .map(a -> {
                    if (a.isSterms())
                    {
                        return a.sterms().buckets().array();
                    }
                    if (a.isLterms())
                    {
                        return a.lterms().buckets().array();
                    }
                    if (a.isDterms())
                    {
                        return a.dterms().buckets().array();
                    }
                    return null;
                })
                .orElse(emptyList());
    }

    private String extractBucketKey(TermsBucketBase bucket)
    {
        return (String) ofNullable(bucket)
                .map(b -> {
                    if (b instanceof LongTermsBucket)
                    {
                        return ((LongTermsBucket) b).key();
                    }
                    else if (b instanceof StringTermsBucket)
                    {
                        return ((StringTermsBucket) b).key();
                    }
                    else if (b instanceof DoubleTermsBucket)
                    {
                        return ((DoubleTermsBucket) b).key();
                    }
                    return null;
                })
                .orElse(null);
    }
}
