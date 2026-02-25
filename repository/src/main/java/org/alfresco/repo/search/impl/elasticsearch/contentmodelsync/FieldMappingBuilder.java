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
package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;
import org.opensearch.client.opensearch.generic.Request;
import org.opensearch.client.opensearch.generic.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.ElasticsearchFieldMapping;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.ElasticsearchFieldMapper;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.ElasticsearchFieldMapper.FieldMappingContext;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.custom.CustomFieldMapper;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.PredefinedFieldMapper;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.util.Pair;

/**
 * A component which accepts the set of attributes that compose an Alfresco property definition and outputs the corresponding Elasticsearch field mapping. It is possible to provide a custom mappers by implementing {@link CustomFieldMapper} and declaring them in the Spring Context. All mapper beans will be injected at startup time - no additional configuration is needed.
 */
public class FieldMappingBuilder
{
    private final Logger logger = LoggerFactory.getLogger(FieldMappingBuilder.class);

    private final Collection<PredefinedFieldMapper> predefinedFieldMappers;

    private final Collection<CustomFieldMapper> customFieldMappers;

    public FieldMappingBuilder(Collection<PredefinedFieldMapper> predefinedFieldMappers, Collection<CustomFieldMapper> customFieldMappers)
    {
        this.predefinedFieldMappers = predefinedFieldMappers;
        this.customFieldMappers = customFieldMappers;
    }

    /**
     * Builds the Elasticsearch field mappings according with the input data.
     *
     * @param indexName
     *            the target Elasticsearch index name.
     * @param contentModelProperties
     *            the Alfresco property (e.g. {<a href="http://www.alfresco.org/model/content/1.0">...</a>}content)
     * @return a Pair object containing as first value the Elasticsearch field mappings corresponding to the input property definition and as second value containing the successfully mapped property count.
     */
    public Pair<Request, Integer> buildFieldsMappings(String indexName, Collection<PropertyDefinition> contentModelProperties)
    {
        // we need a counter because a PropertyDefinition can be mapped in multiple Elasticsearch field mapping
        AtomicInteger counter = new AtomicInteger();

        Map<String, Object> mappings = contentModelProperties.stream()
                .filter(PropertyDefinition::isIndexed)
                .map(this::buildFieldMappings)
                .peek(t -> counter.incrementAndGet())
                .collect(HashMap::new, Map::putAll, Map::putAll);

        JSONObject jsonMappings = new JSONObject(Map.of("properties", mappings));

        Request requests = Requests.builder().method("PUT")
                .endpoint("/" + indexName + "/_mapping").json(jsonMappings.toString()).build();

        return new Pair<>(requests, counter.get());
    }

    private Map<String, Object> buildFieldMappings(PropertyDefinition propertyDefinition)
    {
        FieldMappingContext mappingContext = new FieldMappingContext(propertyDefinition);

        logger.trace("Property: Alfresco = {}, Elasticsearch encoded = {}, datatype = {}", mappingContext.name()
                .raw(),
                mappingContext.name()
                        .encoded(),
                propertyDefinition.getDataType());

        return mapWithMappers(customFieldMappers, mappingContext).or(() -> mapWithMappers(predefinedFieldMappers, mappingContext))
                .map(ElasticsearchFieldMapping::asMap)
                .orElseGet(() -> {
                    logUnsupportedDatatype(propertyDefinition);

                    return Collections.emptyMap();
                });
    }

    private Optional<ElasticsearchFieldMapping> mapWithMappers(Collection<? extends ElasticsearchFieldMapper> mappers, FieldMappingContext context)
    {
        return mappers.stream()
                .filter(mapper -> mapper.canMap(context))
                .findFirst()
                .map(fieldMapper -> fieldMapper.buildMapping(context));
    }

    private void logUnsupportedDatatype(PropertyDefinition propertyDefinition)
    {
        logger.debug("Unsupported datatype ({}), property name was  {}", propertyDefinition.getDataType(), propertyDefinition.getName().toString());
    }
}
