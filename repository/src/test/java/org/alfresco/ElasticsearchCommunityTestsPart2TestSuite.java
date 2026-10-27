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

import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ElasticsearchInitialiserIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.IndexingIT;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchFieldAnalyzersConfigIT;
import org.alfresco.repo.search.impl.elasticsearch.query.ClassQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.FilterQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.PathQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.ResultSetIT;
import org.alfresco.repo.search.impl.elasticsearch.query.TermsAggregationsIT;
import org.alfresco.repo.search.impl.elasticsearch.query.TypeQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.ExactTermQueryIT;

/**
 * Elasticsearch community tests, part 2 of 3.
 * <p>
 * The suite is split only to shorten CI: each part runs as its own job. {@link ElasticsearchCommunityTestsTestSuite} still runs all parts.
 */
@RunWith(Categories.class)
@Suite.SuiteClasses({
        // Content model sync
        ElasticsearchInitialiserIT.class,
        IndexingIT.class,
        ElasticsearchFieldAnalyzersConfigIT.class,

        // Query ITs
        FilterQueryIT.class,
        TermsAggregationsIT.class,
        ResultSetIT.class,
        TypeQueryIT.class,
        PathQueryIT.class,
        ClassQueryIT.class,

        // language = afts ITs
        ExactTermQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.PhraseQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.DateMathQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.ContentAndContentMetadataIT.class,

        // language = lucene ITs
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.PhraseQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.BooleanQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.FieldQueryIT.class,

        // language = cmis ITs
        org.alfresco.repo.search.impl.elasticsearch.query.language.cmis.WildcardQueryIT.class
})

public class ElasticsearchCommunityTestsPart2TestSuite
{}
