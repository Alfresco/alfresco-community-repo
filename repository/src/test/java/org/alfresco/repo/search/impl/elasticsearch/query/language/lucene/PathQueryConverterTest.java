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

package org.alfresco.repo.search.impl.elasticsearch.query.language.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.junit.Test;

public class PathQueryConverterTest
{

    private static final String MUST_BE_REGEXP_QUERY = "Converted query must be instance of RegexpQuery";
    private static final String DIFFERENT_FIELD_EXPECTED = "Different field: %s than expected: %s";
    private static final String DIFFERENT_TEXT_EXPECTED = "Different text: %s than expected: %s";

    @Test
    public void testExactPathConversion()
    {
        final String pathPhrase = "/n1:root/n1:a/n1:b";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/n1:root/n1:a/n1:b");
    }

    @Test
    public void testExactPathConversionNoInitialSlash()
    {
        final String pathPhrase = "n1:root/n1:a/n1:b";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/n1:root/n1:a/n1:b");
    }

    @Test
    public void testConversionProperlyEscapesWhitespaceCharacters()
    {
        final String pathPhrase = "/n1:root/n1:a_x0020_b";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/n1:root/n1:a b");
    }

    @Test
    public void testRootChildrenConversion()
    {
        final String pathPhrase = "/*";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/[^/]+");
    }

    @Test
    public void testRootChildrenConversionNoInitialSlash()
    {
        final String pathPhrase = "*";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/[^/]+");
    }

    @Test
    public void testGetRootDescendantsConversion()
    {
        final String pathPhrase = "//*";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/(.*/)?[^/]+");
    }

    @Test
    public void testGetRootAndDescendantsConversion()
    {
        final String pathPhrase = "//";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/(.*/)?");
    }

    @Test
    public void testGetRootOnly()
    {
        final String pathPhrase = "/";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/");
    }

    @Test
    public void testGetAllDescendantsConversionNoOtherWildcards()
    {
        final String pathPhrase = "/n1:root/n1:a//*";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/n1:root/n1:a/(.*/)?[^/]+");
    }

    @Test
    public void testGetAllChildrenConversionNoOtherWildcards()
    {
        final String pathPhrase = "/n1:root/n1:a/*";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/n1:root/n1:a/[^/]+");
    }

    @Test
    public void testGetAllDescendantsConversionMultipleWildcards()
    {
        final String pathPhrase = "/n1:root/n1:a/*/*/n1:d//*";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/n1:root/n1:a/[^/]+/[^/]+/n1:d/(.*/)?[^/]+");
    }

    @Test
    public void testGetAllDescendantsConversionMultipleWildcardsInTheMiddle()
    {
        final String pathPhrase = "/n1:root/n1:a/*/*/n1:d/*/*/n1:g//*";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/n1:root/n1:a/[^/]+/[^/]+/n1:d/[^/]+/[^/]+/n1:g/(.*/)?[^/]+");
    }

    @Test
    public void testGetAllChildrenConversionMultipleWildcards()
    {
        final String pathPhrase = "/n1:root/n1:a/*/*/n1:d/*";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/n1:root/n1:a/[^/]+/[^/]+/n1:d/[^/]+");
    }

    @Test
    public void testGetAllChildrenConversionMultipleWildcardsInTheMiddle()
    {
        final String pathPhrase = "/n1:root/n1:a/*/*/n1:d/*/n1:f/*";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/n1:root/n1:a/[^/]+/[^/]+/n1:d/[^/]+/n1:f/[^/]+");
    }

    @Test
    public void testGetAllChildrenConversionAnyAncestors()
    {
        final String pathPhrase = "//n1:d/*";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/(.*/)?n1:d/[^/]+");
    }

    @Test
    public void testGetChildConversionAnyAncestors()
    {
        final String pathPhrase = "//n1:d";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/(.*/)?n1:d");
    }

    @Test
    public void testGetChildConversionWildcardsAndAnyAncestors()
    {
        final String pathPhrase = "/n1:a/n1:b//*/n1:e//n1:g";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/n1:a/n1:b/(.*/)?[^/]+/n1:e/(.*/)?n1:g");
    }

    @Test
    public void testGetChildrenConversionWildcardsAndAnyAncestors()
    {
        final String pathPhrase = ".//n1:c/n1:d/*";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/(.*/)?n1:c/n1:d/[^/]+");
    }

    @Test
    public void testSearchAllDescendantsConversionWildcardsAndAnyAncestors()
    {
        final String pathPhrase = ".//n1:c/n1:d//*";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/(.*/)?n1:c/n1:d/(.*/)?[^/]+");
    }

    @Test
    public void testGetExactNodeConversionAnyAncestorsOnly()
    {
        final String pathPhrase = "//n1:a//n1:b";
        final Query convertedQuery = PathQueryConverter.toLucenePathQuery(pathPhrase);
        assertRegexpQuery(convertedQuery, "/(.*/)?n1:a/(.*/)?n1:b");
    }

    @Test
    public void testConversionWithSpecialCharactersInPath()
    {
        final String pathPhraseNoWildcards = "/n1:[root]/n1:{a}/n1:(b)/n1:&c@/n1:^d+/n1:~e?";
        final Query convertedQueryNoWildcards = PathQueryConverter.toLucenePathQuery(pathPhraseNoWildcards);
        assertRegexpQuery(convertedQueryNoWildcards, "/n1:\\[root\\]/n1:\\{a\\}/n1:\\(b\\)/n1:\\&c\\@/n1:\\^d\\+/n1:\\~e\\?");
        final String pathPhraseWithWildcards = "/n1:[root]//*/n1:&c@/n1:^d+/n1:~e?/*";
        final Query convertedQueryWithWildcards = PathQueryConverter.toLucenePathQuery(pathPhraseWithWildcards);
        assertRegexpQuery(convertedQueryWithWildcards, "/n1:\\[root\\]/(.*/)?[^/]+/n1:\\&c\\@/n1:\\^d\\+/n1:\\~e\\?/[^/]+");
    }

    private static void assertRegexpQuery(final Query convertedQuery, final String expectedText)
    {
        assertTrue(MUST_BE_REGEXP_QUERY, convertedQuery instanceof RegexpQuery);
        final RegexpQuery regexpQuery = (RegexpQuery) convertedQuery;
        assertEquals(DIFFERENT_FIELD_EXPECTED.formatted(regexpQuery.getField(), PathQueryConverter.PATH_KEYWORD), PathQueryConverter.PATH_KEYWORD,
                regexpQuery.getField());
        assertEquals(DIFFERENT_TEXT_EXPECTED.formatted(regexpQuery.getRegexp().text(), expectedText), expectedText, regexpQuery.getRegexp().text());
    }
}
