package org.alfresco.repo.search.impl.lucene.analysis;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

public class LovinsSnowballAnalyser extends SnowballAnalyzer
{

    public LovinsSnowballAnalyser()
    {
        super("Danish");
    }
}
