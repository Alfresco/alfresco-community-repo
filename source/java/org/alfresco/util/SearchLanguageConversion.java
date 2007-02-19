/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
    public static LanguageDefinition DEF_XPATH_LIKE = new SimpleLanguageDef('\\', "%", "_", "\\%_[]");
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
     * CIFS name patch query language summary:
     * <ul>
     *   <li>Escape: \  (but not used)</li>
     *   <li>Single char search: ?</li>
     *   <li>Multiple char search: *</li>
     *   <li>Reserved: "*\<>?/:|¬£%&+;</li>
     * </ul>
     */
    public static LanguageDefinition DEF_CIFS = new SimpleLanguageDef('\\', "*", "?", "\"*\\<>?/:|¬£%&+;");

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
        return "(?s)" + convert(DEF_XPATH_LIKE, DEF_REGEX, xpathLikeClause);
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
    
    /**
     * Convert a <b>CIFS</b> name path into the equivalent <b>Lucene</b> query.
     * 
     * @param cifsNamePath the CIFS named path
     * @return Returns a valid <b>Lucene</b> expression that is equivalent to the
     *      given CIFS name path
     */
    public static String convertCifsToLucene(String cifsNamePath)
    {
        return convert(DEF_CIFS, DEF_LUCENE, cifsNamePath);
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
