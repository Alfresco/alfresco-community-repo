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

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Sort;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import org.alfresco.repo.search.adaptor.QueryParserAdaptor;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfig;
import org.alfresco.repo.search.impl.elasticsearch.query.language.EsTypeResolver;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.QueryBuilderContext;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespacePrefixResolver;

public class AFTSForCMISQueryBuilderContext implements QueryBuilderContext<Query, Sort, ParseException>
{
    private final SearchParameters searchParameters;
    private final NamespacePrefixResolver namespacePrefixResolver;
    private final DictionaryService dictionaryService;
    private final ElasticsearchExactTermSearchConfig exactTermSearchConfig;
    private final SiteService siteService;
    private final EsTypeResolver esTypeResolver;

    public AFTSForCMISQueryBuilderContext(SearchParameters searchParameters,
            NamespacePrefixResolver namespacePrefixResolver,
            DictionaryService dictionaryService,
            ElasticsearchExactTermSearchConfig exactTermSearchConfig,
            SiteService siteService,
            EsTypeResolver esTypeResolver)
    {
        this.searchParameters = searchParameters;
        this.namespacePrefixResolver = namespacePrefixResolver;
        this.dictionaryService = dictionaryService;
        this.exactTermSearchConfig = exactTermSearchConfig;
        this.siteService = siteService;
        this.esTypeResolver = esTypeResolver;
    }

    @Override
    public QueryParserAdaptor<Query, Sort, ParseException> getLuceneQueryParserAdaptor()
    {
        return new AFTSForCMISQueryParserAdaptor(searchParameters, namespacePrefixResolver, dictionaryService, siteService, exactTermSearchConfig, esTypeResolver);
    }

    @Override
    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return namespacePrefixResolver;
    }
}
