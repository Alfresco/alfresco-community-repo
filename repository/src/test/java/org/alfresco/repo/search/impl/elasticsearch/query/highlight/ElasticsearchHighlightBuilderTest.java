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
package org.alfresco.repo.search.impl.elasticsearch.query.highlight;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensearch.client.opensearch.core.search.Highlight;
import org.opensearch.client.opensearch.core.search.HighlightField;

import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.search.FieldHighlightParameters;
import org.alfresco.service.cmr.search.GeneralHighlightParameters;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.QName;

/** Unit tests for {@link ElasticsearchHighlightBuilder}. */
@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchHighlightBuilderTest
{
    private static final String NAMESPACE = "http://www.alfresco.org/model/content/1.0";

    @InjectMocks
    ElasticsearchHighlightBuilder elasticsearchHighlightBuilder;
    @Mock
    NamespaceDAO namespaceDAO;
    @Mock
    DictionaryService dictionaryService;
    @Mock
    SearchParameters searchParameters;
    @Mock
    GeneralHighlightParameters generalHighlightParameters;
    @Mock
    FieldHighlightParameters fieldHighlightParametersA;
    @Mock
    FieldHighlightParameters fieldHighlightParametersB;
    @Mock
    PropertyDefinition propertyDefinitionA;
    @Mock
    PropertyDefinition propertyDefinitionB;

    @Before
    public void setUp()
    {
        given(namespaceDAO.getNamespaceURI("cm")).willReturn(NAMESPACE);
        given(namespaceDAO.getPrefixes(NAMESPACE)).willReturn(List.of("cm"));

        given(fieldHighlightParametersA.getField()).willReturn("cm:fieldA");
        given(fieldHighlightParametersB.getField()).willReturn("cm:fieldB");
        given(dictionaryService.getProperty(QName.createQName("{%s}fieldA".formatted(NAMESPACE)))).willReturn(propertyDefinitionA);
        given(dictionaryService.getProperty(QName.createQName("{%s}fieldB".formatted(NAMESPACE)))).willReturn(propertyDefinitionB);
        given(generalHighlightParameters.getSnippetCount()).willReturn(null);
        given(generalHighlightParameters.getFragmentSize()).willReturn(null);
        given(fieldHighlightParametersA.getSnippetCount()).willReturn(null);
        given(fieldHighlightParametersA.getFragmentSize()).willReturn(null);
        given(fieldHighlightParametersB.getSnippetCount()).willReturn(null);
        given(fieldHighlightParametersB.getFragmentSize()).willReturn(null);
    }

    @Test
    public void testGetHighlightBuilder_supplyFields_returnsList()
    {
        given(searchParameters.getHighlight()).willReturn(generalHighlightParameters);
        given(generalHighlightParameters.getFields()).willReturn(List.of(fieldHighlightParametersA, fieldHighlightParametersB));

        Highlight highlightBuilder = elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters);

        HighlightField fieldA = new HighlightField.Builder().field("cm%3AfieldA").build();
        HighlightField fieldB = new HighlightField.Builder().field("cm%3AfieldB").build();
        assertEquals("Unexpected list of fields without pre- and postfix to highlight.", List.of(fieldA.field(), fieldB.field()), List.of(highlightBuilder.fields().keySet().stream().toList().get(0), highlightBuilder.fields().keySet().stream().toList().get(1)));
    }

    @Test
    public void testGetHighlightBuilder_noHighlightFields_returnsEmptyList()
    {
        given(searchParameters.getHighlight()).willReturn(generalHighlightParameters);

        Highlight highlightBuilder = elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters);

        assertEquals("Expected no fields when no fields requested.", true, highlightBuilder.fields().values().isEmpty());
    }

    @Test
    public void testGetHighlightBuilder_noHighlightParameters_returnsNull()
    {
        Highlight highlightBuilder = elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters);

        assertNull("Expected null to be returned with no exception.", highlightBuilder);
    }

    @Test
    public void testGetHighlightBuilder_oneFieldPreAndPostfix()
    {
        given(searchParameters.getHighlight()).willReturn(generalHighlightParameters);
        given(generalHighlightParameters.getFields()).willReturn(List.of(fieldHighlightParametersA, fieldHighlightParametersB));
        given(fieldHighlightParametersA.getPrefix()).willReturn("(");
        given(fieldHighlightParametersA.getPostfix()).willReturn(")");

        Highlight highlightBuilder = elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters);

        HighlightField fieldA = new HighlightField.Builder().field("cm%3AfieldA").preTags("(").postTags(")").build();
        HighlightField fieldB = new HighlightField.Builder().field("cm%3AfieldB").build();
        assertTrue(highlightBuilder.preTags().isEmpty());
        assertTrue(highlightBuilder.postTags().isEmpty());
        assertEquals("Unexpected list of pre- and postfixes in fields to highlight.", List.of(fieldA.preTags().get(0), fieldA.postTags().get(0)), List.of(highlightBuilder.fields().values().stream().toList().get(0).preTags().get(0), highlightBuilder.fields().values().stream().toList().get(0).postTags().get(0)));
        assertEquals("Unexpected list of pre- and postfixes in fields to highlight.", List.of(fieldB.preTags(), fieldB.postTags()), List.of(highlightBuilder.fields().values().stream().toList().get(1).preTags(), highlightBuilder.fields().values().stream().toList().get(1).postTags()));
    }

    @Test
    public void testGetHighlightBuilder_multipleFieldPreAndPostfixes()
    {
        given(searchParameters.getHighlight()).willReturn(generalHighlightParameters);
        given(generalHighlightParameters.getFields()).willReturn(List.of(fieldHighlightParametersA, fieldHighlightParametersB));
        given(fieldHighlightParametersA.getPrefix()).willReturn("(");
        given(fieldHighlightParametersA.getPostfix()).willReturn(")");
        given(fieldHighlightParametersB.getPrefix()).willReturn("¿");
        given(fieldHighlightParametersB.getPostfix()).willReturn("?");

        Highlight highlightBuilder = elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters);

        HighlightField fieldA = new HighlightField.Builder().field("cm%3AfieldA").preTags("(").postTags(")").build();

        HighlightField fieldB = new HighlightField.Builder().field("cm%3AfieldB").preTags("¿").postTags("?").build();

        assertTrue(highlightBuilder.preTags().isEmpty());
        assertTrue(highlightBuilder.postTags().isEmpty());
        assertEquals("Unexpected list of pre- and postfixes in fields to highlight.", List.of(fieldA.preTags().get(0), fieldA.postTags().get(0)), List.of(highlightBuilder.fields().values().stream().toList().get(0).preTags().get(0), highlightBuilder.fields().values().stream().toList().get(0).postTags().get(0)));
        assertEquals("Unexpected list of pre- and postfixes in fields to highlight.", List.of(fieldB.preTags().get(0), fieldB.postTags().get(0)), List.of(highlightBuilder.fields().values().stream().toList().get(1).preTags().get(0), highlightBuilder.fields().values().stream().toList().get(1).postTags().get(0)));
    }

    @Test
    public void testGetHighlightBuilder_generalPreAndPostfix()
    {
        given(searchParameters.getHighlight()).willReturn(generalHighlightParameters);
        given(generalHighlightParameters.getFields()).willReturn(List.of(fieldHighlightParametersA, fieldHighlightParametersB));
        given(generalHighlightParameters.getPrefix()).willReturn("(");
        given(generalHighlightParameters.getPostfix()).willReturn(")");

        Highlight highlightBuilder = elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters);

        HighlightField fieldA = new HighlightField.Builder().field("cm%3AfieldA").build();
        HighlightField fieldB = new HighlightField.Builder().field("cm%3AfieldB").build();
        assertEquals("(", highlightBuilder.preTags().get(0));
        assertEquals(")", highlightBuilder.postTags().get(0));
        assertEquals("Unexpected list of fields without pre- and postfix to highlight.", List.of(fieldA.field(), fieldB.field()), List.of(highlightBuilder.fields().keySet().stream().toList().get(0), highlightBuilder.fields().keySet().stream().toList().get(1)));
    }

    @Test
    public void testGetHighlightBuilder_emptyFieldPreAndPostfix()
    {
        given(searchParameters.getHighlight()).willReturn(generalHighlightParameters);
        given(generalHighlightParameters.getFields()).willReturn(List.of(fieldHighlightParametersA, fieldHighlightParametersB));
        given(fieldHighlightParametersA.getPrefix()).willReturn(StringUtils.EMPTY);
        given(fieldHighlightParametersA.getPostfix()).willReturn(StringUtils.EMPTY);

        Highlight highlightBuilder = elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters);

        HighlightField fieldA = new HighlightField.Builder().field("cm%3AfieldA").preTags(StringUtils.EMPTY).postTags(StringUtils.EMPTY).build();

        HighlightField fieldB = new HighlightField.Builder().field("cm%3AfieldB").build();

        assertEquals("Unexpected list of fields without pre- and postfix to highlight.", List.of(fieldA.preTags().get(0), fieldA.postTags().get(0)), List.of(highlightBuilder.fields().values().stream().toList().get(0).preTags().get(0), highlightBuilder.fields().values().stream().toList().get(0).postTags().get(0)));
        assertEquals("Unexpected list of fields without pre- and postfix to highlight.", List.of(fieldB.preTags(), fieldA.postTags()), List.of(highlightBuilder.fields().values().stream().toList().get(1).preTags(), highlightBuilder.fields().values().stream().toList().get(0).postTags()));

    }

    @Test
    public void testGetHighlightBuilder_emptyGeneralPreAndPostfix()
    {
        given(searchParameters.getHighlight()).willReturn(generalHighlightParameters);
        given(generalHighlightParameters.getFields()).willReturn(List.of(fieldHighlightParametersA, fieldHighlightParametersB));
        given(generalHighlightParameters.getPrefix()).willReturn(StringUtils.EMPTY);
        given(generalHighlightParameters.getPostfix()).willReturn(StringUtils.EMPTY);

        Highlight highlightBuilder = elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters);

        HighlightField fieldA = new HighlightField.Builder().field("cm%3AfieldA").build();
        HighlightField fieldB = new HighlightField.Builder().field("cm%3AfieldB").build();
        assertEquals(StringUtils.EMPTY, highlightBuilder.preTags().get(0));
        assertEquals(StringUtils.EMPTY, highlightBuilder.postTags().get(0));
        assertEquals("Unexpected list of fields without pre- and postfix to highlight.", List.of(fieldA.field(), fieldB.field()), List.of(highlightBuilder.fields().keySet().stream().toList().get(0), highlightBuilder.fields().keySet().stream().toList().get(1)));
    }

    @Test
    public void testGetHighlightBuilder_nullGeneralPreAndPostfix()
    {
        given(searchParameters.getHighlight()).willReturn(generalHighlightParameters);
        given(generalHighlightParameters.getFields()).willReturn(List.of(fieldHighlightParametersA, fieldHighlightParametersB));
        given(generalHighlightParameters.getPrefix()).willReturn(null);
        given(generalHighlightParameters.getPostfix()).willReturn(null);

        Highlight highlightBuilder = elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters);

        HighlightField fieldA = new HighlightField.Builder().field("cm%3AfieldA").build();
        HighlightField fieldB = new HighlightField.Builder().field("cm%3AfieldB").build();
        assertTrue(highlightBuilder.preTags().isEmpty());
        assertTrue(highlightBuilder.postTags().isEmpty());
        assertEquals("Unexpected list of fields without pre- and postfix to highlight.", List.of(fieldA.field(), fieldB.field()), List.of(highlightBuilder.fields().keySet().stream().toList().get(0), highlightBuilder.fields().keySet().stream().toList().get(1)));
    }

    @Test
    public void testGetHighlightBuilder_generalSnippetCount()
    {
        given(searchParameters.getHighlight()).willReturn(generalHighlightParameters);
        given(generalHighlightParameters.getFields()).willReturn(List.of(fieldHighlightParametersA, fieldHighlightParametersB));
        given(generalHighlightParameters.getSnippetCount()).willReturn(5);

        Highlight highlightBuilder = elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters);

        HighlightField fieldA = new HighlightField.Builder().field("cm%3AfieldA").build();
        HighlightField fieldB = new HighlightField.Builder().field("cm%3AfieldB").build();
        assertEquals(5, highlightBuilder.numberOfFragments().intValue());
        assertEquals("Unexpected list of fields without pre- and postfix to highlight.", List.of(fieldA.field(), fieldB.field()), List.of(highlightBuilder.fields().keySet().stream().toList().get(0), highlightBuilder.fields().keySet().stream().toList().get(1)));
    }

    @Test
    public void testGetHighlightBuilder_fieldSpecificSnippetCount()
    {
        given(searchParameters.getHighlight()).willReturn(generalHighlightParameters);
        given(generalHighlightParameters.getFields()).willReturn(List.of(fieldHighlightParametersA, fieldHighlightParametersB));
        given(fieldHighlightParametersA.getSnippetCount()).willReturn(5);

        Highlight highlightBuilder = elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters);

        HighlightField fieldA = new HighlightField.Builder().field("cm%3AfieldA").numberOfFragments(5).build();
        HighlightField fieldB = new HighlightField.Builder().field("cm%3AfieldB").build();
        assertNull(highlightBuilder.numberOfFragments());
        assertEquals("Unexpected list of fields without pre- and postfix to highlight.", fieldA.numberOfFragments(), highlightBuilder.fields().values().stream().toList().get(0).numberOfFragments());
        assertEquals("Unexpected list of fields without pre- and postfix to highlight.", fieldB.numberOfFragments(), highlightBuilder.fields().values().stream().toList().get(1).numberOfFragments());
    }

    @Test
    public void testGetHighlightBuilder_generalFragmentSize()
    {
        given(searchParameters.getHighlight()).willReturn(generalHighlightParameters);
        given(generalHighlightParameters.getFields()).willReturn(List.of(fieldHighlightParametersA, fieldHighlightParametersB));
        given(generalHighlightParameters.getFragmentSize()).willReturn(50);

        Highlight highlightBuilder = elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters);

        HighlightField fieldA = new HighlightField.Builder().field("cm%3AfieldA").build();
        HighlightField fieldB = new HighlightField.Builder().field("cm%3AfieldB").build();
        assertEquals(50, highlightBuilder.fragmentSize().intValue());
        assertEquals("Unexpected list of fields without pre- and postfix to highlight.", List.of(fieldA.field(), fieldB.field()), List.of(highlightBuilder.fields().keySet().stream().toList().get(0), highlightBuilder.fields().keySet().stream().toList().get(1)));
    }

    @Test
    public void testGetHighlightBuilder_fieldSpecificFragmentSize()
    {
        given(searchParameters.getHighlight()).willReturn(generalHighlightParameters);
        given(generalHighlightParameters.getFields()).willReturn(List.of(fieldHighlightParametersA, fieldHighlightParametersB));
        given(fieldHighlightParametersB.getFragmentSize()).willReturn(50);

        Highlight highlightBuilder = elasticsearchHighlightBuilder.getHighlightBuilder(searchParameters);

        HighlightField fieldA = new HighlightField.Builder().field("cm%3AfieldA").build();
        HighlightField fieldB = new HighlightField.Builder().field("cm%3AfieldB").fragmentSize(50).build();
        assertNull(highlightBuilder.fragmentSize());
        assertEquals("Unexpected list of fields without pre- and postfix to highlight.", fieldA.fragmentSize(), highlightBuilder.fields().values().stream().toList().get(0).fragmentSize());
        assertEquals("Unexpected list of fields without pre- and postfix to highlight.", fieldB.fragmentSize().intValue(), highlightBuilder.fields().values().stream().toList().get(1).fragmentSize().intValue());
    }
}
