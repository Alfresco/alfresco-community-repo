/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import static org.apache.lucene.search.WildcardQuery.WILDCARD_STRING;

import java.util.regex.Pattern;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;

import org.alfresco.util.ISO9075;

/**
 * Utility class used for ES path query conversion
 */
public final class PathQueryConverter
{
    static final Pattern REGEX_CHARS_TO_ESCAPE = Pattern.compile("[.?+^|{}\\[\\]()\"#@&<>~\\\\]");
    private static final String SLASH = "/";
    private static final String WILDCARD = String.valueOf(WILDCARD_STRING);
    private static final String DOUBLE_SLASH = "//";
    private static final String EMPTY_STRING = "";
    private static final String DOT = ".";
    private static final String SLASH_DOT = SLASH + DOT;
    private static final String DOT_SLASH = DOT + SLASH;
    static final String PATH_KEYWORD = "PATH.keyword";
    private static final String REGEXP_ANY_BUT_SLASH = "[^/]+";
    private static final String REGEXP_SLASH_ANYTHING = "/(.*/)?";

    private PathQueryConverter()
    {}

    static boolean isPathQuery(final RegexpQuery regexpQuery)
    {
        return PATH_KEYWORD.equals(regexpQuery.getField());
    }

    static Query toLucenePathQuery(final String pathQueryInput)
    {
        final String pathQuery = adjustWildcardsAndDoubleSlashes(adjustPathPhrase(pathQueryInput));
        return new RegexpQuery(new Term(PATH_KEYWORD, pathQuery));
    }

    /**
     * The method replaces '//', '*' with following strings: // -> /(.'*'/)? regexp: slash followed by a group of [any character (0 or more times) and slash] (0 or 1 time) (asterisk in '' to keep javadoc working) * -> [^/]+ regexp: any character but slash (/) occurring at least once
     *
     * @param pathQuery
     *            path query
     * @return adjusted path query
     */
    private static String adjustWildcardsAndDoubleSlashes(final String pathQuery)
    {
        return pathQuery
                .replace(WILDCARD, REGEXP_ANY_BUT_SLASH) // (regexp "[^/]+")
                .replace(DOUBLE_SLASH, REGEXP_SLASH_ANYTHING); // (regexp "/(.*/)?")
    }

    /**
     * The method will decode whitespace characters Also, punctuation character ('.') is removed from the query Additionally, if the query doesn't start with '/' then initial '/' is added.
     *
     * @param pathPhrase
     *            path query
     * @return adjusted path query (if applicable)
     */
    private static String adjustPathPhrase(final String pathPhrase)
    {
        String adjusted = ISO9075.decode(pathPhrase);
        if (!pathPhrase.startsWith(SLASH) && !pathPhrase.startsWith(DOT_SLASH))
        {
            adjusted = SLASH + adjusted;
        }
        if (pathPhrase.endsWith(SLASH_DOT))
        {
            return adjusted.replace(SLASH_DOT, EMPTY_STRING);
        }
        if (pathPhrase.startsWith(DOT_SLASH))
        {
            return adjusted.replace(DOT_SLASH, SLASH);
        }
        return escapeSpecialRegexChars(adjusted);
    }

    /**
     * This method escapes all but asterisk (*) regex special characters. Asterisk is not escaped, because it can be part of PATH Query (XPath alike)
     * 
     * @param str
     *            input string
     * @return input string with special chars escaped with \\
     */
    static String escapeSpecialRegexChars(final String str)
    {
        return REGEX_CHARS_TO_ESCAPE.matcher(str).replaceAll("\\\\$0");
    }
}
