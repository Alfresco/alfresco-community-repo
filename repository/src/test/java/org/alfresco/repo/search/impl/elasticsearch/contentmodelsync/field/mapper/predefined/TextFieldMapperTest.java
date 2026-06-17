/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined;

import static java.lang.String.format;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.utils.MapperTestUtils.TEST_PROPERTY_NAME;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.utils.MapperTestUtils.assertMapsEquals;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.utils.MapperTestUtils.mockPropertyDefinition;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.utils.MapperTestUtils.readMappingFromFile;

import java.util.List;
import java.util.Map;

import com.google.testing.junit.testparameterinjector.TestParameter;
import com.google.testing.junit.testparameterinjector.TestParameterInjector;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;

import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfig;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchFieldAnalyzersConfig;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.ElasticsearchFieldMapper.FieldMappingContext;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.utils.MapperTestUtils;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

@RunWith(TestParameterInjector.class)
public class TextFieldMapperTest
{
    private final ElasticsearchExactTermSearchConfig exactTermSearchConfig = mock();
    private final ElasticsearchFieldAnalyzersConfig elasticsearchFieldAnalyzersConfig = mock();

    private final TextFieldMapper mapper = new TextFieldMapper(exactTermSearchConfig, elasticsearchFieldAnalyzersConfig);

    @Test
    public void shouldBeAbleToMapText(@TestParameter TextType textType)
    {
        assertTrue(mapper.canMap(new FieldMappingContext(mockPropertyDefinition(textType.dataTypeName))));
    }

    @Test
    @SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
    public void shouldNotBeAbleToMapOtherDatatypes()
    {
        List<QName> unsupportedDatatypes = List.of(DataTypeDefinition.INT, DataTypeDefinition.LONG, DataTypeDefinition.BOOLEAN, DataTypeDefinition.DATETIME);

        unsupportedDatatypes.stream()
                .map(MapperTestUtils::mockPropertyDefinition)
                .map(FieldMappingContext::new)
                .map(mapper::canMap)
                .forEach(Assert::assertFalse);
    }

    @Test
    public void shouldProperlyMapProperties(@TestParameter TextType textType, @TestParameter IndexTokenisationMode indexTokenisationMode, @TestParameter Facetable facetable,
            @TestParameter AnalyzersType analyzersType)
    {
        when(exactTermSearchConfig.isExactTermSearchEnabled(any())).thenReturn(false);
        analyzersType.setupConfig(elasticsearchFieldAnalyzersConfig);

        var expectedMapping = getExpectedMappingWhenExactTermSearchEnabled(textType, indexTokenisationMode, facetable, analyzersType, analyzersType.expectedMappingsDirectory);

        PropertyDefinition propertyDefinition = mockPropertyDefinition(textType.dataTypeName, TEST_PROPERTY_NAME);

        when(propertyDefinition.getIndexTokenisationMode()).thenReturn(indexTokenisationMode);
        when(propertyDefinition.getFacetable()).thenReturn(facetable);

        FieldMappingContext context = new FieldMappingContext(propertyDefinition);

        var actualMapping = mapper.buildMapping(context)
                .asMap();

        assertMapsEquals(expectedMapping, actualMapping);
    }

    @Test
    public void shouldProperlyMapPropertiesWhenExactTermSearchEnabled(@TestParameter TextType textType, @TestParameter IndexTokenisationMode indexTokenisationMode,
            @TestParameter Facetable facetable)
    {
        when(exactTermSearchConfig.isExactTermSearchEnabled(any())).thenReturn(true);
        AnalyzersType.ASYMMETRIC.setupConfig(elasticsearchFieldAnalyzersConfig);

        var expectedMapping = getExpectedMappingWhenExactTermSearchEnabled(textType, indexTokenisationMode, facetable, AnalyzersType.ASYMMETRIC, "exactTermSearch");

        PropertyDefinition propertyDefinition = mockPropertyDefinition(textType.dataTypeName, TEST_PROPERTY_NAME);

        when(propertyDefinition.getIndexTokenisationMode()).thenReturn(indexTokenisationMode);
        when(propertyDefinition.getFacetable()).thenReturn(facetable);

        FieldMappingContext context = new FieldMappingContext(propertyDefinition);

        var actualMapping = mapper.buildMapping(context)
                .asMap();

        assertMapsEquals(expectedMapping, actualMapping);
    }

