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

import org.alfresco.repo.search.impl.lucene.QueryParser;


/**
 * Helper class to provide conversions between different search languages
 * @author Derek Hulley
 */
public class SearchLanguageConversion
{
    /**
     * XPath like query language summary:
     * <ul>
     *   <li>Escape: \</li>
     *   <li>Single char search: _</li>
     *   <li>Multiple char search: %</li>
     *   <li>Reserved: \%_</li>
     * </ul>
     */
    public static LanguageDefinition DEF_XPATH_LIKE = new SimpleLanguageDef('\\', "%", "_", "\\%_");
    /**
     * Regular expression query language summary:
     * <ul>
     *   <li>Escape: \</li>
     *   <li>Single char search: .</li>
     *   <li>Multiple char search: .*</li>
     *   <li>Reserved: \*.+?^$(){}|</li>
     * </ul>
     */
    public static LanguageDefinition DEF_REGEX = new SimpleLanguageDef('\\', ".*", ".", "\\*.+?^$(){}|");
    /**
     * Lucene syntax summary: {@link QueryParser#escape(String) Lucene Query Parser}
     */
    public static LanguageDefinition DEF_LUCENE = new LuceneLanguageDef();

    /**
     * Escape a string according to the <b>XPath</b> like function syntax.
     * 
     * @param str the string to escape
     * @return Returns the escaped string
     */
    public static String escapeForXPathLike(String str)
    {
        return escape(DEF_XPATH_LIKE, str);
    }
    
    /**
     * Escape a string according to the <b>regex</b> language syntax.
     * 
     * @param str the string to escape
     * @return Returns the escaped string
     */
    public static String escapeForRegex(String str)
    {
        return escape(DEF_REGEX, str);
    }
    
    /**
     * Escape a string according to the <b>Lucene</b> query syntax.
     * 
     * @param str the string to escape
     * @return Returns the escaped string
     */
    public static String escapeForLucene(String str)
    {
        return escape(DEF_LUCENE, str);
    }
    
    /**
     * Generic escaping using the language definition
     */
    private static String escape(LanguageDefinition def, String str)
    {
        StringBuilder sb = new StringBuilder(str.length() * 2);
        
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            // first check for reserved chars
            if (def.isReserved(chars[i]))
            {
                // escape it
                sb.append(def.escapeChar);
            }
            sb.append(chars[i]);
        }
        return sb.toString();
    }
    
    /**
     * Convert an <b>xpath</b> like function clause into a <b>regex</b> query.
     * 
     * @param xpathLikeClause
     * @return Returns a valid regular expression that is equivalent to the
     *      given <b>xpath</b> like clause.
     */
    public static String convertXPathLikeToRegex(String xpathLikeClause)
    {
        return convert(DEF_XPATH_LIKE, DEF_REGEX, xpathLikeClause);
    }
    
    /**
     * Convert an <b>xpath</b> like function clause into a <b>Lucene</b> query.
     * 
     * @param xpathLikeClause
     * @return Returns a valid <b>Lucene</b> expression that is equivalent to the
     *      given <b>xpath</b> like clause.
     */
    public static String convertXPathLikeToLucene(String xpathLikeClause)
    {
        return convert(DEF_XPATH_LIKE, DEF_LUCENE, xpathLikeClause);
    }
    
    public static String convert(LanguageDefinition from, LanguageDefinition to, String query)
    {
        char[] chars = query.toCharArray();
        
        StringBuilder sb = new StringBuilder(chars.length * 2);
        
        boolean escaping = false;
        
        for (int i = 0; i < chars.length; i++)
        {
            if (escaping)   // if we are currently escaping, just escape the current character
            {
                sb.append(to.escapeChar);           // the to format escape char
                sb.append(chars[i]);                // the current char
                escaping = false;
            }
            else if (chars[i] == from.escapeChar)    // not escaping and have escape char
            {
                escaping = true;
            }
            else if (query.startsWith(from.multiCharWildcard, i))  // not escaping but have multi-char wildcard
            {
                // translate the wildcard
                sb.append(to.multiCharWildcard);
            }
            else if (query.startsWith(from.singleCharWildcard, i))  // have single-char wildcard
            {
                // translate the wildcard
                sb.append(to.singleCharWildcard);
            }
            else if (to.isReserved(chars[i]))    // reserved character
            {
                sb.append(to.escapeChar).append(chars[i]);
            }
            else                                // just a normal char in both
            {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }
    
    /**
     * Simple store of special characters for a given query language
     */
    public static abstract class LanguageDefinition
    {
        public final char escapeChar;
        public final String multiCharWildcard;
        public final String singleCharWildcard;
        
        public LanguageDefinition(char escapeChar, String multiCharWildcard, String singleCharWildcard)
        {
            this.escapeChar = escapeChar;
            this.multiCharWildcard = multiCharWildcard;
            this.singleCharWildcard = singleCharWildcard;
        }
        public abstract boolean isReserved(char ch);
    }
    private static class SimpleLanguageDef extends LanguageDefinition
    {
        private String reserved;
        public SimpleLanguageDef(char escapeChar, String multiCharWildcard, String singleCharWildcard, String reserved)
        {
            super(escapeChar, multiCharWildcard, singleCharWildcard);
            this.reserved = reserved; 
        }
        @Override
        public boolean isReserved(char ch)
        {
            return (reserved.indexOf(ch) > -1);
        }
    }
    private static class LuceneLanguageDef extends LanguageDefinition
    {
        private String reserved;
        public LuceneLanguageDef()
        {
            super('\\', "*", "?");
            init();
        }
        /**
         * Discovers all the reserved chars
         */
        private void init()
        {
            StringBuilder sb = new StringBuilder(20);
            for (char ch = 0; ch < 256; ch++)
            {
                char[] chars = new char[] {ch};
                String unescaped = new String(chars);
                // check it
                String escaped = QueryParser.escape(unescaped);
                if (!escaped.equals(unescaped))
                {
                    // it was escaped
                    sb.append(ch);
                }
            }
            reserved = sb.toString();
        }
        @Override
        public boolean isReserved(char ch)
        {
            return (reserved.indexOf(ch) > -1);
        }
    }
}
