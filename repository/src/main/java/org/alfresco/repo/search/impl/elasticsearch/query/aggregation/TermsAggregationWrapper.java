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
package org.alfresco.repo.search.impl.elasticsearch.query.aggregation;

import java.util.Map;
import java.util.Optional;

import org.opensearch.client.opensearch._types.aggregations.TermsAggregation;
import org.opensearch.client.opensearch._types.query_dsl.Query;

/**
 * Wraps a {@link TermsAggregation} with its aggregation name, additional metadata to support post-processing operations and a complementary aggregation if required.
 * <p>
 * Practical examples of the use of this wrapper include:
 * <p>
 * For non-SITE aggregations {@code postProcessingData} is empty and {@code complementaryAggregator} is absent.
 * <p>
 * For SITE aggregations:
 * <ul>
 * <li>{@code postProcessingData} maps each site (and Shared Files folder) node UUID to its name label, allowing {@code primaryHierarchy} bucket keys to be translated back to site short names or {@code _SHARED_FILES_}.</li>
 * <li>{@code complementaryAggregation} carries the name and filter query for a complementary aggregation that counts documents not belonging to any site or Shared Files, to be presented as a {@code _REPOSITORY_} bucket in the results.</li>
 * </ul>
 *
 * @param name
 *            the aggregation name used as the key in the ES request
 * @param termsAggregation
 *            the built {@link TermsAggregation}
 * @param postProcessingData
 *            Map containing data that will be used in the result post-processing phase, e.g., UUID-to-name-label mapping for SITE aggregations
 * @param complementaryAggregation
 *            complementary aggregation, if applicable
 */
public record TermsAggregationWrapper(
        String name,
        TermsAggregation termsAggregation,
        Map<String, String> postProcessingData,
        Optional<ComplementaryAggregation> complementaryAggregation)
{

    /**
     * A complementary aggregation that can be used to negate the main term aggregation.
     * <p>
     * For example, in the case of a SITE aggregation, this complementary aggregation applies a filter to count documents that do not belong to any known site or the Shared Files folder.
     * <p>
     * Its doc count is presented as a synthetic bucket alongside the regular site term buckets, using {@code displayLabel} as the bucket key.
     *
     * @param aggregationName
     *            the name under which this filter aggregation is registered in the ES request (must be unique in the request)
     * @param filterQuery
     *            the filter query (typically a {@code bool.must_not terms})
     * @param displayLabel
     *            the label to use for this synthetic bucket in the facet results
     */
    public record ComplementaryAggregation(String aggregationName, Query filterQuery, String displayLabel)
    {}
}
