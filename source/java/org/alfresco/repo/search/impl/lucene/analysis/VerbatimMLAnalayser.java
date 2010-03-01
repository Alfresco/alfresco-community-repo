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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

import org.alfresco.repo.search.MLAnalysisMode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

public class VerbatimMLAnalayser extends Analyzer
{
    private static Log    s_logger = LogFactory.getLog(VerbatimMLAnalayser.class);

   
    private MLAnalysisMode mlAnalaysisMode;

    public VerbatimMLAnalayser(MLAnalysisMode mlAnalaysisMode)
    {
        this.mlAnalaysisMode = mlAnalaysisMode;
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        // We use read ahead to get the language info - if this does not exist we need to restart
        // an use the default - there foer we need mark and restore.

        if (!(reader instanceof BufferedReader))
        {
            BufferedReader breader = new BufferedReader(reader);
            try
            {
                if (!breader.markSupported())
                {
                    throw new AnalysisException(
                            "Multilingual tokenisation requires a reader that supports marks and reset");
                }
                breader.mark(100);
                StringBuilder builder = new StringBuilder();
                if (breader.read() == '\u0000')
                {
                    String language = "";
                    String country = "";
                    String varient = "";
                    char c;
                    int count = 0;
                    while ((c = (char) breader.read()) != '\u0000')
                    {
                        if (count++ > 99)
                        {
                            breader.reset();
                            return getAnalyser().tokenStream(fieldName, breader);
                        }
                        if (c == '_')
                        {
                            if (language.length() == 0)
                            {
                                language = builder.toString();
                            }
                            else if (country.length() == 0)
                            {
                                country = builder.toString();
                            }
                            else if (varient.length() == 0)
                            {
                                varient = builder.toString();
                            }
                            else
                            {
                                breader.reset();
                                return getAnalyser().tokenStream(fieldName, breader);
                            }
                            builder = new StringBuilder();
                        }
                        else
                        {
                            builder.append(c);
                        }
                    }
                    if (builder.length() > 0)
                    {
                        if (language.length() == 0)
                        {
                            language = builder.toString();
                        }
                        else if (country.length() == 0)
                        {
                            country = builder.toString();
                        }
                        else if (varient.length() == 0)
                        {
                            varient = builder.toString();
                        }
                        else
                        {
                            breader.reset();
                            return getAnalyser().tokenStream(fieldName, breader);
                        }
                    }
                    Locale locale = new Locale(language, country, varient);
                    // leave the reader where it is ....
                    return new MLTokenDuplicator(getAnalyser().tokenStream(fieldName, breader), locale, breader, mlAnalaysisMode);
                }
                else
                {
                    breader.reset();
                    return getAnalyser().tokenStream(fieldName, breader);
                }
            }
            catch (IOException io)
            {
                try
                {
                    breader.reset();
                }
                catch (IOException e)
                {
                    throw new AnalysisException("Failed to reset buffered reader - token stream will be invalid", e);
                }
                return getAnalyser().tokenStream(fieldName, breader);
            }

        }
        else
        {
            throw new AnalysisException("Multilingual tokenisation requires a buffered reader");
        }
    }

    /**
     * @return
     */
    private Analyzer getAnalyser()
    {
        return new VerbatimAnalyser(false);
    }

    

   
}
