package org.alfresco.repo.search.impl.lucene.analysis;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

public class NorwegianSnowballAnalyser extends SnowballAnalyzer
{

    public NorwegianSnowballAnalyser()
    {
        super("Danish");
    }
}
