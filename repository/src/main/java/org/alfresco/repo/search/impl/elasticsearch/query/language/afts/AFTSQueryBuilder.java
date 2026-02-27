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
package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import static org.alfresco.repo.search.impl.parsers.FTSParser.Mode.DEFAULT_CONJUNCTION;
import static org.alfresco.repo.search.impl.parsers.FTSParser.Mode.DEFAULT_DISJUNCTION;
import static org.alfresco.repo.search.impl.parsers.FTSQueryParser.RerankPhase.SINGLE_PASS;
import static org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective.AND;
import static org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective.OR;

import java.util.ArrayList;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Sort;

import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfig;
import org.alfresco.repo.search.impl.elasticsearch.query.language.EsTypeResolver;
import org.alfresco.repo.search.impl.elasticsearch.query.language.LanguageQueryBuilder;
import org.alfresco.repo.search.impl.parsers.AlfrescoFunctionEvaluationContext;
import org.alfresco.repo.search.impl.parsers.FTSParser;
import org.alfresco.repo.search.impl.parsers.FTSQueryParser;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilder;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.QueryBuilderContext;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.Operator;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespacePrefixResolver;

/**
 * Object to handle translation from an Alfresco Full Text Search (AFTS) query to an Elasticsearch query.
 */
public class AFTSQueryBuilder implements LanguageQueryBuilder
{
    private final NamespacePrefixResolver namespace;
    private final DictionaryService dictionaryService;
    private final ElasticsearchExactTermSearchConfig exactTermSearchConfig;
    private final SiteService siteService;
    private final EsTypeResolver esTypeResolver;

    public AFTSQueryBuilder(NamespacePrefixResolver namespace,
            DictionaryService dictionaryService,
            ElasticsearchExactTermSearchConfig exactTermSearchConfig,
            SiteService siteService,
            EsTypeResolver esTypeResolver)
    {
        this.namespace = namespace;
        this.dictionaryService = dictionaryService;
        this.exactTermSearchConfig = exactTermSearchConfig;
        this.siteService = siteService;
        this.esTypeResolver = esTypeResolver;
    }

    /**
     * Convert an AFTS query to an Elasticsearch query.
     *
     * @param searchParameters
     *            The query to convert.
     * @return The Elasticsearch query builder to use in a Elasticsearch search request.
     * @throws ParseException
     *             If there is a problem interpreting the AFTS query.
     */
    public org.opensearch.client.opensearch._types.query_dsl.Query getQuery(SearchParameters searchParameters) throws ParseException
    {
        // Read the AFTS query.
        AlfrescoFunctionEvaluationContext functionContext = new AlfrescoFunctionEvaluationContext(namespace, dictionaryService,
                searchParameters.getNamespace());

        Query alfrescoQuery = constructQuery(searchParameters, functionContext);
        QueryBuilderContext<org.opensearch.client.opensearch._types.query_dsl.Query, Sort, ParseException> elasticContext = new AFTSQueryBuilderContext(searchParameters,
                namespace, dictionaryService, exactTermSearchConfig, siteService, esTypeResolver);

        // Create the Elasticsearch query alfrescoQuery.
        return createQueryBuilder(functionContext, alfrescoQuery, elasticContext);
    }

    /**
     * Create the Alfresco internal query object.
     *
     * @param searchParameters
     *            The AFTS query.
     * @param functionContext
     *            An object containing helper functions for converting AFTS to the internal model.
     * @return The internal query object.
     */
    private Query constructQuery(SearchParameters searchParameters, AlfrescoFunctionEvaluationContext functionContext)
    {
        QueryModelFactory factory = new LuceneQueryModelFactory<>();
        FTSParser.Mode mode = DEFAULT_CONJUNCTION;
        Connective defaultFieldConnective = AND;
        if (searchParameters.getDefaultFTSOperator() == Operator.OR)
        {
            mode = DEFAULT_DISJUNCTION;
            defaultFieldConnective = OR;
        }
        // Using SINGLE_PASS avoid query rewrite that cause issues like phrase queries executed as boolean queries.
        Constraint constraint = FTSQueryParser.buildFTS(searchParameters.getQuery(), factory, functionContext, null, null, mode,
                defaultFieldConnective, searchParameters.getQueryTemplates(), searchParameters.getDefaultFieldName(), SINGLE_PASS);
        return factory.createQuery(null, null, constraint, new ArrayList<>());
    }

    /**
     * Create the Elasticsearch query builder object from the Alfresco internal model.
     *
     * @param functionContext
     *            The internal model for Alfresco queries.
     * @param builder
     *            The object responsible for converting the Alfresco internal model to the ES query builder.
     * @param elasticContext
     *            An object containing helper functions for converting the internal model to ES.
     * @return The Elasticsearch query builder.
     * @throws ParseException
     *             If there is a problem converting the internal object.
     */
    private org.opensearch.client.opensearch._types.query_dsl.Query createQueryBuilder(AlfrescoFunctionEvaluationContext functionContext, Query builder,
            QueryBuilderContext<org.opensearch.client.opensearch._types.query_dsl.Query, Sort, ParseException> elasticContext) throws ParseException
    {
        // LuceneQuery implements Query and LuceneQueryBuilder.
        @SuppressWarnings("unchecked")
        LuceneQueryBuilder<org.opensearch.client.opensearch._types.query_dsl.Query, Sort, ParseException> queryBuilder = (LuceneQueryBuilder<org.opensearch.client.opensearch._types.query_dsl.Query, Sort, ParseException>) builder;
        return queryBuilder.buildQuery(null, elasticContext, functionContext);
    }

}
