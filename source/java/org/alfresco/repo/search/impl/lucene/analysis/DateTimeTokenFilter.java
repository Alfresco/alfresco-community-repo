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
package org.alfresco.repo.search.impl.lucene.analysis;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.alfresco.util.CachingDateFormat;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;

/**
 * @author andyh
 */
public class DateTimeTokenFilter extends Tokenizer
{
    Tokenizer baseTokeniser;
    
    public DateTimeTokenFilter(Reader in)
    {
        super(in);
        baseTokeniser = new WhitespaceTokenizer(in);
    }

    public Token next() throws IOException
    {
        SimpleDateFormat df = CachingDateFormat.getDateFormat();
        SimpleDateFormat dof = CachingDateFormat.getDateFormat();
        Token candidate;
        while((candidate = baseTokeniser.next()) != null)
        {
            Date date;
            try
            {
                date = df.parse(candidate.termText());
            }
            catch (ParseException e)
            {
               continue;
            }
            String valueString = dof.format(date);
            Token integerToken = new Token(valueString, candidate.startOffset(), candidate.startOffset(),
                    candidate.type());
            return integerToken;
        }
        return null;
    }
}