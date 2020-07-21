/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.util;

import junit.framework.TestCase;

/**
 * @see org.alfresco.util.SearchLanguageConversion
 * 
 * @author Derek Hulley
 */
public class SearchLanguageConversionTest extends TestCase
{
    /**
     * A string with a whole lod of badness to stress test with
     */
    private static final String BAD_STRING =
            "\\ | ! \" £ " +
            "$ % ^ & * ( " +
            ") _ { } [ ] " +
            "@ # ~ ' : ; " +
            ", . < > + ? " +
            "/ \\\\ \\* \\? \\_";
    
    public void testEscapeXPathLike()
    {
        String good = SearchLanguageConversion.escapeForXPathLike(BAD_STRING);
        assertEquals("Escaping for xpath failed",
                "\\\\ | ! \" £ " +
                "$ \\% ^ & * ( " +
                ") \\_ { } \\[ \\] " +
                "@ # ~ ' : ; " +
                ", . < > + ? " +
                "/ \\\\\\\\ \\\\* \\\\? \\\\\\_",
                good);
    }
    
    public void testEscapeRegex()
    {
        String good = SearchLanguageConversion.escapeForRegex(BAD_STRING);
        assertEquals("Escaping for regex failed",
                "\\\\ \\| ! \" £ " +
                "\\$ % \\^ & \\* \\( " +
                "\\) _ \\{ \\} \\[ \\] " +
                "@ # ~ ' : ; " +
                ", \\. < > \\+ \\? " +
                "/ \\\\\\\\ \\\\\\* \\\\\\? \\\\_",
                good);
    }
    
    public void testEscapeLucene()
    {
        String good = SearchLanguageConversion.escapeForLucene(BAD_STRING);
        assertEquals("Escaping for Lucene failed",
                "\\\\ \\| \\! \\\" £ " +
                "$ % \\^ \\& \\* \\( " +
                "\\) _ \\{ \\} \\[ \\] " +
                "@ # \\~ ' \\: ; " +
                ", . < > \\+ \\? " +
                "\\/ \\\\\\\\ \\\\\\* \\\\\\? \\\\_",
                good);
    }
    
    public void testConvertXPathLikeToRegex()
    {
        String good = SearchLanguageConversion.convertXPathLikeToRegex(BAD_STRING);
        assertEquals("XPath like to regex failed",
                "(?s) \\| ! \" £ " +
                "\\$ .* \\^ & \\* \\( " +
                "\\) . \\{ \\} \\[ \\] " +
                "@ # ~ ' : ; " +
                ", \\. < > \\+ \\? " +
                "/ \\\\ \\* \\? _",
                good);
    }
    
    public void testConvertXPathLikeToLucene()
    {
        String good = SearchLanguageConversion.convertXPathLikeToLucene(BAD_STRING);
        assertEquals("XPath like to Lucene failed",
                " \\| \\! \\\" £ " +
                "$ * \\^ \\& \\* \\( " +
                "\\) ? \\{ \\} \\[ \\] " +
                "@ # \\~ ' \\: ; " +
                ", . < > \\+ \\? " +
                "\\/ \\\\ \\* \\? _",
                good);
    }
    
    public void testSqlToLucene()
    {
        String sqlLike = "AB%_*?\\%\\_";
        String lucene = "AB*?\\*\\?%_";
        String converted = SearchLanguageConversion.convert(SearchLanguageConversion.DEF_SQL_LIKE, SearchLanguageConversion.DEF_LUCENE, sqlLike);
        assertEquals(lucene, converted);
    }
    
    public void testLuceneToRegexp()
    {
        String lucene = "AB*?\\*\\?.*.";
        String regexp = "AB.*.\\*\\?\\..*\\.";
        String converted = SearchLanguageConversion.convert(SearchLanguageConversion.DEF_LUCENE, SearchLanguageConversion.DEF_REGEX, lucene);
        assertEquals(regexp, converted);
    }
    
    public void testLuceneToSql()
    {
        String lucene = "%_";
        String sql = "\\%\\_";
        String converted = SearchLanguageConversion.convert(SearchLanguageConversion.DEF_LUCENE, SearchLanguageConversion.DEF_SQL_LIKE, lucene);
        assertEquals(sql, converted);
    }
    
    public void testTokenizeString()
    {
        String[] res = SearchLanguageConversion.tokenizeString("");
        assertTrue(res.length == 1);
        res = SearchLanguageConversion.tokenizeString("bob");
        assertTrue(res.length == 1);
        assertEquals("bob", res[0]);
        res = SearchLanguageConversion.tokenizeString("   bob   ");
        assertTrue(res.length == 1);
        assertEquals("bob", res[0]);
        res = SearchLanguageConversion.tokenizeString("   bob hope ");
        assertTrue(res.length == 2);
        assertEquals("bob", res[0]);
        assertEquals("hope", res[1]);
        res = SearchLanguageConversion.tokenizeString("   bob    hope ");
        assertTrue(res.length == 2);
        assertEquals("bob", res[0]);
        assertEquals("hope", res[1]);
        res = SearchLanguageConversion.tokenizeString("    bob  no    hope  ");
        assertTrue(res.length == 3);
        assertEquals("bob", res[0]);
        assertEquals("no", res[1]);
        assertEquals("hope", res[2]);
    }
}
