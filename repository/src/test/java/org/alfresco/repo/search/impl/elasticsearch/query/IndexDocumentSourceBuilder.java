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
package org.alfresco.repo.search.impl.elasticsearch.query;

import static java.util.Arrays.asList;

import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.ALIVE;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.ASPECT;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.CONTENT_ATTRIBUTE_NAME;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.CREATION_DATE_FIELD;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.METADATA_INDEXING_LAST_UPDATE;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.MODIFICATION_DATE_FIELD;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.NAME;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.OWNER;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.PATH;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.PRIMARY_HIERARCHY;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.READER;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.TAG;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.TYPE;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.UNPREFIXED_PATH;
import static org.alfresco.repo.search.impl.elasticsearch.shared.translator.AlfrescoQualifiedNameTranslator.encode;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.namespace.QName;

public class IndexDocumentSourceBuilder
{
    private static final String PATH_DELIMITER = "/";
    private static final String NAMESPACE_PREFIX = ":";
    private String name;
    private String content;
    private Date creationDate = new Date();
    private Date date = new Date();
    private List<String> path;
    private List<String> unprefixedPath;
    private Map<String, Object> additionalProperties = new HashMap<>();
    private List<String> aspects;
    private List<String> type;
    private List<String> readers = Collections.singletonList("GROUP_EVERYONE");
    private List<String> tags = List.of();
    private List<String> primaryHierarchy = List.of();

    public static IndexDocumentSourceBuilder from(Map<QName, Serializable> properties)
    {
        return new IndexDocumentSourceBuilder()
                .withName((String) properties.get(PROP_NAME));
    }

    public IndexDocumentSourceBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public IndexDocumentSourceBuilder withContent(String content)
    {
        this.content = content;
        return this;
    }

    public IndexDocumentSourceBuilder withDate(Date date)
    {
        this.date = date;
        return this;
    }

    public IndexDocumentSourceBuilder withCreationDate(Date date)
    {
        this.creationDate = date;
        return this;
    }

    public IndexDocumentSourceBuilder withAdditionalProperties(Map<String, Object> additionalProperties)
    {
        this.additionalProperties = additionalProperties;
        return this;
    }

    public IndexDocumentSourceBuilder withAspects(String... aspects)
    {
        this.aspects = asList(aspects);
        return this;
    }

    public IndexDocumentSourceBuilder withType(String... type)
    {
        this.type = asList(type);
        return this;
    }

    public IndexDocumentSourceBuilder withPath(String path)
    {
        final String pathWithoutNamespaces = removeNamespaces(path);
        this.path = List.of(path);
        this.unprefixedPath = List.of(pathWithoutNamespaces);
        return this;
    }

    public IndexDocumentSourceBuilder withTag(String... tags)
    {
        this.tags = Stream
                .of(tags)
                .filter(Objects::nonNull)
                .map(t -> t.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableList());
        return this;
    }

    public IndexDocumentSourceBuilder withPrimaryHierarchy(Collection<String> primaryHierarchy)
    {
        this.primaryHierarchy = primaryHierarchy.stream()
                .filter(Objects::nonNull)
                .map(t -> t.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableList());
        return this;
    }

    public IndexDocumentSourceBuilder withReaders(String... readers)
    {
        this.readers = asList(readers);
        return this;
    }

    public String getName()
    {
        return name;
    }

    /**
     * Build the source to send as index request body to Elasticsearch The "name" value will be used as content if it is null.
     *
     * @return
     * @throws IOException
     */
    public Map<String, Object> buildSource()
            throws IOException
    {
        if (content == null)
        {
            content = name;
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        Map<String, Object> source = new HashMap<>();
        source.put(READER, readers);
        source.put(encode(CREATION_DATE_FIELD), df.format(creationDate));
        source.put(encode(MODIFICATION_DATE_FIELD), df.format(date));
        source.put(encode(CONTENT_ATTRIBUTE_NAME), content);
        source.put(encode(OWNER), AuthenticationUtil.getFullyAuthenticatedUser());
        source.put(encode(ALIVE), true);
        source.put(encode(ASPECT), aspects);
        source.put(encode(TYPE), type);
        source.put(encode(PATH), path);
        source.put(encode(UNPREFIXED_PATH), unprefixedPath);
        source.put(encode(NAME), name);
        source.put(encode(TAG), tags.toArray(String[]::new));
        source.put(encode(PRIMARY_HIERARCHY), primaryHierarchy.toArray(String[]::new));

        additionalProperties.forEach((key, value) -> safeAddFieldToSource(source, key, value));
        return source;
    }

    public Map<String, Object> buildeSourceForDeletedDocument() throws IOException
    {
        Map<String, Object> source = new HashMap<>();
        source.put(METADATA_INDEXING_LAST_UPDATE, 100);
        source.put("ALIVE", false);

        return source;
    }

    private void safeAddFieldToSource(Map<String, Object> source, String key, Object value)
    {
        try
        {
            source.put(encode(key), value);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private String removeNamespaces(final String path)
    {
        return Arrays.stream(path.split(PATH_DELIMITER))
                .map(p -> p.substring(p.indexOf(NAMESPACE_PREFIX) + 1))
                .collect(Collectors.joining(PATH_DELIMITER));
    }

}
