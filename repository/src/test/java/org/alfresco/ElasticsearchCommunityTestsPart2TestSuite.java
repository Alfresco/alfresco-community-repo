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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Community Elasticsearch tests, part 2 of 3 (190 tests in 17 classes).
 * <p>
 * The Elasticsearch tests are split across three suites purely to keep CI wall-clock time down: each part runs as its own job, against its own Elasticsearch and PostgreSQL containers. A single combined job took around 30 minutes.
 * <p>
 * Parts are balanced by the test count JUnit actually reports, which is not the same as counting {@code @Test} annotations: several classes here are parameterised, and the {@link org.alfresco.repo.search.impl.elasticsearch.query.LuceneOrAFTSQueryIT} subclasses run each of their tests twice, once per query language. When moving a class between parts, rebalance on the reported count rather than the annotation count.
 * <p>
 * Only tests needing a Spring application context and a live Elasticsearch belong here; the plain unit tests from these same packages run in {@link AllUnitTestsSuite} with no container. {@link ElasticsearchCommunityTestsTestSuite} aggregates all three parts for local runs.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchInitialiserIT.class,
        org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.IndexingIT.class,
        org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchFieldAnalyzersConfigIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.ClassQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.FilterQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.PathQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.ResultSetIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.TermsAggregationsIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.TypeQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.ContentAndContentMetadataIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.DateMathQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.ExactTermQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.PhraseQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.cmis.WildcardQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.BooleanQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.FieldQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.PhraseQueryIT.class
})
public class ElasticsearchCommunityTestsPart2TestSuite
{}
