/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
                ") \\_ { } [ ] " +
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
