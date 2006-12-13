package org.alfresco.repo.search.impl.lucene.analysis;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

public class DanishSnowballAnalyser extends SnowballAnalyzer
{

    public DanishSnowballAnalyser()
    {
        super("Danish");
    }
}
