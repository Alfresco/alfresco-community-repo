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
package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import static org.alfresco.repo.search.impl.QueryParserUtils.matchPropertyDefinition;

import java.util.Optional;

import org.apache.lucene.search.Query;

import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfig;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.LuceneQueryParser;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespacePrefixResolver;

/** The parser of {@link AFTSQueryParserAdaptor} (AFTS and CMIS): date values are converted with {@link DateValueConverter}. */
class AFTSLuceneQueryParser extends LuceneQueryParser
{
    private final NamespacePrefixResolver namespaceResolver;
    private final DictionaryService dictionaryService;
    private final SearchParameters searchParameters;

    AFTSLuceneQueryParser(NamespacePrefixResolver namespaceResolver, DictionaryService dictionaryService, SiteService siteService,
            SearchParameters searchParameters, ElasticsearchExactTermSearchConfig exactTermSearchConfig)
    {
        super(namespaceResolver, dictionaryService, siteService, searchParameters, exactTermSearchConfig);
        this.namespaceResolver = namespaceResolver;
        this.dictionaryService = dictionaryService;
        this.searchParameters = searchParameters;
    }

    @Override
    protected Optional<Query> propertyFieldQuery(String fieldName, String text, boolean quoted, boolean exactTermSearch, boolean untokenisedSearch)
    {
        if (!isDateProperty(fieldName))
        {
            return super.propertyFieldQuery(fieldName, text, quoted, exactTermSearch, untokenisedSearch);
        }
        String date = DateValueConverter.withNumericMonth(text);
        if (DateValueConverter.isYearOrMonth(date))
        {
            return propertyRangeQuery(fieldName, date, date, true, true, exactTermSearch);
        }
        return super.propertyFieldQuery(fieldName, date, quoted, exactTermSearch, untokenisedSearch);
    }

    @Override
    protected Optional<Query> propertyRangeQuery(String fieldName, String part1, String part2, boolean startInclusive, boolean endInclusive, boolean exactTermSearch)
    {
        if (!isDateProperty(fieldName))
        {
            return super.propertyRangeQuery(fieldName, part1, part2, startInclusive, endInclusive, exactTermSearch);
        }
        return super.propertyRangeQuery(fieldName, DateValueConverter.toRangeBound(part1), DateValueConverter.toRangeBound(part2), startInclusive, endInclusive, exactTermSearch);
    }

    /** A d:date or d:datetime property, resolved as the base parser resolves property fields. */
    private boolean isDateProperty(String fieldName)
    {
        return stripPrefixAndSuffix(fieldName)
                .map(name -> matchPropertyDefinition(searchParameters.getNamespace(), namespaceResolver, dictionaryService, name))
                .map(PropertyDefinition::getDataType)
                .map(DataTypeDefinition::getName)
                .filter(type -> DataTypeDefinition.DATE.equals(type) || DataTypeDefinition.DATETIME.equals(type))
                .isPresent();
    }
}
