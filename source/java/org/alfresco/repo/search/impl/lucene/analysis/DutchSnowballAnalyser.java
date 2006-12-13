package org.alfresco.repo.search.impl.lucene.analysis;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

public class DutchSnowballAnalyser extends SnowballAnalyzer
{

    public DutchSnowballAnalyser()
    {
        super("Danish");
    }
}
