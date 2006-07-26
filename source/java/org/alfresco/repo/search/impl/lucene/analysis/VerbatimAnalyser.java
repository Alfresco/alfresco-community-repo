package org.alfresco.repo.search.impl.lucene.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

public class VerbatimAnalyser

extends Analyzer
{

    public VerbatimAnalyser()
    {
        super();
    }

    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        return new VerbatimTokenFilter(reader);
    }
}
