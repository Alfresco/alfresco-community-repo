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

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.iterate;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

@PropertySource(value = {"classpath:/alfresco/extension/exactTermSearch.properties",
        "classpath:/alfresco/search/elasticsearch/config/exactTermSearch.properties",
        "classpath:/exactTermSearch.properties"}, name = "exactTermSearchConfiguration", ignoreResourceNotFound = true)
public class ElasticsearchExactTermSearchConfig
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchExactTermSearchConfig.class);

    private final static String EXACT_TERM_SEARCH_DATATYPE_PREFIX = "alfresco.cross.locale.datatype.";
    private final static String EXACT_TERM_SEARCH_PROPERTY_PREFIX = "alfresco.cross.locale.property.";

    private final Set<QName> crossLocaleSearchDataTypes = new HashSet<>();
    private final Set<QName> crossLocaleSearchProperties = new HashSet<>();

    public ElasticsearchExactTermSearchConfig(Environment environment)
    {
        try
        {
            crossLocaleSearchDataTypes.addAll(getDatatypesWithExactTermSearchEnabled(environment, EXACT_TERM_SEARCH_DATATYPE_PREFIX));
            crossLocaleSearchProperties.addAll(getDatatypesWithExactTermSearchEnabled(environment, EXACT_TERM_SEARCH_PROPERTY_PREFIX));

            LOGGER.info("Exact term search: number of configured datatypes is {}.", crossLocaleSearchDataTypes.size());
            LOGGER.debug("Exact term search, configured datatypes: {}", crossLocaleSearchDataTypes);

            LOGGER.info("Exact term search: number of configured properties is {}.", crossLocaleSearchProperties.size());
            LOGGER.debug("Exact term search, configured properties: {}", crossLocaleSearchProperties);
        }
        catch (Exception exception)
        {
            LOGGER.warn("Exact term search is disabled; an issue has been detected in the configuration. Please check that and the stacktrace below for the cause.", exception);
        }
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
    {
        return new PropertySourcesPlaceholderConfigurer();
    }

    private Set<QName> getDatatypesWithExactTermSearchEnabled(Environment environment, String configPropertyPrefix)
    {
        return iterate(0, i -> i + 1).mapToObj(Integer::toString)
                .map(configPropertyPrefix::concat)
                .map(environment::getProperty)
                .takeWhile(Objects::nonNull)
                .map(QName::createQName)
                .collect(toSet());
    }

    public boolean isExactTermSearchEnabled(PropertyDefinition propertyDefinition)
    {
        return crossLocaleSearchDataTypes.contains(propertyDefinition.getDataType()
                .getName()) || crossLocaleSearchProperties.contains(propertyDefinition.getName());
    }
}
