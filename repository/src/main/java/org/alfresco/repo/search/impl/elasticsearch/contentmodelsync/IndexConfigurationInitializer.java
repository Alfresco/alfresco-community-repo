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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.search.impl.elasticsearch.model.FieldName;

/**
 * Allows to load and cache ElasticSearch index configuration from a JSON file, and to verify if properties are indexed or tokenized.
 */
public class IndexConfigurationInitializer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexConfigurationInitializer.class);
    private static final String PROPERTIES_FIELD = "properties";
    private static final String BASIC_FIELDS_FILE = "basicFields.json";

    private static final String BASIC_FIELDS_PATH = ContentModelSynchronizer.CONFIG_PATH + BASIC_FIELDS_FILE;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(
            new JsonFactory().disable(JsonParser.Feature.AUTO_CLOSE_SOURCE));
    private Map<String, Object> configuration;

    public IndexConfigurationInitializer()
    {
        try
        {
            this.configuration = loadConfiguration();
        }
        catch (IOException e)
        {
            LOGGER.error("Error while trying to load the Elasticsearch configuration: {}", e.getMessage());
        }
    }

    /**
     * Loads the JSON ES index configuration and turns it into a Map
     *
     * @return the ES index configuration as a Map
     * @throws IOException
     *             if the JSON config file is not found
     */
    private Map<String, Object> loadConfiguration() throws IOException
    {
        final InputStream configurationInputStream = getClass().getResourceAsStream(BASIC_FIELDS_PATH);
        if (configurationInputStream == null)
        {
            throw new IOException("Elasticsearch configuration file: " + BASIC_FIELDS_PATH + " not found.");
        }

        return OBJECT_MAPPER.readValue(configurationInputStream, new TypeReference<>() {});
    }

    /**
     * Loads Elasticsearch index configuration in JSON format.
     *
     * @return an {@link InputStream} to the index configuration in JSON format
     * @throws IOException
     *             if the configuration is not readable
     */
    public InputStream loadConfigurationAsInputStream() throws IOException
    {
        Objects.requireNonNull(configuration, "The ElasticSearch configuration should not be null.");
        return new ByteArrayInputStream(OBJECT_MAPPER.writeValueAsBytes(configuration));
    }

    /**
     * Verifies if property is specified in index configuration.
     *
     * @param propertyName
     *            - property name
     * @return true if property is present in configuration file
     */
    @SuppressWarnings("unchecked")
    public boolean isPropertyIndexed(final String propertyName)
    {
        return StringUtils.isNotEmpty(propertyName) &&
                configuration.containsKey(PROPERTIES_FIELD) &&
                ((Map<String, Object>) configuration.get(PROPERTIES_FIELD)).containsKey(propertyName);
    }

    /**
     * Verifies basing on index configuration if property is tokenized.
     *
     * @param propertyName
     *            - property name
     * @return true if property is tokenized
     */
    @SuppressWarnings("unchecked")
    public boolean isPropertyTokenized(final String propertyName)
    {
        if (StringUtils.isEmpty(propertyName))
        {
            return false;
        }

        final String propertyNameWithoutSuffix = FieldName.isUntokenized(propertyName) ? FieldName.fromUntokenized(propertyName).raw() : propertyName;
        return configuration.containsKey(PROPERTIES_FIELD) &&
                ((Map<String, Object>) configuration.get(PROPERTIES_FIELD)).containsKey(propertyNameWithoutSuffix) &&
                !((Map<String, Object>) configuration.get(PROPERTIES_FIELD)).containsKey(FieldName.untokenized(propertyNameWithoutSuffix));
    }

}
