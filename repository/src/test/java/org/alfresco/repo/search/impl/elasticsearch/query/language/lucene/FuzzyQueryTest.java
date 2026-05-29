/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query.language.lucene;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ACLID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ACLTXCOMMITTIME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ACLTXID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ALL;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ANCESTOR;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ASPECT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_AUTHORITY;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_AUTHORITYSET;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_CASCADETX;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_CLASS;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_DBID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_DENIED;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_DENYSET;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_EXACTASPECT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_EXACTTYPE;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_FINGERPRINT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_FTSSTATUS;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_INACLTXID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_INTXID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ISCONTAINER;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ISNODE;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ISNOTNULL;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ISNULL;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ISROOT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ISUNSET;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_OWNER;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_OWNERSET;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PARENT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PATH;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PATHWITHREPEATS;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PRIMARYASSOCQNAME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PRIMARYASSOCTYPEQNAME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PRIMARYPARENT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_QNAME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_READER;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_READERSET;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_SITE;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_SOLR4_ID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TAG;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TENANT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TX;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TXCOMMITTIME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TXID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TYPE;
import static org.alfresco.repo.search.adaptor.QueryConstants.PROPERTY_FIELD_PREFIX;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.lucene.search.Query;
import org.junit.Test;

import org.alfresco.repo.search.adaptor.QueryConstants;
import org.alfresco.repo.search.impl.elasticsearch.shared.translator.AlfrescoQualifiedNameTranslator;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;

@SuppressWarnings("PMD")
public class FuzzyQueryTest extends BaseQueryTest
{
    private static final float MIN_SIMILARITY = 2.0f;

    @Test
    public void propertyTermQueryWithUnqualifiedPropertyName()
    {
        REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES
                .forEach(name -> assertQueryEquals(
                        fuzzyQueryString(toElasticsearchForm(name)),
                        parseAndBuildQuery(fuzzyQueryString(PROPERTY_FIELD_PREFIX + escape(name)))));
    }

    @Test
    public void propertyTermQueryWithPrefixedPropertyName()
    {
        REQUEST_SPECIFIC_PREFIXED_TEXT_ATTRIBUTES
                .forEach(name -> assertQueryEquals(
                        fuzzyQueryString(toElasticsearchForm(name)),
                        parseAndBuildQuery(fuzzyQueryString(PROPERTY_FIELD_PREFIX + escape(name)))));
    }

    @Test
    public void propertyTermQueryWithFullyQualifiedPropertyName()
    {
        REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES
                .forEach(name -> assertQueryEquals(
                        fuzzyQueryString(toElasticsearchForm(name)),
                        parseAndBuildQuery(fuzzyQueryString(PROPERTY_FIELD_PREFIX + escape(name)))));
    }

    @Test
    public void ALLFieldTermQueryExpansionUsingCustomUnqualifiedAllAttributes()
    {
        allFieldExpansionUsingCustomAttributes(
                REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES,
                fuzzyQueryString(FIELD_ALL),
                this::fuzzyQueryString);
    }

    @Test
    public void ALLFieldTermQueryExpansionUsingCustomFullyQualifiedAllAttributes()
    {
        allFieldExpansionUsingCustomAttributes(
                REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES,
                fuzzyQueryString(FIELD_ALL),
                this::fuzzyQueryString);
    }

    @Test
    public void ALLFieldTermQueryExpansionUsingCustomPrefixedAllAttributes()
    {
        allFieldExpansionUsingCustomAttributes(
                REQUEST_SPECIFIC_PREFIXED_TEXT_ATTRIBUTES,
                fuzzyQueryString(FIELD_ALL),
                this::fuzzyQueryString);
    }

    @Test
    public void TEXTFieldTermQueryExpansionUsingCustomUnqualifiedTextAttributes()
    {
        textFieldExpansionUsingCustomTextAttributes(
                REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES,
                fuzzyQueryString(QueryConstants.FIELD_TEXT),
                this::fuzzyQueryString);
    }

    @Test
    public void TEXTFieldTermQueryExpansionUsingCustomPrefixedTextAttributes()
    {
        textFieldExpansionUsingCustomTextAttributes(
                REQUEST_SPECIFIC_PREFIXED_TEXT_ATTRIBUTES,
                fuzzyQueryString(QueryConstants.FIELD_TEXT),
                this::fuzzyQueryString);
    }

