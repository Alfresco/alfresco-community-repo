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
package org.alfresco.repo.search.impl.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_INDEX_FTS_ALFRESCO;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_LUCENE;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.alfresco.repo.search.impl.elasticsearch.query.ElasticsearchQueryExecutor;
import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.AFTSQueryBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.LuceneQueryBuilder;
import org.alfresco.util.ApplicationContextHelper;

public class ElasticsearchSearchServiceFactoryTest
{

    private static ClassPathXmlApplicationContext searchContext;

    @BeforeClass
    public static void init()
    {
        searchContext = new ClassPathXmlApplicationContext(
                new String[]{"alfresco/search/test-elasticsearch-community-context.xml"},
                ApplicationContextHelper.getApplicationContext());
    }

    @Test
    public void shouldFactoryContainsAftsAlfrescoLanguage()
    {
        ElasticsearchSearchServiceFactory elasticsearchSearchServiceFactory = searchContext.getBean(
                ElasticsearchSearchServiceFactory.class);
        ElasticsearchQueryExecutor afts = (ElasticsearchQueryExecutor) elasticsearchSearchServiceFactory.getQueryLanguages().get(LANGUAGE_INDEX_FTS_ALFRESCO);
        assertNotNull(afts);
        assertEquals(AFTSQueryBuilder.class, afts.getLanguageQueryBuilder().getClass());
    }

    @Test
    public void shouldFactoryContainsLuceneLanguage()
    {
        ElasticsearchSearchServiceFactory elasticsearchSearchServiceFactory = searchContext.getBean(
                ElasticsearchSearchServiceFactory.class);
        ElasticsearchQueryExecutor lucene = (ElasticsearchQueryExecutor) elasticsearchSearchServiceFactory.getQueryLanguages().get(LANGUAGE_LUCENE);
        assertNotNull(lucene);
        assertEquals(LuceneQueryBuilder.class, lucene.getLanguageQueryBuilder().getClass());
    }

}
