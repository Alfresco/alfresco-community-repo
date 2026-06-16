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
package org.alfresco.repo.search.impl.elasticsearch.query.language.lucene;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.search.impl.elasticsearch.AssertionUtils.countIgnoredFields;
import static org.alfresco.repo.search.impl.elasticsearch.query.AlfrescoDefaultTextFields.FULLY_QUALIFIED_NAME_SET;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.search.Query;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import org.alfresco.repo.search.impl.elasticsearch.shared.translator.AlfrescoQualifiedNameTranslator;
import org.alfresco.repo.search.impl.elasticsearch.util.MockNamespaceService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

@SuppressWarnings("PMD")
public abstract class BaseQueryTest
{
    protected final static String A_TERM = "thisIsJustOneTerm";
    protected final static String A_PHRASE = "\"this Is Just a phrase\"";
    protected final static String AN_ID = "1a0b110f-1e09-4ca2-b367-fe25e4964a4e";
    protected final static String A_TAG = "yellow";
    protected final static String A_NODEREF = "workspace://SpacesStore/" + AN_ID;
    protected final static String A_TYPE_QNAME = "{http://www.alfresco.org/model/content/1.0}person";
    protected final static String A_TYPE_SHORT_PREFIX_QNAME = "cm:person";
    protected final static QName A_SUB_TYPE_QNAME = QName
            .createQName("http://www.alfresco.org/model/content/1.0", "engineer");
    protected final static QName A_SUB_TYPE_QNAME2 = QName
            .createQName("http://www.alfresco.org/model/content/1.0", "artist");

    protected final static String AN_ASPECT_QNAME = "{http://www.alfresco.org/model/content/1.0}parentAspect";
    protected final static String AN_ASPECT_SHORT_PREFIX_QNAME = "cm:parentAspect";
    protected final static QName AN_ASPECT_SUB_TYPE_QNAME = QName
            .createQName("http://www.alfresco.org/model/content/1.0", "childAspectOne");
    protected final static QName AN_ASPECT_SUB_TYPE_QNAME2 = QName
            .createQName("http://www.alfresco.org/model/content/1.0", "childAspectTwo");

    protected final static List<String> REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES = List
            .of("aTextField", "anotherTextField", "andAnotherOneTextField");

    protected final static List<String> REQUEST_SPECIFIC_PREFIXED_TEXT_ATTRIBUTES = REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES
            .stream().map(fieldName -> NamespaceService.CONTENT_MODEL_PREFIX + ":" + fieldName)
            .collect(Collectors.toList());

    protected final static List<String> REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES = REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES
            .stream().map(fieldName -> "{" + NamespaceService.CONTENT_MODEL_1_0_URI + "}" + fieldName)
            .collect(Collectors.toList());
    @Rule
    public final ErrorCollector unsupportedFieldsFailureCollector = new ErrorCollector();
    protected LuceneQueryParser parser;
    protected SearchParameters parameters;
    protected NamespacePrefixResolver resolver;
    protected DictionaryService dictionaryService;
    protected SiteService siteService1;

    @Before
    public void setUp()
    {
        var resolver = new MockNamespaceService();
        resolver.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);
        resolver.registerNamespace(NamespaceService.DICTIONARY_MODEL_PREFIX, NamespaceService.DICTIONARY_MODEL_1_0_URI);
        resolver.registerNamespace(NamespaceService.SYSTEM_MODEL_PREFIX, NamespaceService.SYSTEM_MODEL_1_0_URI);
        this.resolver = resolver;

        this.dictionaryService = mock(DictionaryService.class);

        var captor = ArgumentCaptor.forClass(QName.class);
        var captor2 = ArgumentCaptor.forClass(Boolean.class);

        when(dictionaryService.getProperty(captor.capture())).thenAnswer((Answer<PropertyDefinition>) invocation -> {
            var qname = captor.getValue();
            if (REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES.contains(qname.getLocalName()) || FULLY_QUALIFIED_NAME_SET
                    .contains(qname.toString()))
            {
                var propertyDefinition = mock(PropertyDefinition.class);
                when(propertyDefinition.getName()).thenReturn(captor.getValue());
                return propertyDefinition;
            }
            else
            {
                return null;
            }
        });

        when(dictionaryService.getType(captor.capture())).thenAnswer((Answer<TypeDefinition>) invocation -> {
            var qname = captor.getValue();
            if (A_TYPE_QNAME.equals(qname.toString()) || A_SUB_TYPE_QNAME.equals(qname) || A_SUB_TYPE_QNAME2
                    .equals(qname))
            {
                TypeDefinition typeDefinition = mock(TypeDefinition.class);
                when(typeDefinition.getName()).thenReturn(captor.getValue());
                when(typeDefinition.getIncludedInSuperTypeQuery()).thenReturn(true);
                return typeDefinition;
            }
            else
            {
                return null;
            }
        });