    @Test
    public void TEXTFieldTermQueryExpansionUsingCustomFullyQualifiedTextAttributes()
    {
        textFieldExpansionUsingCustomTextAttributes(
                REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES,
                fuzzyQueryString(QueryConstants.FIELD_TEXT),
                this::fuzzyQueryString);
    }

    @Override
    protected Stream<Function<String, Query>> queryBuilderFunctions()
    {
        return Stream.of(fieldName -> parser.getFuzzyQuery(fieldName, A_TERM, MIN_SIMILARITY));
    }

    @Override
    protected Stream<String> unsupportedFields()
    {
        return Stream.of(
                // PATH QUERIES
                FIELD_PATH,
                FIELD_PATHWITHREPEATS,
                FIELD_ANCESTOR,
                FIELD_PARENT,
                FIELD_PRIMARYPARENT,
                FIELD_QNAME,
                FIELD_PRIMARYASSOCQNAME,
                FIELD_PRIMARYASSOCTYPEQNAME,
                FIELD_PARENT,
                FIELD_PRIMARYPARENT,

                FIELD_TYPE,
                FIELD_EXACTTYPE,
                FIELD_CLASS,

                FIELD_ASPECT,
                FIELD_EXACTASPECT,

                FIELD_ISUNSET,

                FIELD_SITE,

                FIELD_ISNULL,
                FIELD_ISNOTNULL,

                // Not supported
                FIELD_ISNODE,
                FIELD_ISROOT,
                FIELD_ISCONTAINER,
                FIELD_SOLR4_ID,
                FIELD_CASCADETX,
                FIELD_DBID,
                FIELD_TX,
                FIELD_TXID,
                FIELD_INTXID,
                FIELD_TXCOMMITTIME,
                FIELD_ACLID,
                FIELD_INACLTXID,
                FIELD_ACLTXID,
                FIELD_ACLTXCOMMITTIME,
                FIELD_TENANT,
                FIELD_OWNERSET,
                FIELD_READERSET,
                FIELD_AUTHORITYSET,
                FIELD_DENYSET,
                FIELD_FTSSTATUS,
                FIELD_FINGERPRINT);
    }

    @Test
    public void OWNERFieldShouldDelegateToDefaultQueryParser()
    {
        Stream.of("agazzarini~2", "maxred~2", "a1s3s83a~2", "383e8dj~2")
                .forEach(value -> assertQueryEquals(
                        fuzzyQueryString(FIELD_OWNER, value),
                        parseAndBuildQuery(fuzzyQueryString(FIELD_OWNER, value))));
    }

    @Test
    public void TAGFieldShouldDelegateToDefaultQueryParser()
    {
        Stream.of("agazzarini~2", "maxred~2", "a1s3s83a~2", "383e8dj~2")
                .forEach(value -> assertQueryEquals(
                        fuzzyQueryString(FIELD_TAG, value),
                        parseAndBuildQuery(fuzzyQueryString(FIELD_TAG, value))));
    }

    @Test
    public void READERFieldShouldDelegateToDefaultQueryParser()
    {
        Stream.of("agazzarini~2", "maxred~2", "a1s3s83a~2", "383e8dj~2")
                .forEach(value -> assertQueryEquals(
                        fuzzyQueryString(FIELD_READER, value),
                        parseAndBuildQuery(fuzzyQueryString(FIELD_READER, value))));
    }

    @Test
    public void DENIEDFieldShouldDelegateToDefaultQueryParser()
    {
        Stream.of("agazzarini~2", "maxred~2", "a1s3s83a~2", "383e8dj~2")
                .forEach(value -> assertQueryEquals(
                        fuzzyQueryString(FIELD_DENIED, value),
                        parseAndBuildQuery(fuzzyQueryString(FIELD_DENIED, value))));
    }

    @Test
    public void AUTHORITYFieldShouldDelegateToDefaultQueryParser()
    {
        Stream.of("agazzarini~2", "maxred~2", "a1s3s83a~2", "383e8dj~2")
                .forEach(value -> assertQueryEquals(
                        fuzzyQueryString(FIELD_AUTHORITY, value),
                        parseAndBuildQuery(fuzzyQueryString(FIELD_AUTHORITY, value))));
    }

