package org.alfresco.repo.search.impl.lucene.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import org.alfresco.repo.search.MLAnalysisMode;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

/**
 * Create duplicate tokens for multilingual varients The forms are Tokens: Token - all languages {fr}Token - if a
 * language is specified {fr_CA}Token - if a language and country is specified {fr_CA_Varient}Token - for all three
 * {fr__Varient}Token - for a language varient with no country
 * 
 * @author andyh
 */
public class MLTokenDuplicator extends Tokenizer
{
    private static Logger s_logger = Logger.getLogger(MLTokenDuplicator.class);
    
    TokenStream source;

    Locale locale;

    Iterator<Token> it;

    HashSet<String> prefixes;

    public MLTokenDuplicator(TokenStream source, Locale locale, Reader reader, MLAnalysisMode mlAnalaysisMode)
    {
        super(reader);
        this.source = source;
        this.locale = locale;

        boolean l = locale.getLanguage().length() != 0;
        boolean c = locale.getCountry().length() != 0;
        boolean v = locale.getVariant().length() != 0;

        prefixes = new HashSet<String>(4);
        if (mlAnalaysisMode.includesAll())
        {
            prefixes.add("");
        }

        if (mlAnalaysisMode.includesExact())
        {
            StringBuffer result = new StringBuffer();
            result.append("{").append(locale.toString()).append("}");
            prefixes.add(result.toString());
        }

        if (mlAnalaysisMode.includesContaining())
        {
            if (v)
            {
                Locale noVarient = new Locale(locale.getLanguage(), locale.getCountry(), "");
                StringBuffer result = new StringBuffer();
                result.append("{").append(noVarient.toString()).append("}");
                prefixes.add(result.toString());

                Locale noCountry = new Locale(locale.getLanguage(), "", "");
                result = new StringBuffer();
                result.append("{").append(noCountry.toString()).append("}");
                prefixes.add(result.toString());
            }
            if (c)
            {
                Locale noCountry = new Locale(locale.getLanguage(), "", "");
                StringBuffer result = new StringBuffer();
                result.append("{").append(noCountry.toString()).append("}");
                prefixes.add(result.toString());
            }
        }

        if (mlAnalaysisMode.includesContained())
        {
            // varients have not contained
            if (!v)
            {
                if (!c)
                {
                    if (!l)
                    {
                        // All
                        for (Locale toAdd : Locale.getAvailableLocales())
                        {
                            StringBuffer result = new StringBuffer();
                            result.append("{").append(toAdd.toString()).append("}");
                            prefixes.add(result.toString());
                        }
                    }
                    else
                    {
                        // All that match language
                        for (Locale toAdd : Locale.getAvailableLocales())
                        {
                            if (locale.getLanguage().equals(toAdd.getLanguage()))
                            {
                                StringBuffer result = new StringBuffer();
                                result.append("{").append(toAdd.toString()).append("}");
                                prefixes.add(result.toString());
                            }
                        }
                    }
                }
                else
                {
                    // All that match language and country
                    for (Locale toAdd : Locale.getAvailableLocales())
                    {
                        if ((locale.getLanguage().equals(toAdd.getLanguage()))
                                && (locale.getCountry().equals(toAdd.getCountry())))
                        {
                            StringBuffer result = new StringBuffer();
                            result.append("{").append(toAdd.toString()).append("}");
                            prefixes.add(result.toString());
                        }
                    }
                }
            }
        }
        if(s_logger.isDebugEnabled())
        {
            s_logger.debug("Locale "+ locale +" using "+mlAnalaysisMode+" is "+prefixes);
        }

    }

    @Override
    public Token next() throws IOException
    {
        if (it == null)
        {
            it = buildIterator();
        }
        if (it == null)
        {
            return null;
        }
        if (it.hasNext())
        {
            return it.next();
        }
        else
        {
            it = null;
            return this.next();
        }
    }

    private Iterator<Token> buildIterator() throws IOException
    {
        Token token = source.next();
        if (token == null)
        {
            return null;
        }

        ArrayList<Token> tokens = new ArrayList<Token>(prefixes.size());
        for (String prefix : prefixes)
        {
            Token newToken = new Token(prefix + token.termText(), token.startOffset(), token.endOffset(), token.type());
            if (tokens.size() == 0)
            {
                newToken.setPositionIncrement(token.getPositionIncrement());
            }
            else
            {
                newToken.setPositionIncrement(0);
            }
            tokens.add(newToken);
        }
        return tokens.iterator();

    }

}