        when(dictionaryService.getAspect(captor.capture())).thenAnswer((Answer<AspectDefinition>) invocation -> {
            var qname = captor.getValue();
            if (AN_ASPECT_QNAME.equals(qname.toString())
                    || AN_ASPECT_SUB_TYPE_QNAME.equals(qname)
                    || AN_ASPECT_SUB_TYPE_QNAME2.equals(qname))
            {
                AspectDefinition aspectDefinition = mock(AspectDefinition.class);
                when(aspectDefinition.getName()).thenReturn(captor.getValue());
                when(aspectDefinition.getIncludedInSuperTypeQuery()).thenReturn(true);
                when(aspectDefinition.isAspect()).thenReturn(true);
                return aspectDefinition;
            }
            else
            {
                return null;
            }
        });

        when(dictionaryService.getClass(captor.capture())).thenAnswer((Answer<ClassDefinition>) invocation -> {
            var qname = captor.getValue();
            if (A_TYPE_QNAME.equals(qname.toString()) || A_SUB_TYPE_QNAME.equals(qname) || A_SUB_TYPE_QNAME2
                    .equals(qname))
            {
                TypeDefinition typeDefinition = mock(TypeDefinition.class);
                when(typeDefinition.getName()).thenReturn(captor.getValue());
                when(typeDefinition.getIncludedInSuperTypeQuery()).thenReturn(true);
                return typeDefinition;
            }
            else if (AN_ASPECT_QNAME.equals(qname.toString())
                    || AN_ASPECT_SUB_TYPE_QNAME.equals(qname)
                    || AN_ASPECT_SUB_TYPE_QNAME2.equals(qname))
            {
                AspectDefinition aspectDefinition = mock(AspectDefinition.class);
                when(aspectDefinition.getName()).thenReturn(captor.getValue());
                when(aspectDefinition.getIncludedInSuperTypeQuery()).thenReturn(true);
                when(aspectDefinition.isAspect()).thenReturn(true);
                return aspectDefinition;
            }
            else
            {
                return null;
            }
        });

        when(dictionaryService.getSubTypes(captor.capture(), captor2.capture()))
                .thenAnswer((Answer<List>) invocation -> {
                    var qname = captor.getValue();
                    if (A_TYPE_QNAME.equals(qname.toString()))
                    {
                        List<QName> subTypes = Arrays.asList(captor.getValue(), A_SUB_TYPE_QNAME, A_SUB_TYPE_QNAME2);
                        return subTypes;
                    }
                    else
                    {
                        return null;
                    }
                });

        when(dictionaryService.getSubAspects(captor.capture(), captor2.capture()))
                .thenAnswer((Answer<List>) invocation -> {
                    var qname = captor.getValue();
                    if (AN_ASPECT_QNAME.equals(qname.toString()))
                    {
                        return Arrays.asList(captor.getValue(), AN_ASPECT_SUB_TYPE_QNAME, AN_ASPECT_SUB_TYPE_QNAME2);
                    }
                    else
                    {
                        return null;
                    }
                });

        siteService1 = mock(SiteService.class);

