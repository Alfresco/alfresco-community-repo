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
package org.alfresco.repo.search.impl.lucene.analysis;

import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

/**
 * @author andyh
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class PathTokenFilter extends Tokenizer
{
    public final static String INTEGER_FORMAT = "0000000000";

    public final static char PATH_SEPARATOR = ';';

    public final static char NAMESPACE_START_DELIMITER = '{';

    public final static char NAMESPACE_END_DELIMITER = '}';

    public final static String SEPARATOR_TOKEN_TEXT = ";";

    public final static String NO_NS_TOKEN_TEXT = "<No Namespace>";

    public final static String TOKEN_TYPE_PATH_SEP = "PATH_SEPARATOR";

    public final static String TOKEN_TYPE_PATH_LENGTH = "PATH_LENGTH";

    public final static String TOKEN_TYPE_PATH_ELEMENT_NAME = "PATH_ELEMENT_NAME";

    public final static String TOKEN_TYPE_PATH_ELEMENT_NAMESPACE = "PATH_ELEMENT_NAMESPACE";
    
    public final static String TOKEN_TYPE_PATH_ELEMENT_NAMESPACE_PREFIX = "PATH_ELEMENT_NAMESPACE_PREFIX";

    char pathSeparator;

    String separatorTokenText;

    String noNsTokenText;

    char nsStartDelimiter;

    int nsStartDelimiterLength;

    char nsEndDelimiter;

    int nsEndDelimiterLength;

    char nsPrefixDelimiter = ':';

    LinkedList<Token> tokens = new LinkedList<Token>();

    Iterator<Token> it = null;

    private boolean includeNamespace;

    public PathTokenFilter(Reader in, char pathSeparator, String separatorTokenText, String noNsTokenText,
            char nsStartDelimiter, char nsEndDelimiter, boolean includeNameSpace)
    {
        super(in);
        this.pathSeparator = pathSeparator;
        this.separatorTokenText = separatorTokenText;
        this.noNsTokenText = noNsTokenText;
        this.nsStartDelimiter = nsStartDelimiter;
        this.nsEndDelimiter = nsEndDelimiter;
        this.includeNamespace = includeNameSpace;

        this.nsStartDelimiterLength = 1;
        this.nsEndDelimiterLength = 1;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.analysis.TokenStream#next()
     */

    public Token next() throws IOException
    {
        Token nextToken;
        if (it == null)
        {
            buildTokenListAndIterator();
        }
        if (it.hasNext())
        {
            nextToken = it.next();
        }
        else
        {
            nextToken = null;
        }
        return nextToken;
    }

    private void buildTokenListAndIterator() throws IOException
    {
        NumberFormat nf = new DecimalFormat(INTEGER_FORMAT);

        // Could optimise to read each path ata time - not just all paths
        int insertCountAt = 0;
        int lengthCounter = 0;
        Token t;
        Token pathSplitToken = null;
        Token nameToken = null;
        Token countToken = null;
        Token namespaceToken = null;
        while ((t = nextToken()) != null)
        {
            String text = t.termText();

            if (text.length() == 0)
            {
                continue; //  Skip  if we find // or /; or ;; etc 
            }

            
            if (text.charAt(text.length() - 1) == pathSeparator)
            {
                text = text.substring(0, text.length() - 1);
                pathSplitToken = new Token(separatorTokenText, t.startOffset(), t.endOffset(), TOKEN_TYPE_PATH_SEP);
                pathSplitToken.setPositionIncrement(1);

            }

            int split = -1;
            boolean isPrefix = false;

            if ((text.length() > 0) && (text.charAt(0) == nsStartDelimiter))
            {
                split = text.indexOf(nsEndDelimiter);
            }

            if (split == -1)
            {
                split = text.indexOf(nsPrefixDelimiter);
                isPrefix = true;
            }

            if (split == -1)
            {
                namespaceToken = new Token(noNsTokenText, t.startOffset(), t.startOffset(),
                        TOKEN_TYPE_PATH_ELEMENT_NAMESPACE);
                nameToken = new Token(text, t.startOffset(), t.endOffset(), TOKEN_TYPE_PATH_ELEMENT_NAME);

            }
            else
            {
                if (isPrefix)
                {
                    namespaceToken = new Token(text.substring(0, split), t.startOffset(), t.startOffset() + split,
                            TOKEN_TYPE_PATH_ELEMENT_NAMESPACE_PREFIX);
                    nameToken = new Token(text.substring(split + 1), t.startOffset()
                            + split + 1, t.endOffset(), TOKEN_TYPE_PATH_ELEMENT_NAME);
                }
                else
                {
                    namespaceToken = new Token(text.substring(nsStartDelimiterLength,
                            (split + nsEndDelimiterLength - 1)), t.startOffset(), t.startOffset() + split,
                            TOKEN_TYPE_PATH_ELEMENT_NAMESPACE);
                    nameToken = new Token(text.substring(split + nsEndDelimiterLength), t.startOffset()
                            + split + nsEndDelimiterLength, t.endOffset(), TOKEN_TYPE_PATH_ELEMENT_NAME);
                }
            }

            namespaceToken.setPositionIncrement(1);
            nameToken.setPositionIncrement(1);

            if (includeNamespace)
            {
                tokens.add(namespaceToken);
            }
            tokens.add(nameToken);

            lengthCounter++;

            if (pathSplitToken != null)
            {

                String countString = nf.format(lengthCounter);
                countToken = new Token(countString, t.startOffset(), t.endOffset(), TOKEN_TYPE_PATH_SEP);
                countToken.setPositionIncrement(1);

                tokens.add(insertCountAt, countToken);
                tokens.add(pathSplitToken);

                lengthCounter = 0;
                insertCountAt = tokens.size();

                pathSplitToken = null;
            }
        }

        String countString = nf.format(lengthCounter);
        countToken = new Token(countString, 0, 0, TOKEN_TYPE_PATH_SEP);
        countToken.setPositionIncrement(1);

        tokens.add(insertCountAt, countToken);

        if ((tokens.size() == 0) || !(tokens.get(tokens.size() - 1).termText().equals(TOKEN_TYPE_PATH_SEP)))
        {
            pathSplitToken = new Token(separatorTokenText, 0, 0, TOKEN_TYPE_PATH_SEP);
            pathSplitToken.setPositionIncrement(1);
            tokens.add(pathSplitToken);
        }

        it = tokens.iterator();
    }

    int readerPosition = 0;

    private Token nextToken() throws IOException
    {
        if (readerPosition == -1)
        {
            return null;
        }
        StringBuilder buffer = new StringBuilder(64);
        boolean inNameSpace = false;
        int start = readerPosition;
        int current;
        char c;
        while ((current = input.read()) != -1)
        {
            c = (char) current;
            readerPosition++;
            if (c == nsStartDelimiter)
            {
                inNameSpace = true;
            }
            else if (c == nsEndDelimiter)
            {
                inNameSpace = false;
            }
            else if (!inNameSpace && (c == '/'))
            {
                return new Token(buffer.toString(), start, readerPosition - 1, "QNAME");
            }
            else if (!inNameSpace && (c == ';'))
            {
                buffer.append(c);
                return new Token(buffer.toString(), start, readerPosition , "LASTQNAME");
            }
            
            buffer.append(c);
        }
        readerPosition = -1;
        if (!inNameSpace)
        {
            return new Token(buffer.toString(), start, readerPosition - 1, "QNAME");
        }
        else
        {
            throw new IllegalStateException("QName terminated incorrectly: " + buffer.toString());
        }

    }
}