    @Test
    public void shouldProperlyMapPropertiesWhenExactTermSearchEnabledWithDefaultAnalyzers(@TestParameter TextType textType,
            @TestParameter IndexTokenisationMode indexTokenisationMode, @TestParameter Facetable facetable)
    {
        when(exactTermSearchConfig.isExactTermSearchEnabled(any())).thenReturn(true);
        AnalyzersType.DEFAULT.setupConfig(elasticsearchFieldAnalyzersConfig);

        var expectedMapping = getExpectedMappingWhenExactTermSearchEnabled(textType, indexTokenisationMode, facetable, AnalyzersType.DEFAULT, "exactTermSearchDefaultAnalyzers");

        PropertyDefinition propertyDefinition = mockPropertyDefinition(textType.dataTypeName, TEST_PROPERTY_NAME);

        when(propertyDefinition.getIndexTokenisationMode()).thenReturn(indexTokenisationMode);
        when(propertyDefinition.getFacetable()).thenReturn(facetable);

        FieldMappingContext context = new FieldMappingContext(propertyDefinition);

        var actualMapping = mapper.buildMapping(context)
                .asMap();

        assertMapsEquals(expectedMapping, actualMapping);
    }

    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    private Map<?, ?> getExpectedMappingWhenExactTermSearchEnabled(TextType textType, IndexTokenisationMode indexTokenisationMode, Facetable facetable, AnalyzersType analyzersType,
            String expectedMappingsDirectory)
    {
        if (indexTokenisationMode == IndexTokenisationMode.FALSE)
        {
            return readMappingFromFile("text/not_tokenized");
        }

        String analyzerType = textType == TextType.CONTENT ? "content" : "text";
        String filePrefix = analyzersType.isTextTypeSpecific ? format("analyzers_%s_", analyzerType) : "";

        if (shouldCreateUntokenizedAndTokenizedMappings(indexTokenisationMode, facetable))
        {
            return readMappingFromFile(format("text/%s/%stokenization_both", expectedMappingsDirectory, filePrefix));
        }

        if (indexTokenisationMode == IndexTokenisationMode.TRUE)
        {
            return readMappingFromFile(format("text/%s/%stokenization_true", expectedMappingsDirectory, filePrefix));
        }

        throw new RuntimeException();
    }

    private boolean shouldCreateUntokenizedAndTokenizedMappings(IndexTokenisationMode indexTokenisationMode, Facetable facetable)
    {
        return indexTokenisationMode == IndexTokenisationMode.BOTH || indexTokenisationMode == IndexTokenisationMode.TRUE && facetable == Facetable.TRUE;
    }

    public enum AnalyzersType
    {
        ASYMMETRIC(analyzer -> analyzer.getName()
                .contains("_index")
                || analyzer.getName()
                        .contains("_query"),
                "customAsymmetricAnalyzers", true), ASYMMETRIC_AND_QUOTE(analyzer -> analyzer.getName()
                        .contains("_quote") || ASYMMETRIC.analyzerMatcher.matches(analyzer), "customAsymmetricAndQuoteAnalyzers", true), SYMMETRIC(
                                analyzer -> analyzer.getName()
                                        .endsWith("_text")
                                        || analyzer.getName()
                                                .endsWith("_content"),
                                "customSymmetricAnalyzers", true), DEFAULT(analyzer -> false, "defaultSymmetricAnalyzers", false);

        private final ArgumentMatcher<ElasticsearchAnalyzer> analyzerMatcher;

        private final String expectedMappingsDirectory;

        private final boolean isTextTypeSpecific;

        AnalyzersType(ArgumentMatcher<ElasticsearchAnalyzer> analyzerMatcher, String expectedMappingsDirectory, boolean isTextTypeSpecific)
        {
            this.analyzerMatcher = analyzerMatcher;
            this.expectedMappingsDirectory = expectedMappingsDirectory;
            this.isTextTypeSpecific = isTextTypeSpecific;
        }

        public void setupConfig(ElasticsearchFieldAnalyzersConfig config)
        {
            when(config.isAnalyzerDefinedInElasticsearch(argThat(analyzerMatcher))).thenReturn(true);
        }
    }

    public enum TextType
    {
        CONTENT(DataTypeDefinition.CONTENT), TEXT(DataTypeDefinition.TEXT), MULTILANGUAGE_TEXT(DataTypeDefinition.MLTEXT), ENCRYPTED(DataTypeDefinition.ENCRYPTED);

        private final QName dataTypeName;

        TextType(QName dataTypeName)
        {
            this.dataTypeName = dataTypeName;
        }
    }
}
