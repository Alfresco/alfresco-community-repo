/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.search.impl.lucene.analysis;

import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class AlfrescoStandardFilter extends TokenFilter
{

    /** Construct filtering <i>in</i>. */
    public AlfrescoStandardFilter(TokenStream in)
    {
        super(in);
    }

    private static final String APOSTROPHE_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.APOSTROPHE];

    private static final String ACRONYM_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.ACRONYM];

    private static final String HOST_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.HOST];

    private static final String ALPHANUM_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.ALPHANUM];

    private Queue<org.apache.lucene.analysis.Token> hostTokens = null;

    /**
     * Returns the next token in the stream, or null at EOS.
     * <p>
     * Removes <tt>'s</tt> from the end of words.
     * <p>
     * Removes dots from acronyms.
     * <p>
     * Splits host names ...
     */
    public final org.apache.lucene.analysis.Token next() throws java.io.IOException
    {
        if (hostTokens == null)
        {
            org.apache.lucene.analysis.Token t = input.next();

            if (t == null)
                return null;

            String text = t.termText();
            String type = t.type();

            if (type == APOSTROPHE_TYPE && // remove 's
                    (text.endsWith("'s") || text.endsWith("'S")))
            {
                return new org.apache.lucene.analysis.Token(text.substring(0, text.length() - 2), t.startOffset(), t
                        .endOffset(), type);

            }
            else if (type == ACRONYM_TYPE)
            { // remove dots
                StringBuffer trimmed = new StringBuffer();
                for (int i = 0; i < text.length(); i++)
                {
                    char c = text.charAt(i);
                    if (c != '.')
                        trimmed.append(c);
                }
                return new org.apache.lucene.analysis.Token(trimmed.toString(), t.startOffset(), t.endOffset(), type);

            }
            else if (type == HOST_TYPE)
            {
                // <HOST: <ALPHANUM> ("." <ALPHANUM>)+ >
                // There must be at least two tokens ....
                hostTokens = new LinkedList<org.apache.lucene.analysis.Token>();
                StringTokenizer tokeniser = new StringTokenizer(text, ".");
                int start = t.startOffset();
                int end;
                while (tokeniser.hasMoreTokens())
                {
                    String token = tokeniser.nextToken();
                    end = start + token.length();
                    hostTokens.offer(new org.apache.lucene.analysis.Token(token, start, end, ALPHANUM_TYPE));
                    start = end + 1;
                }
                // check if we have an acronym ..... yes a.b.c ends up here ...

                if (text.length() == hostTokens.size() * 2 - 1)
                {
                    hostTokens = null;
                    // acronym
                    StringBuffer trimmed = new StringBuffer();
                    for (int i = 0; i < text.length(); i++)
                    {
                        char c = text.charAt(i);
                        if (c != '.')
                            trimmed.append(c);
                    }
                    return new org.apache.lucene.analysis.Token(trimmed.toString(), t.startOffset(), t.endOffset(),
                            ALPHANUM_TYPE);
                }
                else
                {
                    return hostTokens.remove();
                }
            }
            else
            {
                return t;
            }
        }
        else
        {
            org.apache.lucene.analysis.Token token = hostTokens.remove();
            if (hostTokens.isEmpty())
            {
                hostTokens = null;
            }
            return token;
        }
    }
}
