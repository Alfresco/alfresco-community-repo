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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.StringUtils;


/**
 * Helper class to provide conversions between different search languages
 * 
 * @author Derek Hulley
 */
public class SearchLanguageConversion
{
    /**
     * SQL like query language summary:
     * <ul>
     * <li>Escape: \</li>
     * <li>Single char search: _</li>
     * <li>Multiple char search: %</li>
     * <li>Reserved: \%_</li>
     * </ul>
     */
    public static LanguageDefinition DEF_SQL_LIKE = new SimpleLanguageDef('\\', "%", "_", "\\%_[]");

    /**
     * XPath like query language summary:
     * <ul>
     * <li>Escape: \</li>
     * <li>Single char search: _</li>
     * <li>Multiple char search: %</li>
     * <li>Reserved: \%_</li>
     * </ul>
     */
    public static LanguageDefinition DEF_XPATH_LIKE = new SimpleLanguageDef('\\', "%", "_", "\\%_[]");

    /**
     * Regular expression query language summary:
     * <ul>
     * <li>Escape: \</li>
     * <li>Single char search: .</li>
     * <li>Multiple char search: .*</li>
     * <li>Reserved: \*.+?^$(){}[]|</li>
     * </ul>
     */
    public static LanguageDefinition DEF_REGEX = new SimpleLanguageDef('\\', ".*", ".", "\\*.+?^$(){}[]|");

    /**
     * Lucene syntax summary: Lucene Query Parser
     */
    public static LanguageDefinition DEF_LUCENE = new LuceneLanguageDef(true);

    public static LanguageDefinition DEF_LUCENE_INTERNAL = new LuceneLanguageDef(false);

    /**
     * CIFS name patch query language summary:
     * <ul>
     * <li>Escape: \ (but not used)</li>
     * <li>Single char search: ?</li>
     * <li>Multiple char search: *</li>
     * <li>Reserved: "*\<>?/:|¬£%&+;</li>
     * </ul>
     */
    public static LanguageDefinition DEF_CIFS = new SimpleLanguageDef('\\', "*", "?", "\"*\\<>?/:|¬£%&+;");

    /**
     * Escape a string according to the <b>XPath</b> like function syntax.
     * 
     * @param str
     *            the string to escape
     * @return Returns the escaped string
     */
    public static String escapeForXPathLike(String str)
    {
        return escape(DEF_XPATH_LIKE, str);
    }

    /**
     * Escape a string according to the <b>regex</b> language syntax.
     * 
     * @param str
     *            the string to escape
     * @return Returns the escaped string
     */
    public static String escapeForRegex(String str)
    {
        return escape(DEF_REGEX, str);
    }

    /**
     * Escape a string according to the <b>Lucene</b> query syntax.
     * 
     * @param str
     *            the string to escape
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
     * @param xpathLikeClause String
     * @return Returns a valid regular expression that is equivalent to the given <b>xpath</b> like clause.
     */
    public static String convertXPathLikeToRegex(String xpathLikeClause)
    {
        return "(?s)" + convert(DEF_XPATH_LIKE, DEF_REGEX, xpathLikeClause);
    }

    /**
     * Convert an <b>xpath</b> like function clause into a <b>Lucene</b> query.
     * 
     * @param xpathLikeClause String
     * @return Returns a valid <b>Lucene</b> expression that is equivalent to the given <b>xpath</b> like clause.
     */
    public static String convertXPathLikeToLucene(String xpathLikeClause)
    {
        return convert(DEF_XPATH_LIKE, DEF_LUCENE, xpathLikeClause);
    }

    /**
     * Convert a <b>sql</b> like function clause into a <b>Lucene</b> query.
     * 
     * @param sqlLikeClause String
     * @return Returns a valid <b>Lucene</b> expression that is equivalent to the given <b>sql</b> like clause.
     */
    public static String convertSQLLikeToLucene(String sqlLikeClause)
    {
        return convert(DEF_SQL_LIKE, DEF_LUCENE_INTERNAL, sqlLikeClause);
    }

    /**
     * Convert a <b>sql</b> like function clause into a <b>regex</b> query.
     * 
     * @param sqlLikeClause String
     * @return Returns a valid regular expression that is equivalent to the given <b>sql</b> like clause.
     */
    public static String convertSQLLikeToRegex(String sqlLikeClause)
    {
        return "(?s)" + convert(DEF_SQL_LIKE, DEF_REGEX, sqlLikeClause);
    }

    /**
     * Convert a <b>CIFS</b> name path into the equivalent <b>Lucene</b> query.
     * 
     * @param cifsNamePath
     *            the CIFS named path
     * @return Returns a valid <b>Lucene</b> expression that is equivalent to the given CIFS name path
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
            if (escaping) // if we are currently escaping, just escape the current character
            {
                if(to.isReserved(chars[i]))
                {
                    sb.append(to.escapeChar); // the to format escape char
                }
                sb.append(chars[i]); // the current char
                escaping = false;
            }
            else if (chars[i] == from.escapeChar) // not escaping and have escape char
            {
                escaping = true;
            }
            else if (query.startsWith(from.multiCharWildcard, i)) // not escaping but have multi-char wildcard
            {
                // translate the wildcard
                sb.append(to.multiCharWildcard);
            }
            else if (query.startsWith(from.singleCharWildcard, i)) // have single-char wildcard
            {
                // translate the wildcard
                sb.append(to.singleCharWildcard);
            }
            else if (to.isReserved(chars[i])) // reserved character
            {
                sb.append(to.escapeChar).append(chars[i]);
            }
            else
            // just a normal char in both
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

        public LuceneLanguageDef(boolean reserve)
        {
            super('\\', "*", "?");
            if (reserve)
            {
                init();
            }
            else
            {
                reserved = "";
            }
        }

        /**
         * Discovers all the reserved chars
         */
        private void init()
        {
            StringBuilder sb = new StringBuilder(20);
            for (char ch = 0; ch < 256; ch++)
            {
                char[] chars = new char[] { ch };
                String unescaped = new String(chars);
                // check it
                String escaped = SearchLanguageConversion.escapeLuceneQuery(unescaped);
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
    
    /**
     * We have to escape lucene query strings outside of lucene - as we do not depend on any given version of lucene
     * The escaping here is taken from lucene 4.9.0
     *  
     * The reserved (and escaped characters) are: 
     *  
     *  \ + - ! ( ) : ^ [ ] " { } ~ * ? | & /
     *  
     *  The escape character is \
     *  
     * @param query String
     * @return - the escaped query string 
     */
    public static String escapeLuceneQuery(String query)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            // These characters are part of the query syntax and must be escaped
            if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':'
                    || c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~'
                    || c == '*' || c == '?' || c == '|' || c == '&' || c == '/') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString(); 
    }
    
    public static String[] tokenizeString(String query)
    {
        String trimmed = StringUtils.trimWhitespace(query);
        if (trimmed == null || trimmed.length() < 1) return new String[]{query};
        List<String> split = new ArrayList<String>();
        
        char[] toSplit = trimmed.toCharArray();
        StringBuffer buff = new StringBuffer();
        
        for (char c : toSplit) {
            if (Character.isWhitespace(c))
            {
                if (buff.length() > 0)
                {
                    split.add(buff.toString());
                    buff = new StringBuffer();
                }
            }
            else 
            {
                buff.append(c);
            }
        }
        
        if (buff.length() > 0)
        {
            split.add(buff.toString());
        }

        return split.toArray(new String[split.size()]);
    }
}
