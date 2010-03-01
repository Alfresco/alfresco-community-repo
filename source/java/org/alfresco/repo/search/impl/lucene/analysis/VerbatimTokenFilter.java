/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.search.impl.lucene.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

public class VerbatimTokenFilter extends Tokenizer
{
    boolean readInput = true;

    boolean lowerCase;
    
    VerbatimTokenFilter(Reader in, boolean lowerCase)
    {
        super(in);
        this.lowerCase = lowerCase;
    }

    @Override
    public Token next() throws IOException
    {
        if (readInput)
        {
            readInput = false;
            StringBuilder buffer = new StringBuilder();
            int current;
            char c;
            while ((current = input.read()) != -1)
            {
                c = (char) current;
                buffer.append(c);
            }

            String token = buffer.toString();
            if(lowerCase)
            {
                token = token.toLowerCase();
            }
            return new Token(token, 0, token.length() - 1, "VERBATIM");
        }
        else
        {
            return null;
        }
    }

}
