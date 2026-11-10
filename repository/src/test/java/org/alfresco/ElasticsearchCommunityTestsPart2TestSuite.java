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

import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.ExactTermQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.IndexLocaleAnalyzerIT;

/**
 * Elasticsearch community integration tests, part 2 of 3: AFTS and CMIS query language ITs.
 * <p>
 * The unit tests of this area run in {@link AllUnitTestsSuite}. {@link ElasticsearchCommunityTestsTestSuite} runs all three parts.
 */
@RunWith(Categories.class)
@Suite.SuiteClasses({
        // language = afts ITs
        ExactTermQueryIT.class,
        IndexLocaleAnalyzerIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.TermQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.PhraseQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.WildcardQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.RangeQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.DateMathQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.BooleanQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.ContentAndContentMetadataIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.PermissionQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.afts.EscapeCharacterIT.class,

        // language = cmis ITs
        org.alfresco.repo.search.impl.elasticsearch.query.language.cmis.EscapeCharacterIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.cmis.TermQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.cmis.PhraseQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.cmis.WildcardQueryIT.class
})

public class ElasticsearchCommunityTestsPart2TestSuite
{}
