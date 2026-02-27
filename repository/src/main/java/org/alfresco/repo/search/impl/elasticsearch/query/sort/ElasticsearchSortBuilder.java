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
package org.alfresco.repo.search.impl.elasticsearch.query.sort;

import static org.alfresco.repo.search.adaptor.QueryConstants.PROPERTY_FIELD_PREFIX;
import static org.alfresco.repo.search.impl.QueryParserUtils.matchPropertyDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opensearch.client.opensearch._types.FieldSort;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.ScoreSort;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.IndexConfigurationInitializer;
import org.alfresco.repo.search.impl.elasticsearch.model.FieldName;
import org.alfresco.repo.search.impl.parsers.AlfrescoFunctionEvaluationContext;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class ElasticsearchSortBuilder
{
    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchSortBuilder.class);

    private final NamespaceDAO namespaceDAO;
    private final DictionaryService dictionaryService;
    private final IndexConfigurationInitializer indexConfigurationInitializer;

    public ElasticsearchSortBuilder(NamespaceDAO namespaceDAO, DictionaryService dictionaryService, IndexConfigurationInitializer indexConfigurationInitializer)
    {
        this.namespaceDAO = namespaceDAO;
        this.dictionaryService = dictionaryService;
        this.indexConfigurationInitializer = indexConfigurationInitializer;
    }

    /**
     * Convert sortDefinitions in Elasticsearch SortBuilders with correct field names
     *
     * @param searchParameters
     *            Containing sort definition
     * @return List of SortBuilders
     */
    public List<SortOptions> getSortBuilders(SearchParameters searchParameters)
    {

        List<SearchParameters.SortDefinition> sortDefinitions = searchParameters.getSortDefinitions();
        if (sortDefinitions == null)
            return Collections.emptyList();

        AlfrescoFunctionEvaluationContext functionContext = new AlfrescoFunctionEvaluationContext(
                namespaceDAO, dictionaryService, searchParameters.getNamespace());

        return searchParameters.getSortDefinitions().stream().map(sortDefinition -> {
            SortOrder sortOrder = sortDefinition.isAscending() ? SortOrder.Asc : SortOrder.Desc;
            SortOptions sortBuilder = null;
            switch (sortDefinition.getSortType())
            {
            case FIELD:
                String sortField = normalizeSortField(sortDefinition.getField());

                String luceneFieldName = functionContext.getLuceneFieldName(sortField);

                if (!fieldIsIndexed(luceneFieldName))
                {
                    LOGGER.warn("Ignorning sort on field {}. Sorting by unindexed fields is not supported.",
                            sortDefinition.getField());
                    break;
                }

                if (fieldIsTokenizedOnly(luceneFieldName))
                {
                    LOGGER.warn("Ignorning sort on field {}. Sorting by tokenized fields is not supported.",
                            sortDefinition.getField());
                    break;
                }

                sortBuilder = new SortOptions.Builder().field(new FieldSort.Builder().field(getSortableFieldName(luceneFieldName)).missing(FieldValue.of("_last")).order(sortOrder).build()).build();
                break;
            case DOCUMENT:
                sortBuilder = new SortOptions.Builder().field(new FieldSort.Builder().field("_doc").missing(FieldValue.of("_last")).order(sortOrder).build()).build();
                break;
            case SCORE:
                sortBuilder = new SortOptions.Builder().score(new ScoreSort.Builder().order(sortOrder).build()).build();
                break;
            default:
            }

            return sortBuilder;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private String getSortableFieldName(String name)
    {
        return FieldName.fromLucene(name, namespaceDAO).untokenized();
    }

    // Normalizes the sort field by converting special fields like ":TYPE" to their prefixed QName form.
    private String normalizeSortField(String sortField)
    {
        if (":TYPE".equalsIgnoreCase(sortField))
        {
            QName typeQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "type");
            return typeQName.toPrefixString(namespaceDAO);
        }
        return sortField;
    }

    // Verify if this field is indexed or not. Sorting by unindexed fields is not supported
    private boolean fieldIsIndexed(String fieldName)
    {
        return Optional.ofNullable(fieldName)
                .map(this::getPropertyDefinitionFromField)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(PropertyDefinition::isIndexed)
                // if field not found in dictionary service, lookup in ElasticSearch index configuration
                .orElse(indexConfigurationInitializer.isPropertyIndexed(getSortableFieldName(fieldName)));
    }

    // Verify if tokenized=true on field. Sorting by fields that do not have the untokenized field indexed is not supported
    private boolean fieldIsTokenizedOnly(String fieldName)
    {
        return Optional.ofNullable(fieldName)
                .map(this::getPropertyDefinitionFromField)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(PropertyDefinition::getIndexTokenisationMode)
                .map(IndexTokenisationMode.TRUE::equals)
                // if field not found in dictionary service, lookup in ElasticSearch index configuration
                .orElse(indexConfigurationInitializer.isPropertyTokenized(getSortableFieldName(fieldName)));
    }

    private Optional<PropertyDefinition> getPropertyDefinitionFromField(String fieldName)
    {
        return Optional.ofNullable(fieldName)
                .map(name -> name.startsWith(PROPERTY_FIELD_PREFIX) ? name.substring(PROPERTY_FIELD_PREFIX.length()) : name)
                .map(name -> QName.resolveToQName(namespaceDAO, name))
                .map(qname -> matchPropertyDefinition(qname.getNamespaceURI(), namespaceDAO, dictionaryService, qname.getLocalName()));
    }
}
