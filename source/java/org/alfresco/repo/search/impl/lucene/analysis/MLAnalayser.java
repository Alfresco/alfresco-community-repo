package org.alfresco.repo.search.impl.lucene.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

public class MLAnalayser extends Analyzer
{
    private static Logger s_logger = Logger.getLogger(MLAnalayser.class);

    private DictionaryService dictionaryService;

    private HashMap<Locale, Analyzer> analysers = new HashMap<Locale, Analyzer>();
    
    private MLAnalysisMode mlAnalaysisMode;

    public MLAnalayser(DictionaryService dictionaryService, MLAnalysisMode mlAnalaysisMode)
    {
        this.dictionaryService = dictionaryService;
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
                            return getDefaultAnalyser().tokenStream(fieldName, breader);
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
                                return getDefaultAnalyser().tokenStream(fieldName, breader);
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
                            return getDefaultAnalyser().tokenStream(fieldName, breader);
                        }
                    }
                    Locale locale = new Locale(language, country, varient);
                    // leave the reader where it is ....
                    return new MLTokenDuplicator(getAnalyser(locale).tokenStream(fieldName, breader), locale, breader, mlAnalaysisMode);
                }
                else
                {
                    breader.reset();
                    return getDefaultAnalyser().tokenStream(fieldName, breader);
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
                return getDefaultAnalyser().tokenStream(fieldName, breader);
            }

        }
        else
        {
            throw new AnalysisException("Multilingual tokenisation requires a buffered reader");
        }
    }

    private Analyzer getDefaultAnalyser()
    {
        return getAnalyser(I18NUtil.getLocale());
    }

    private Analyzer getAnalyser(Locale locale)
    {
        Analyzer analyser = (Analyzer) analysers.get(locale);
        if (analyser == null)
        {
            analyser = findAnalyser(locale);
        }
        // wrap analyser to produce plain and prefixed tokens
        return analyser;
    }

    private Analyzer findAnalyser(Locale locale)
    {
        Analyzer analyser = loadAnalyzer(locale);
        analysers.put(locale, analyser);
        return analyser;
    }

    private Analyzer loadAnalyzer(Locale locale)
    {
        DataTypeDefinition dataType = dictionaryService.getDataType(DataTypeDefinition.TEXT);
        String analyserClassName = dataType.getAnalyserClassName(locale);
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Loading " + analyserClassName + " for " + locale);
        }
        try
        {
            Class<?> clazz = Class.forName(analyserClassName);
            Analyzer analyser = (Analyzer) clazz.newInstance();
            return analyser;
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Unable to load analyser for property of type "
                    + dataType.getName() + " using " + analyserClassName);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException("Unable to load analyser for property of type "
                    + dataType.getName() + " using " + analyserClassName);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Unable to load analyser for property of type "
                    + dataType.getName() + " using " + analyserClassName);
        }
    }
}
