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
package org.alfresco.repo.search.impl.elasticsearch.model;

import static org.alfresco.repo.search.adaptor.QueryConstants.PROPERTY_FIELD_PREFIX;
import static org.alfresco.repo.search.impl.elasticsearch.shared.translator.AlfrescoQualifiedNameTranslator.encode;

import java.util.Objects;

import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.search.impl.elasticsearch.shared.translator.AlfrescoQualifiedNameTranslator;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

/**
 * The various names of a property stored in different ways in Elasticsearch.
 *
 * @param raw
 *            The raw property name - e.g. cm:content, PATH
 */
public record FieldName(
        String raw)
{
    private final static String UNTOKENIZED_FIELD_SUFFIX = "_untokenized";
    private final static String EXACT_TERM_SEARCH_FIELD_SUFFIX = "_exact";
    private final static String TRUNCATED_PREFIX = "...";

    public FieldName(PropertyDefinition propertyDefinition)
    {
        this(propertyDefinition.getName()
                .getPrefixString());
    }

    /**
     * Create a new FieldName from the exact term version.
     *
     * @param name
     *            The exact term search field - e.g. cm%3Acontent_exact
     * @return The new FieldName.
     */
    public static FieldName fromExactTermSearch(String name)
    {
        if (!isExactTermSearch(name))
        {
            throw new IllegalArgumentException("Name should be in exact term search format");
        }

        return new FieldName(convertToRaw(name, EXACT_TERM_SEARCH_FIELD_SUFFIX));
    }

    /**
     * Create a new FieldName from the untokenized version.
     *
     * @param name
     *            The untokenized version of the property - e.g. cm%3Aname_untokenized, ASPECT_untokenized
     * @return The new FieldName.
     */
    public static FieldName fromUntokenized(String name)
    {
        if (!isUntokenized(name))
        {
            throw new IllegalArgumentException("Name should be in untokenized format");
        }

        return new FieldName(convertToRaw(name, UNTOKENIZED_FIELD_SUFFIX));
    }

    /**
     * Create a FieldName from a Lucene property name.
     *
     * @param name
     *            The lucene property name - for example @cm:name, @{http://www.alfresco.org/model/content/1.0}title
     * @param namespaceDAO
     *            The namespace DAO used to find the prefix for the property.
     * @return The new FieldName.
     */
    public static FieldName fromLucene(String name, NamespaceDAO namespaceDAO)
    {
        Objects.requireNonNull(name);
        Objects.requireNonNull(namespaceDAO);

        if (name.startsWith(PROPERTY_FIELD_PREFIX))
        {
            name = name.substring(1);
        }

        QName fieldQName = QName.createQName(name);
        String prefixString = fieldQName.toPrefixString(namespaceDAO);
        return new FieldName(prefixString);
    }

    private static String convertToRaw(String name, String suffix)
    {
        return decode(name.substring(0, name.indexOf(suffix)));
    }

    /**
     * Generate the encoded format of the property name.
     *
     * @return The encoded property name - e.g. cm%3Acontent, PATH
     */
    public String encoded()
    {
        return encoded(raw);
    }

    /**
     * Generate the ES untokenized version of the property name.
     *
     * @return The untokenized property name - e.g. cm%3Acontent_untokenized, ASPECT_untokenized
     */
    public String untokenized()
    {
        return untokenized(raw);
    }

    /**
     * Generate the ES exact term search format of the property name.
     *
     * @return The exact term search field - e.g. cm%3Acontent_exact
     */
    public String exactTermSearch()
    {
        return exactTermSearch(raw);
    }

    /**
     * Generate the truncated format of the field name.
     *
     * @return The truncated name - e.g. cm:content, ...ty_with_insanely_long_name_for_testing_purposes
     */
    public String truncated()
    {
        return truncated(raw);
    }

    /**
     * Check if a property is an ES untokenized field.
     *
     * @param nameWithUnknownFormat
     *            The property name to check.
     * @return true if the name ends with the untokenized suffix.
     */
    public static boolean isUntokenized(String nameWithUnknownFormat)
    {
        return nameWithUnknownFormat.endsWith(UNTOKENIZED_FIELD_SUFFIX);
    }

    /**
     * Check if a property is an ES exact term field.
     *
     * @param nameWithUnknownFormat
     *            The property name to check.
     * @return true if the name ends with the exact term suffix.
     */
    public static boolean isExactTermSearch(String nameWithUnknownFormat)
    {
        return nameWithUnknownFormat.endsWith(EXACT_TERM_SEARCH_FIELD_SUFFIX);
    }

    /**
     * Convert a raw property name to an encoded format.
     *
     * @param name
     *            The raw property name - e.g. cm:content, PATH
     * @return The encoded property name - e.g. cm%3Acontent, PATH
     */
    public static String encoded(String name)
    {
        return encode(name);
    }

    /**
     * Convert a raw property name to the ES untokenized property name.
     *
     * @param name
     *            The raw property name - e.g. cm:content, ASPECT
     * @return The untokenized property name - e.g. cm%3Acontent_untokenized, ASPECT_untokenized
     */
    public static String untokenized(String name)
    {
        return encoded(name) + UNTOKENIZED_FIELD_SUFFIX;
    }

    /**
     * Convert a raw property name to the ES exact term search property name.
     *
     * @param name
     *            The raw property name - e.g. cm:content
     * @return The exact term search field - e.g. cm%3Acontent_exact
     */
    public static String exactTermSearch(String name)
    {
        return encoded(name) + EXACT_TERM_SEARCH_FIELD_SUFFIX;
    }

    /**
     * Create the truncated format of the field name.
     *
     * @param name
     *            A property name - e.g. cm:content, cm:property_with_insanely_long_name_for_testing_purposes
     * @return The truncated name - e.g. cm:content, ...ty_with_insanely_long_name_for_testing_purposes
     */
    public static String truncated(String name)
    {
        return name.length() <= 50 ? name : TRUNCATED_PREFIX + name.substring(name.length() - 47);
    }

    /**
     * Convert an encoded property name to the raw name.
     *
     * @param name
     *            The encoded property name - e.g. cm%3Aname, PATH
     * @return The raw name - e.g. cm:name, PATH
     */
    public static String decode(String name)
    {
        return AlfrescoQualifiedNameTranslator.decode(name);
    }
}
