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
 * Community Elasticsearch tests, part 3 of 3 (189 tests in 16 classes).
 * <p>
 * The Elasticsearch tests are split across three suites purely to keep CI wall-clock time down: each part runs as its own job, against its own Elasticsearch and PostgreSQL containers. A single combined job took around 30 minutes.
 * <p>
 * Parts are balanced by the test count JUnit actually reports, which is not the same as counting {@code @Test} annotations: several classes here are parameterised, and the {@link org.alfresco.repo.search.impl.elasticsearch.query.LuceneOrAFTSQueryIT} subclasses run each of their tests twice, once per query language. When moving a class between parts, rebalance on the reported count rather than the annotation count.
 * <p>
 * Only tests needing a Spring application context and a live Elasticsearch belong here; the plain unit tests from these same packages run in {@link AllUnitTestsSuite} with no container. {@link ElasticsearchCommunityTestsTestSuite} aggregates all three parts for local runs.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        org.alfresco.repo.search.impl.elasticsearch.ElasticsearchSearchServiceFactoryTest.class,
        org.alfresco.repo.search.impl.elasticsearch.ElasticsearchTagSupportIT.class,
        org.alfresco.repo.search.impl.elasticsearch.admin.ElasticsearchDocumentsServiceIT.class,
        org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchIndexServiceIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.AspectQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.IgnoreUnsupportedPropertyIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.SortIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.BooleanQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.EscapeCharacterIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.IndexLocaleAnalyzerIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.RangeQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.WildcardQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.cmis.TermQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.RangeQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.TermQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.WildcardQueryIT.class
})
public class ElasticsearchCommunityTestsPart3TestSuite
{}
