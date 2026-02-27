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
package org.alfresco.repo.search.impl.elasticsearch.query.language.cmis;

import static org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import static org.alfresco.service.cmr.search.SearchParameters.SortDefinition.SortType.FIELD;

import java.util.Set;

import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Sort;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.search.CMISQueryOptions;
import org.alfresco.opencmis.search.CMISQueryOptions.CMISQueryMode;
import org.alfresco.opencmis.search.CMISQueryParser;
import org.alfresco.opencmis.search.CmisFunctionEvaluationContext;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfig;
import org.alfresco.repo.search.impl.elasticsearch.query.language.EsTypeResolver;
import org.alfresco.repo.search.impl.elasticsearch.query.language.LanguageQueryBuilder;
import org.alfresco.repo.search.impl.querymodel.Order;
import org.alfresco.repo.search.impl.querymodel.Ordering;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilder;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.QueryBuilderContext;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespacePrefixResolver;

/**
 * Query builder to convert {@link SearchParameters} into an Elasticsearch {@link org.opensearch.client.opensearch._types.query_dsl.Query} object.
 */
public class CMISQueryBuilder implements LanguageQueryBuilder
{
    private CMISDictionaryService cmisDictionaryService;
    private CMISQueryParserFactory cmisQueryParserFactory;
    private NamespacePrefixResolver namespacePrefixResolver;
    private DictionaryService dictionaryService;
    private ElasticsearchExactTermSearchConfig exactTermSearchConfig;
    private SiteService siteService;
    private EsTypeResolver esTypeResolver;

    public CMISQueryBuilder(NamespacePrefixResolver namespacePrefixResolver,
            DictionaryService dictionaryService,
            ElasticsearchExactTermSearchConfig exactTermSearchConfig,
            SiteService siteService,
            CMISDictionaryService cmisDictionaryService,
            CMISQueryParserFactory cmisQueryParserFactory,
            EsTypeResolver esTypeResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
        this.dictionaryService = dictionaryService;
        this.exactTermSearchConfig = exactTermSearchConfig;
        this.siteService = siteService;
        this.cmisDictionaryService = cmisDictionaryService;
        this.cmisQueryParserFactory = cmisQueryParserFactory;
        this.esTypeResolver = esTypeResolver;
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getQuery(SearchParameters searchParameters) throws ParseException
    {
        CMISQueryOptions options = CMISQueryOptions.create(searchParameters);
        options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);

        QueryModelFactory factory = new LuceneQueryModelFactory<>();

        CmisFunctionEvaluationContext functionContext = new CmisFunctionEvaluationContext();
        functionContext.setCmisDictionaryService(cmisDictionaryService);
        functionContext.setValidScopes(CmisFunctionEvaluationContext.ALFRESCO_SCOPES);

        CMISQueryParser parser = cmisQueryParserFactory.makeParser(options, cmisDictionaryService, CapabilityJoin.INNERANDOUTER);
        Query queryModel = parser.parse(factory, functionContext);

        sortQuery(queryModel, functionContext, searchParameters);

        Set<String> selectorGroup = queryModel.getSource().getSelectorGroups(functionContext).get(0);
        QueryBuilderContext<org.opensearch.client.opensearch._types.query_dsl.Query, Sort, ParseException> elasticContext = new AFTSForCMISQueryBuilderContext(searchParameters,
                namespacePrefixResolver, dictionaryService, exactTermSearchConfig, siteService, esTypeResolver);

        LuceneQueryBuilder<org.opensearch.client.opensearch._types.query_dsl.Query, Sort, ParseException> queryBuilder = (LuceneQueryBuilder<org.opensearch.client.opensearch._types.query_dsl.Query, Sort, ParseException>) queryModel;
        return queryBuilder.buildQuery(selectorGroup, elasticContext, functionContext);
    }

    public void sortQuery(Query queryModel, CmisFunctionEvaluationContext functionContext, SearchParameters searchParameters)
    {
        if (queryModel.getOrderings() != null)
        {
            for (Ordering ordering : queryModel.getOrderings())
            {
                if (ordering.getColumn().getFunction().getName().equals(PropertyAccessor.NAME))
                {
                    PropertyArgument property = (PropertyArgument) ordering.getColumn().getFunctionArguments().get(PropertyAccessor.ARG_PROPERTY);

                    if (property == null)
                    {
                        throw new IllegalStateException("Could not find property name for ordering.");
                    }

                    String luceneSortField = functionContext.getLuceneFieldName(property.getPropertyName());

                    boolean isAscending = ordering.getOrder().equals(Order.ASCENDING);
                    searchParameters.addSort(new SortDefinition(FIELD, luceneSortField, isAscending));
                }
                else
                {
                    throw new UnsupportedOperationException("Sorting method is unsupported: " + ordering.getColumn().getFunction().getName());
                }
            }
        }

    }
}
