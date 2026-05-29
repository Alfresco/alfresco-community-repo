/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

    @SuppressWarnings("PMD.TestClassWithoutTestCases")
    protected class LuceneQueryParserUnderTest extends LuceneQueryParser
    {
        public LuceneQueryParserUnderTest()
        {
            super(null, null, null, new SearchParameters());
        }
    }
}
