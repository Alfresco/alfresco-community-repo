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
package org.alfresco.repo.search.impl.elasticsearch.query.highlight;

import static java.util.Optional.ofNullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.opensearch.client.opensearch.core.search.Highlight;
import org.opensearch.client.opensearch.core.search.HighlightField;
import org.opensearch.client.opensearch.core.search.HighlighterType;

import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.search.impl.elasticsearch.model.FieldName;
import org.alfresco.repo.search.impl.parsers.AlfrescoFunctionEvaluationContext;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.FieldHighlightParameters;
import org.alfresco.service.cmr.search.GeneralHighlightParameters;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Responsible for including highlight information in the ES request.
 */
public class ElasticsearchHighlightBuilder
{
    private static final String DEFAULT_POSTFIX = "</em>";
    private static final String DEFAULT_HIGHLIGHTER = "plain";

    private final NamespaceDAO namespaceDAO;
    private final DictionaryService dictionaryService;

    public ElasticsearchHighlightBuilder(NamespaceDAO namespaceDAO, DictionaryService dictionaryService)
    {
        this.namespaceDAO = namespaceDAO;
        this.dictionaryService = dictionaryService;
    }

    /**
     * Create the Elasticsearch HighlightBuilder from the AFTS request.
     *
     * @param searchParameters
     *            AFTS request containing the highlight definition.
     * @return The ES highlight builder or null if no highlighting is required.
     */
    public Highlight getHighlightBuilder(SearchParameters searchParameters)
    {
        GeneralHighlightParameters generalHighlightParameters = searchParameters.getHighlight();
        if (generalHighlightParameters == null)
        {
            return null;
        }

        AlfrescoFunctionEvaluationContext functionContext = new AlfrescoFunctionEvaluationContext(
                namespaceDAO, dictionaryService, searchParameters.getNamespace());

        return createHighlightBuilder(generalHighlightParameters, functionContext);
    }

    private Highlight createHighlightBuilder(GeneralHighlightParameters generalHighlightParameters, AlfrescoFunctionEvaluationContext functionContext)
    {
        Map<String, HighlightField> highlightFieldMap = new LinkedHashMap<>();
        Highlight.Builder highlightBuilder = new Highlight.Builder();
        generalHighlightParameters.getFields().forEach(fieldHighlightParameters -> {
            String luceneFieldName = functionContext.getLuceneFieldName(fieldHighlightParameters.getField());
            String fieldName = FieldName.fromLucene(luceneFieldName, namespaceDAO).encoded();

            Optional<String> prefix = ofNullable(generalHighlightParameters.getPrefix());
            Optional<String> postfix = ofNullable(generalHighlightParameters.getPostfix());
            Optional<Integer> snippetCount = ofNullable(generalHighlightParameters.getSnippetCount());
            Optional<Integer> fragmentSize = ofNullable(generalHighlightParameters.getFragmentSize());

            prefix.ifPresent(highlightBuilder::preTags);
            postfix.ifPresentOrElse(highlightBuilder::postTags, () -> prefix.ifPresent(ignore -> highlightBuilder.postTags(DEFAULT_POSTFIX)));
            snippetCount.ifPresent(highlightBuilder::numberOfFragments);
            fragmentSize.ifPresent(highlightBuilder::fragmentSize);

            HighlightField field = createField(fieldHighlightParameters);

            highlightFieldMap.put(fieldName, field);

        });

        highlightBuilder.fields(highlightFieldMap);
        highlightBuilder.type(new HighlighterType.Builder().custom(DEFAULT_HIGHLIGHTER).build());
        return highlightBuilder.build();
    }

    private static HighlightField createField(FieldHighlightParameters fieldHighlightParameters)
    {
        Optional<String> prefix = ofNullable(fieldHighlightParameters.getPrefix());
        Optional<String> postfix = ofNullable(fieldHighlightParameters.getPostfix());
        Optional<Integer> snippetCount = ofNullable(fieldHighlightParameters.getSnippetCount());
        Optional<Integer> fragmentSize = ofNullable(fieldHighlightParameters.getFragmentSize());

        HighlightField.Builder field = new HighlightField.Builder();
        prefix.ifPresent(field::preTags);
        postfix.ifPresentOrElse(field::postTags, () -> prefix.ifPresent(ignore -> field.postTags(DEFAULT_POSTFIX)));
        snippetCount.ifPresent(field::numberOfFragments);
        fragmentSize.ifPresent(field::fragmentSize);

        return field.build();
    }
}