    @Test
    public void datatypeDefinitionQueryUsingFullyQualifiedName()
    {
        var allAttributesOfGivenType = REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES.stream()
                .map(QName::createQName)
                .collect(toList());

        when(dictionaryService.getAllProperties(DataTypeDefinition.DATE))
                .thenReturn(allAttributesOfGivenType);

        parser = new LuceneQueryParserUnderTest() {
            Optional<DataTypeDefinition> datatypeDefinition(String fieldName)
            {
                var dataTypeDefinition = mock(DataTypeDefinition.class);
                when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.DATE);
                return Optional.of(dataTypeDefinition);
            }
        };

        var query = parseAndBuildQuery(fuzzyQueryString(escape(DataTypeDefinition.DATE.toString())));

        assertQueryEquals(
                allAttributesOfGivenType.stream()
                        .map(qname -> qname.toPrefixString(resolver))
                        .map(AlfrescoQualifiedNameTranslator::encode)
                        .map(this::fuzzyQueryString)
                        .collect(joining(" ")),
                query);
    }

    @Test
    public void datatypeDefinitionQueryUsingPrefixedName()
    {
        var allAttributesOfGivenType = REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES.stream()
                .map(QName::createQName)
                .collect(toList());

        when(dictionaryService.getAllProperties(DataTypeDefinition.DATE))
                .thenReturn(allAttributesOfGivenType);

        parser = new LuceneQueryParserUnderTest() {
            Optional<DataTypeDefinition> datatypeDefinition(String fieldName)
            {
                var dataTypeDefinition = mock(DataTypeDefinition.class);
                when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.DATE);
                return Optional.of(dataTypeDefinition);
            }
        };

        var query = parseAndBuildQuery(fuzzyQueryString(escape(DataTypeDefinition.DATE.toPrefixString(resolver))));

        assertQueryEquals(
                allAttributesOfGivenType.stream()
                        .map(qname -> qname.toPrefixString(resolver))
                        .map(AlfrescoQualifiedNameTranslator::encode)
                        .map(this::fuzzyQueryString)
                        .collect(joining(" ")),
                query);
    }

    @Test
    public void datatypeDefinitionQueryUsingUnknownDatatypeName()
    {
        parser = new LuceneQueryParserUnderTest() {
            Optional<DataTypeDefinition> datatypeDefinition(String fieldName)
            {
                return Optional.empty();
            }
        };

        var query = parseAndBuildQuery(fuzzyQueryString(escape(DataTypeDefinition.DATE.toPrefixString(resolver))));
        assertQueryEquals(query.toString(), query);
    }

    @Test
    public void datatypeDefinitionQueryUsingDatatypeWithNoAssociatedProperties()
    {
        List<QName> allAttributesOfGivenType = emptyList();

        when(dictionaryService.getAllProperties(DataTypeDefinition.DATE))
                .thenReturn(allAttributesOfGivenType);

        parser = new LuceneQueryParserUnderTest() {
            Optional<DataTypeDefinition> datatypeDefinition(String fieldName)
            {
                var dataTypeDefinition = mock(DataTypeDefinition.class);
                when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.DATE);
                return Optional.of(dataTypeDefinition);
            }
        };

        var query = parseAndBuildQuery(fuzzyQueryString(escape(DataTypeDefinition.DATE.toPrefixString(resolver))));
        assertQueryEquals(query.toString(), query);
    }

    private String fuzzyQueryString(String fieldName, String value)
    {
        return fieldName + ":" + value;
    }

    private String fuzzyQueryString(String fieldName, boolean surrounded)
    {
        return fuzzyQueryString(
                (surrounded ? "(" : "") +
                        fieldName,
                A_TERM + "~" + (int) MIN_SIMILARITY) +
                (surrounded ? ")" : "");
    }

    private String fuzzyQueryString(String fieldName)
    {
        return fuzzyQueryString(fieldName, false);
    }

    @Override
    protected String queryTypeUnderTest()
    {
        return "FuzzyQuery";
    }
}
