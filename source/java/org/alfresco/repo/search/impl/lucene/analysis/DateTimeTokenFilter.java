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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

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

    Iterator<Token> tokenIterator = null;

    public DateTimeTokenFilter(Reader in)
    {
        super(in);
        baseTokeniser = new WhitespaceTokenizer(in);
    }

    public Token next() throws IOException
    {
        if (tokenIterator == null)
        {
            buildIterator();
        }
        if (tokenIterator.hasNext())
        {
            return tokenIterator.next();
        }
        else
        {
            return null;
        }
    }

    public void buildIterator() throws IOException
    {
        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", true);
        Token candidate;
        ArrayList<Token> tokens = new ArrayList<Token>();
        while ((candidate = baseTokeniser.next()) != null)
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

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            Token token;

            // four digits
            token = new Token("YE" + cal.get(Calendar.YEAR), candidate.startOffset(), candidate.startOffset(), candidate.type());
            tokens.add(token);

            // 2 digits
            int month = cal.get(Calendar.MONTH);
            if (month < 10)
            {
                token = new Token("MO0" + month, candidate.startOffset(), candidate.startOffset(), candidate.type());
                tokens.add(token);
            }
            else
            {
                token = new Token("MO" + month, candidate.startOffset(), candidate.startOffset(), candidate.type());
                tokens.add(token);
            }

            int day = cal.get(Calendar.DAY_OF_MONTH);
            if (day < 10)
            {
                token = new Token("DA0" + day, candidate.startOffset(), candidate.startOffset(), candidate.type());
                tokens.add(token);
            }
            else
            {
                token = new Token("DA" + day, candidate.startOffset(), candidate.startOffset(), candidate.type());
                tokens.add(token);
            }

            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (hour < 10)
            {
                token = new Token("HO0" + hour, candidate.startOffset(), candidate.startOffset(), candidate.type());
                tokens.add(token);
            }
            else
            {
                token = new Token("HO" + hour, candidate.startOffset(), candidate.startOffset(), candidate.type());
                tokens.add(token);
            }

            int minute = cal.get(Calendar.MINUTE);
            if (minute < 10)
            {
                token = new Token("MI0" + minute, candidate.startOffset(), candidate.startOffset(), candidate.type());
                tokens.add(token);
            }
            else
            {
                token = new Token("MI" + minute, candidate.startOffset(), candidate.startOffset(), candidate.type());
                tokens.add(token);
            }

            int second = cal.get(Calendar.SECOND);
            if (second < 10)
            {
                token = new Token("SE0" + second, candidate.startOffset(), candidate.startOffset(), candidate.type());
                tokens.add(token);
            }
            else
            {
                token = new Token("SE" + second, candidate.startOffset(), candidate.startOffset(), candidate.type());
                tokens.add(token);
            }

            int millis = cal.get(Calendar.MILLISECOND);
            if (millis < 10)
            {
                token = new Token("MS00" + millis, candidate.startOffset(), candidate.startOffset(), candidate.type());
                tokens.add(token);
            }
            else if (millis < 100)
            {
                token = new Token("MS0" + millis, candidate.startOffset(), candidate.startOffset(), candidate.type());
                tokens.add(token);
            }
            else
            {
                token = new Token("MS" + millis, candidate.startOffset(), candidate.startOffset(), candidate.type());
                tokens.add(token);
            }

            break;
        }

        tokenIterator = tokens.iterator();
    }
}