        parameters = new SearchParameters();
        parser = new LuceneQueryParserUnderTest();
    }

    /**
     * Each concrete test subclass, which is supposed to test a specific kind of query must implement this methods
     */
    @Test
    public void fieldsNotSupportedOrNotImplementedShouldThrowAnExceptionOrReturnNull()
    {
        unsupportedFields().forEach(fieldName -> queryBuilderFunctions().forEach(func -> {
            AtomicReference<Query> queryHolder = new AtomicReference<>();
            boolean fieldIgnored;
            try
            {
                long ignoredFieldCount = countIgnoredFields(() -> queryHolder.set(func.apply(fieldName)));
                fieldIgnored = queryHolder.get() == null && ignoredFieldCount == 1;
            }
            catch (UnsupportedOperationException expected)
            {
                fieldIgnored = true;
                // Nothing to be done here, the exception is expected
            }

            if (!fieldIgnored)
            {
                unsupportedFieldsFailureCollector.addError(
                        new AssertionError("Field \"" + fieldName + "\" must be added in the unsupported list of " + getClass()));
            }
        }));
    }

    /**
     * A concrete implementor is supposed to test a specific type of query (e.g. prefix, phrase) and as part of its implementation it must provide a list (a stream) of fields that have been marked as unsupported, regardless the reason (e.g. not yet implemented, not supported, no longer valid in Elasticsearch context)
     */
    protected abstract Stream<String> unsupportedFields();

    /**
     * A concrete implementor is supposed to test a specific type of query (e.g. prefix, phrase) and as part of its implementation it must provide a stream of functions which takes a field name and produces a Lucene {@link Query} instance.
     */
    protected abstract Stream<Function<String, Query>> queryBuilderFunctions();

    /**
     * A concrete implementor is supposed to test a specific type of query (e.g. prefix, phrase) and as part of its implementation it must provide the name of such type.
     */
    protected abstract String queryTypeUnderTest();

    /**
     * Asserts that the "stringified" version of the input Query equals to the expected argument.
     */
    protected void assertQueryEquals(String expected, Query built)
    {
        assertEquals(expected, built.toString());
    }

    protected String fieldQueryString(String fieldName, String value)
    {
        return fieldName + ":" + value;
    }

    protected String toPrefixedForm(String propertyName)
    {
        return Optional.of(propertyName).map(field -> QName.resolveToQName(resolver, field))
                .map(qname -> qname.toPrefixString(resolver)).orElse(propertyName);
    }

    protected String toElasticsearchForm(String propertyName)
    {
        return AlfrescoQualifiedNameTranslator.encode(toPrefixedForm(propertyName));
    }

    protected String quoted(String value)
    {
        return "\"" + value + "\"";
    }

    protected Query parseAndBuildQuery(String query)
    {
        try
        {
            return parser.parse(query);
        }
        catch (UnsupportedOperationException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            throw new RuntimeException(exception);
        }
    }

    /**
     * <li>
     * <ul>
     * If the field name is TEXT
     * </ul>
     * <ul>
     * If the request parameters contain custom text attributes (i.e. If {@link SearchParameters#getTextAttributes()} is not empty
     * </ul>
     * )</li>
     *
     * the built query must have an optional clause for each text attribute.
     */
    protected void textFieldExpansionUsingCustomTextAttributes(List<String> requestSpecificTextAttributes, String inputQueryString, Function<String, String> buildExpectedQuery)
    {
        parameters.setQuery(inputQueryString);
        requestSpecificTextAttributes.forEach(parameters::addTextAttribute);

        var query = parseAndBuildQuery(parameters.getQuery());

        assertQueryEquals(parameters.getTextAttributes().stream().map(field -> QName.resolveToQName(resolver, field))
                .map(qname -> qname.toPrefixString(resolver)).map(AlfrescoQualifiedNameTranslator::encode)
                .map(buildExpectedQuery).collect(joining(" ", "(", ")")), query);
    }

    protected void allFieldExpansionUsingCustomAttributes(List<String> requestSpecificTextAttributes, String inputQueryString, Function<String, String> buildExpectedQuery)
    {
        parameters.setQuery(inputQueryString);
        requestSpecificTextAttributes.forEach(parameters::addAllAttribute);

        var query = parseAndBuildQuery(parameters.getQuery());

        assertQueryEquals(parameters.getAllAttributes().stream().map(field -> QName.resolveToQName(resolver, field))
                .map(qname -> qname.toPrefixString(resolver)).map(AlfrescoQualifiedNameTranslator::encode)
                .map(buildExpectedQuery).collect(joining(" ", "(", ")")), query);
    }

    @Test
    public void datatypeDefinitionQueryUsingPrefixedName()
    {
        var allAttributesOfGivenType = REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES.stream().map(QName::createQName)
                .collect(toList());

        when(dictionaryService.getAllProperties(DataTypeDefinition.DATE)).thenReturn(allAttributesOfGivenType);

        parser = new LuceneQueryParserUnderTest() {
            Optional<DataTypeDefinition> datatypeDefinition(String fieldName)
            {
                var dataTypeDefinition = mock(DataTypeDefinition.class);
                when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.DATE);
                return Optional.of(dataTypeDefinition);
            }
        };

        var query = parseAndBuildQuery(
                fieldQueryString(escape(DataTypeDefinition.DATE.toPrefixString(resolver)), A_TERM));

        assertQueryEquals(allAttributesOfGivenType.stream().map(qname -> qname.toPrefixString(resolver))
                .map(AlfrescoQualifiedNameTranslator::encode).map(fieldName -> fieldQueryString(fieldName, A_TERM))
                .collect(joining(" ")), query);
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

        var query = parseAndBuildQuery(
                fieldQueryString(escape(DataTypeDefinition.DATE.toPrefixString(resolver)), A_TERM));

        assertQueryEquals(query.toString(), query);
    }

    @Test
    public void datatypeDefinitionQueryUsingDatatypeWithNoAssociatedProperties()
    {
        List<QName> allAttributesOfGivenType = emptyList();

        when(dictionaryService.getAllProperties(DataTypeDefinition.DATE)).thenReturn(allAttributesOfGivenType);

        parser = new LuceneQueryParserUnderTest() {
            Optional<DataTypeDefinition> datatypeDefinition(String fieldName)
            {
                var dataTypeDefinition = mock(DataTypeDefinition.class);
                when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.DATE);
                return Optional.of(dataTypeDefinition);
            }
        };

        var query = parseAndBuildQuery(
                fieldQueryString(escape(DataTypeDefinition.DATE.toPrefixString(resolver)), A_TERM));
        assertQueryEquals(query.toString(), query);
    }

    protected String escape(String propertyName)
    {
        return propertyName.replace(":", "\\:")
                .replace("/", "\\/")
                .replace("}", "\\}")
                .replace("{", "\\{");
    }

    protected class LuceneQueryParserUnderTest extends LuceneQueryParser
    {
        public LuceneQueryParserUnderTest()
        {
            super(resolver, dictionaryService, siteService1, parameters);
        }
    }
}
