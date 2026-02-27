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

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined;

import static java.util.Optional.ofNullable;

import static org.alfresco.repo.dictionary.IndexTokenisationMode.BOTH;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.ElasticsearchFieldMapping.mappingWithMetadata;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer.AnalyzerPurpose.ASYMMETRIC_INDEX_ANALYZER;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer.AnalyzerPurpose.QUOTE_ANALYZER;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer.AnalyzerPurpose.SEARCH_ANALYZER;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer.AnalyzerPurpose.SYMMETRIC_INDEX_ANALYZER;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer.AnalyzerType.LOCALE_ANALYZER;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer.AnalyzerType.LOCALE_CROSS_ANALYZER;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer.DEFAULT_ANALYZER;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer.DEFAULT_EXACT_TERM_SEARCH_ANALYZER;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchPropertyKey.COPY_TO;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchType.KEYWORD;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchType.TEXT;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util.TextArray.textArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfig;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchFieldAnalyzersConfig;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.ElasticsearchFieldMapping;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer.AnalyzerType;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer.TextType;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchType;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util.Property;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util.TextArray;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;

@Component
public class TextFieldMapper implements PredefinedFieldMapper
{

    private static final Set<QName> supportedTypes = Set.of(DataTypeDefinition.TEXT, DataTypeDefinition.MLTEXT, DataTypeDefinition.ENCRYPTED, DataTypeDefinition.CONTENT);

    private final ElasticsearchExactTermSearchConfig exactTermSearchConfig;
    private final ElasticsearchFieldAnalyzersConfig fieldAnalyzersConfig;

    public TextFieldMapper(ElasticsearchExactTermSearchConfig exactTermSearchConfig, ElasticsearchFieldAnalyzersConfig fieldAnalyzersConfig)
    {
        this.exactTermSearchConfig = exactTermSearchConfig;
        this.fieldAnalyzersConfig = fieldAnalyzersConfig;
    }

    @Override
    public boolean canMap(FieldMappingContext mappingContext)
    {
        return supportedTypes.contains(mappingContext.propertyDefinition().getDataType().getName());
    }

    @Override
    public ElasticsearchFieldMapping buildMapping(FieldMappingContext context)
    {
        return switch (context.propertyDefinition().getIndexTokenisationMode())
        {
        case FALSE -> buildKeywordMapping(context);
        case TRUE, BOTH -> buildTextMapping(context);
        };
    }

    private ElasticsearchFieldMapping buildKeywordMapping(FieldMappingContext context)
    {
        return mappingWithMetadata(context.name(), KEYWORD).withAlias();
    }

    private ElasticsearchFieldMapping buildTextMapping(FieldMappingContext context)
    {
        ElasticsearchFieldMapping mapping = mappingWithMetadata(context.name(), TEXT);
        TextArray copyToDestinations = textArray(COPY_TO);

        TextType textType = getTextType(context);

        mapping.baseMapping()
                .addChildren(getFieldAnalyzers(textType));

        if (exactTermSearchConfig.isExactTermSearchEnabled(context.propertyDefinition()))
        {
            mapping.exactTermSearchMapping()
                    .addChild(TEXT)
                    .addChildren(getExactTermSearchFieldAnalyzers(textType));

            copyToDestinations.add(context.name()
                    .exactTermSearch());
        }

        if (shouldCreateNotTokenizedIndex(context))
        {
            mapping.notTokenizedMapping()
                    .addChild(ElasticsearchType.KEYWORD);

            copyToDestinations.add(context.name()
                    .untokenized());
        }

        if (!copyToDestinations.isEmpty())
        {
            mapping.baseMapping()
                    .addChild(copyToDestinations);
        }

        return mapping;
    }

    private static TextType getTextType(FieldMappingContext context)
    {
        return isContent(context) ? TextType.CONTENT : TextType.TEXT;
    }

    private static boolean isContent(FieldMappingContext context)
    {
        return context.propertyDefinition()
                .getDataType()
                .getName()
                .equals(DataTypeDefinition.CONTENT);
    }

    public List<Property> getFieldAnalyzers(TextType textType)
    {
        return getFieldAnalyzers(textType, LOCALE_ANALYZER, DEFAULT_ANALYZER);
    }

    public List<Property> getExactTermSearchFieldAnalyzers(TextType textType)
    {
        return getFieldAnalyzers(textType, LOCALE_CROSS_ANALYZER, DEFAULT_EXACT_TERM_SEARCH_ANALYZER);
    }

    /**
     * Finds proper analyzer for field. It uses the following flow:
     *
     * <ul>
     * <li>Checks if a custom asymmetric text analysis is defined for the field.</li>
     * <li>Checks if a custom symmetric text analysis is defined for the field.</li>
     * <li>Fallbacks to the default analyzer (i.e. standard)</li>
     * </ul>
     *
     * @return List of relevant analyzers
     */
    private List<Property> getFieldAnalyzers(TextType textType, AnalyzerType analyzerType, ElasticsearchAnalyzer defaultAnalyzer)
    {
        List<Property> analyzers = new ArrayList<>();

        ElasticsearchAnalyzer customAsymmetricIndexAnalyzer = ElasticsearchAnalyzer.of(textType, analyzerType, ASYMMETRIC_INDEX_ANALYZER);
        ElasticsearchAnalyzer customAsymmetricSearchAnalyzer = ElasticsearchAnalyzer.of(textType, analyzerType, SEARCH_ANALYZER);

        if (fieldAnalyzersConfig.isAnalyzerDefinedInElasticsearch(customAsymmetricIndexAnalyzer) && fieldAnalyzersConfig.isAnalyzerDefinedInElasticsearch(customAsymmetricSearchAnalyzer))
        {
            analyzers.add(customAsymmetricIndexAnalyzer);
            analyzers.add(customAsymmetricSearchAnalyzer);

            ElasticsearchAnalyzer customQuoteAnalyzer = ElasticsearchAnalyzer.of(textType, analyzerType, QUOTE_ANALYZER);

            if (fieldAnalyzersConfig.isAnalyzerDefinedInElasticsearch(customQuoteAnalyzer))
            {
                analyzers.add(customQuoteAnalyzer);
            }

            return analyzers;
        }

        ElasticsearchAnalyzer customSymmetricAnalyzer = ElasticsearchAnalyzer.of(textType, analyzerType, SYMMETRIC_INDEX_ANALYZER);

        if (fieldAnalyzersConfig.isAnalyzerDefinedInElasticsearch(customSymmetricAnalyzer))
        {
            analyzers.add(customSymmetricAnalyzer);

            return analyzers;
        }

        analyzers.add(defaultAnalyzer);

        return analyzers;
    }

    private boolean shouldCreateNotTokenizedIndex(FieldMappingContext context)
    {
        return context.propertyDefinition().getIndexTokenisationMode() == BOTH || ofNullable(context.propertyDefinition()
                .getFacetable()).map(option -> option.equals(Facetable.TRUE))
                        .orElse(false);
    }
}
