package org.alfresco.repo.search.impl.lucene.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

public class VerbatimAnalyser extends Analyzer
{
    boolean lowerCase;
    
    public VerbatimAnalyser()
    {
        lowerCase = false;
    }
    
    public VerbatimAnalyser(boolean lowerCase)
    {
        super();
        this.lowerCase = lowerCase;
    }

    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        return new VerbatimTokenFilter(reader, lowerCase);
    }
}
