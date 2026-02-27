/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import static java.util.Optional.ofNullable;

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.core.search.SearchResult;

import org.alfresco.repo.search.impl.elasticsearch.model.FieldName;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.FieldHighlightParameters;
import org.alfresco.service.cmr.search.GeneralHighlightParameters;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

class HighlightsHandler
{
    Map<NodeRef, List<Pair<String, List<String>>>> handle(SearchParameters searchParameters, SearchResponse<Object> searchResponse)
    {
        Set<String> requestedHighlightFields = extractRequestedHighlightFields(searchParameters);
        List<Hit<Object>> hits = extractHits(searchResponse);
        Map<NodeRef, List<Pair<String, List<String>>>> highlights = new HashMap<>();
        for (Hit<Object> hit : hits)
        {
            NodeRef nodeRef = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, hit.id());

            List<Pair<String, List<String>>> highlightEntries = new ArrayList<>();
            for (Map.Entry<String, List<String>> highlightField : hit.highlight().entrySet())
            {
                String decodedField = FieldName.decode(highlightField.getKey());
                String field = matchRequestedField(decodedField, requestedHighlightFields).orElse(decodedField);
                Pair<String, List<String>> highlightPair = new Pair<>(field, highlightField.getValue());
                highlightEntries.add(highlightPair);
            }
            highlights.put(nodeRef, highlightEntries);
        }
        return Collections.unmodifiableMap(highlights);
    }

    private Set<String> extractRequestedHighlightFields(SearchParameters searchParameters)
    {
        return ofNullable(searchParameters)
                .map(SearchParameters::getHighlight)
                .map(GeneralHighlightParameters::getFields)
                .stream()
                .flatMap(List::stream)
                .map(FieldHighlightParameters::getField)
                .collect(Collectors.toSet());
    }

    private List<Hit<Object>> extractHits(SearchResponse<Object> searchResponse)
    {
        return ofNullable(searchResponse)
                .map(SearchResult::hits)
                .map(HitsMetadata::hits)
                .orElse(List.of());
    }

    /* Share requests highlight on field "content". Elasticsearch returns "cm:content". */
    private Optional<String> matchRequestedField(String decodedField, Set<String> requestedHighlightFields)
    {
        if (requestedHighlightFields.contains(decodedField))
        {
            return Optional.of(decodedField);
        }
        String[] splitPrefixedQName = QName.splitPrefixedQName(decodedField);
        if (splitPrefixedQName == null || splitPrefixedQName.length != 2)
        {
            return Optional.empty();
        }
        String localName = splitPrefixedQName[1];
        if (requestedHighlightFields.contains(localName))
        {
            return Optional.of(localName);
        }
        return Optional.empty();
    }
}
