/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.search.impl.querymodel.impl.lucene;

import org.alfresco.repo.search.impl.lucene.LuceneAnalyser;
import org.alfresco.repo.search.impl.lucene.LuceneConfig;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.apache.lucene.index.IndexReader;

/**
 * @author andyh
 */
public class LuceneQueryBuilderContext
{
    private LuceneQueryParser lqp;

    private NamespacePrefixResolver namespacePrefixResolver;

    /**
     * Context for building lucene queries
     * 
     * @param dictionaryService
     * @param namespacePrefixResolver
     * @param tenantService
     * @param searchParameters
     * @param config
     * @param indexReader
     */
    public LuceneQueryBuilderContext(DictionaryService dictionaryService, NamespacePrefixResolver namespacePrefixResolver, TenantService tenantService,
            SearchParameters searchParameters, LuceneConfig config, IndexReader indexReader)
    {
        LuceneAnalyser analyzer = new LuceneAnalyser(dictionaryService, searchParameters.getMlAnalaysisMode() == null ? config.getDefaultMLSearchAnalysisMode() : searchParameters
                .getMlAnalaysisMode());
        lqp = new LuceneQueryParser(searchParameters.getDefaultFieldName(), analyzer);
        lqp.setDefaultOperator(LuceneQueryParser.OR_OPERATOR);
        lqp.setDictionaryService(dictionaryService);
        lqp.setNamespacePrefixResolver(namespacePrefixResolver);
        lqp.setTenantService(tenantService);
        lqp.setSearchParameters(searchParameters);
        lqp.setLuceneConfig(config);
        lqp.setIndexReader(indexReader);
        lqp.setAllowLeadingWildcard(true);
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    /**
     * @return - the parser
     */
    public LuceneQueryParser getLuceneQueryParser()
    {
        return lqp;
    }

    /**
     * @return - the namespace prefix resolver
     */
    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return namespacePrefixResolver;
    }

}
