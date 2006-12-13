package org.alfresco.repo.search.impl.lucene.analysis;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

public class GermanSnowballAnalyser extends SnowballAnalyzer
{

    public GermanSnowballAnalyser()
    {
        super("Danish");
    }
}
