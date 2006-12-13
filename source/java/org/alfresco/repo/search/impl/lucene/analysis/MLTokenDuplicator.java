package org.alfresco.repo.search.impl.lucene.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

/**
 * Create duplicate tokens for multilingual varients
 * 
 * The forms are 
 * 
 * Tokens:
 * Token - all languages
 * {fr}Token - if a language is specified 
 * {fr_CA}Token - if a language and country is specified
 * {fr_CA_Varient}Token - for all three
 * {fr__Varient}Token - for a language varient with no country
 * 
 * @author andyh
 *
 */
public class MLTokenDuplicator extends Tokenizer
{
    TokenStream source;

    Locale locale;

    Iterator<Token> it;

    ArrayList<String> prefixes;

    public MLTokenDuplicator(TokenStream source, Locale locale, Reader reader)
    {
        super(reader);
        this.source = source;
        this.locale = locale;

        boolean l = locale.getLanguage().length() != 0;
        boolean c = locale.getCountry().length() != 0;
        boolean v = locale.getVariant().length() != 0;

        prefixes = new ArrayList<String>(4);
        prefixes.add("");

        if (l)
        {
            StringBuffer result = new StringBuffer();
            result.append("{").append(locale.getLanguage()).append("}");
            prefixes.add(result.toString());
            result.deleteCharAt(result.length()-1);
            
            if (c || (l && v))
            {
                result.append('_').append(locale.getCountry()).append("}"); 
                prefixes.add(result.toString());
                result.deleteCharAt(result.length()-1);
            }
            if (v && (l || c))
            {
                result.append('_').append(locale.getVariant()).append("}");
                prefixes.add(result.toString());
            }
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
        if(it.hasNext())
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
        for(String prefix : prefixes)
        {
            Token newToken = new Token(prefix+token.termText(), token.startOffset(), token.endOffset(), token.type());
            if(tokens.size() == 0)
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
