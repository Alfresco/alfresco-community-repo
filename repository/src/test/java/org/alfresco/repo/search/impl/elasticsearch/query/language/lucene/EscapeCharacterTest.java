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

import org.junit.Before;
import org.junit.Test;

import org.alfresco.service.cmr.search.SearchParameters;

public class EscapeCharacterTest
{
    private LuceneQueryParser parser;

    @Before
    public void setUp()
    {
        parser = new LuceneQueryParserUnderTest();
    }

    @Test
    public void escapeSpecialCharactersInParserTest()
    {
        assertEquals("\\\\", parser.escapeSpecialCharacters("\\"));
        assertEquals("\\\\x", parser.escapeSpecialCharacters("\\x"));
        assertEquals("\\?", parser.escapeSpecialCharacters("\\?"));
        assertEquals("\\*", parser.escapeSpecialCharacters("\\*"));
        assertEquals("\\(", parser.escapeSpecialCharacters("("));
        assertEquals("\\\\(", parser.escapeSpecialCharacters("\\("));
    }

    /**
     * sanitizeTerms() receives a query text which escape() has already escaped as a whole, so it only has to take care of what is special at the beginning of a term.
     */
    @Test
    public void sanitizeTermsEscapesEveryTermPrefixTest()
    {
        // "-" and "+" are escaped in the terms which escape() left untouched
        assertEquals("foo \\-bar", parser.sanitizeTerms("foo -bar"));
        assertEquals("foo \\+bar", parser.sanitizeTerms("foo +bar"));
        assertEquals("a* \\-b \\+c", parser.sanitizeTerms("a* -b +c"));

        // reserved words are escaped wherever they appear
        assertEquals("foo \\AND bar*", parser.sanitizeTerms("foo AND bar*"));
        assertEquals("\\NOT \\OR", parser.sanitizeTerms("NOT OR"));

        // a single term, or a term where the character isn't leading, is left alone
        assertEquals("single*", parser.sanitizeTerms("single*"));
        assertEquals("foo a-b c+d", parser.sanitizeTerms("foo a-b c+d"));
        assertEquals("foo ANDROID", parser.sanitizeTerms("foo ANDROID"));
    }

    /**
     * A term which is only a wildcard contributes nothing to the split query, as Elasticsearch reads "field:*" as an existence check.
     */
    @Test
    public void sanitizeTermsDropsWildcardOnlyTermsTest()
    {
        assertEquals("apple banana", parser.sanitizeTerms("apple banana *"));
        assertEquals("apple banana", parser.sanitizeTerms("apple * banana"));
        assertEquals("apple banana", parser.sanitizeTerms("* apple * banana *"));

        // a wildcard which is part of a term, or an escaped one, is not a wildcard-only term
        assertEquals("apple ban*", parser.sanitizeTerms("apple ban*"));
        assertEquals("apple \\*", parser.sanitizeTerms("apple \\*"));

        // "?" and "%" are not dropped: "field:?" is a real single character wildcard and "%" reaches Elasticsearch as a literal
        assertEquals("apple ? %", parser.sanitizeTerms("apple ? %"));

        // dropping must never leave an empty query text
        assertEquals("*", parser.sanitizeTerms("*"));
        assertEquals("*", parser.sanitizeTerms("* *"));
    }

    /**
     * The special characters escape() escapes in any position have already been escaped in every term, so sanitizeTerms() must not escape them again.
     */
    @Test
    public void sanitizeTermsDoesNotDoubleEscapeTest()
    {
        assertEquals("foo \\(bar\\) \\-baz*", parser.sanitizeTerms(parser.escape("foo (bar) -baz*", true)));
        assertEquals("a\\:b \\-c\\(d\\)* e", parser.sanitizeTerms(parser.escape("a:b -c(d)* e", true)));
        assertEquals("path\\/to \\-file\\[1\\]*", parser.sanitizeTerms(parser.escape("path/to -file[1]*", true)));

        // the leading "-" of the whole query text has been escaped by escape() already
        assertEquals("\\-lead* trail\\~", parser.sanitizeTerms(parser.escape("-lead* trail~", true)));
    }

    @SuppressWarnings("PMD.TestClassWithoutTestCases")
    protected class LuceneQueryParserUnderTest extends LuceneQueryParser
    {
        public LuceneQueryParserUnderTest()
        {
            super(null, null, null, new SearchParameters());
        }
    }
}
