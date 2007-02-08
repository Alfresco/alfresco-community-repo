/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
                "\\) _ \\{ \\} [ ] " +
                "@ # ~ ' : ; " +
                ", \\. < > \\+ \\? " +
                "/ \\\\\\\\ \\\\\\* \\\\\\? \\\\_",
                good);
    }
    
    public void testEscapeLucene()
    {
        String good = SearchLanguageConversion.escapeForLucene(BAD_STRING);
        assertEquals("Escaping for regex failed",
                "\\\\ | \\! \\\" £ " +
                "$ % \\^ & \\* \\( " +
                "\\) _ \\{ \\} \\[ \\] " +
                "@ # \\~ ' \\: ; " +
                ", . < > \\+ \\? " +
                "/ \\\\\\\\ \\\\\\* \\\\\\? \\\\_",
                good);
    }
    
    public void testConvertXPathLikeToRegex()
    {
        String good = SearchLanguageConversion.convertXPathLikeToRegex(BAD_STRING);
        assertEquals("XPath like to regex failed",
                "(?s)\\ \\| ! \" £ " +
                "\\$ .* \\^ & \\* \\( " +
                "\\) . \\{ \\} [ ] " +
                "@ # ~ ' : ; " +
                ", \\. < > \\+ \\? " +
                "/ \\\\ \\* \\? \\_",
                good);
    }
    
    public void testConvertXPathLikeToLucene()
    {
        String good = SearchLanguageConversion.convertXPathLikeToLucene(BAD_STRING);
        assertEquals("XPath like to regex failed",
                "\\ | \\! \\\" £ " +
                "$ * \\^ & \\* \\( " +
                "\\) ? \\{ \\} \\[ \\] " +
                "@ # \\~ ' \\: ; " +
                ", . < > \\+ \\? " +
                "/ \\\\ \\* \\? \\_",
                good);
    }
}
