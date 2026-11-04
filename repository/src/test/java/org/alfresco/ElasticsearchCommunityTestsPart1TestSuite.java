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

package org.alfresco;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchSearchServiceFactoryTest;
import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchTagSupportIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ContentModelSynchronizerTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchIndexServiceIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchInitialiserIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.IndexConfigurationInitializerIntegrationTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.IndexingIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfigIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchFieldAnalyzersConfigIT;
import org.alfresco.repo.search.impl.elasticsearch.query.AspectQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.ClassQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.FilterAggregationsIT;
import org.alfresco.repo.search.impl.elasticsearch.query.FilterQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.IgnoreUnsupportedPropertyIT;
import org.alfresco.repo.search.impl.elasticsearch.query.MultiValueFieldQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.PathQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.QueryByIdIT;
import org.alfresco.repo.search.impl.elasticsearch.query.ResultSetIT;
import org.alfresco.repo.search.impl.elasticsearch.query.SiteQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.SortIT;
import org.alfresco.repo.search.impl.elasticsearch.query.TagQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.TermsAggregationsIT;
import org.alfresco.repo.search.impl.elasticsearch.query.TypeQueryIT;

/**
 * Elasticsearch community integration tests, part 1 of 3: index set-up, content model sync, search services and the query ITs.
 * <p>
 * The unit tests of this area run in {@link AllUnitTestsSuite}. {@link ElasticsearchCommunityTestsTestSuite} runs all three parts.
 */
@RunWith(Categories.class)
@Suite.SuiteClasses({
        // Content model sync
        ElasticsearchInitialiserIT.class,
        ElasticsearchIndexServiceIT.class,
        ContentModelSynchronizerTest.class,
        IndexConfigurationInitializerIntegrationTest.class,
        IndexingIT.class,
        ElasticsearchExactTermSearchConfigIT.class,
        ElasticsearchFieldAnalyzersConfigIT.class,

        // Search service
        ElasticsearchSearchServiceFactoryTest.class,
        ElasticsearchTagSupportIT.class,

        // Document Service
        org.alfresco.repo.search.impl.elasticsearch.admin.ElasticsearchDocumentsServiceIT.class,

        // Query ITs
        FilterQueryIT.class,
        QueryByIdIT.class,
        FilterAggregationsIT.class,
        TermsAggregationsIT.class,
        ResultSetIT.class,
        SiteQueryIT.class,
        TagQueryIT.class,
        TypeQueryIT.class,
        MultiValueFieldQueryIT.class,
        PathQueryIT.class,
        AspectQueryIT.class,
        ClassQueryIT.class,
        IgnoreUnsupportedPropertyIT.class,
        SortIT.class
})

public class ElasticsearchCommunityTestsPart1TestSuite
{}
