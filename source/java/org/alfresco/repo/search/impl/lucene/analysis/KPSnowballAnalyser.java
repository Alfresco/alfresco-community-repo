package org.alfresco.repo.search.impl.lucene.analysis;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

public class KPSnowballAnalyser extends SnowballAnalyzer
{

    public KPSnowballAnalyser()
    {
        super("Danish");
    }
}
