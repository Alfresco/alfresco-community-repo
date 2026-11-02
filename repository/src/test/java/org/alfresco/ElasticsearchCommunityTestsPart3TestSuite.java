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

/**
 * Elasticsearch community integration tests, part 3 of 3: Lucene query language ITs.
 * <p>
 * The unit tests of this area run in {@link AllUnitTestsSuite}. {@link ElasticsearchCommunityTestsTestSuite} runs all three parts.
 */
@RunWith(Categories.class)
@Suite.SuiteClasses({
        // language = lucene ITs
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.TermQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.WildcardQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.PhraseQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.RangeQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.BooleanQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.ContentAndContentMetadataIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.EscapeCharacterIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.FieldQueryIT.class,
        org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.PermissionQueryIT.class
})

public class ElasticsearchCommunityTestsPart3TestSuite
{}
