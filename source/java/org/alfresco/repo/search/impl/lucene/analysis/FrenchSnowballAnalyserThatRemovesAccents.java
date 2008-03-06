package org.alfresco.repo.search.impl.lucene.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.TokenStream;

public class FrenchSnowballAnalyserThatRemovesAccents extends Analyzer
{
    Analyzer analyzer = new FrenchSnowballAnalyser();

    public FrenchSnowballAnalyserThatRemovesAccents()
    {

    }

    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        TokenStream result = analyzer.tokenStream(fieldName, reader);
        result = new ISOLatin1AccentFilter(result);
        return result;
    }

}
