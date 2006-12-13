package org.alfresco.repo.search.impl.lucene.analysis;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

public class PorterSnowballAnalyser extends SnowballAnalyzer
{

    public PorterSnowballAnalyser()
    {
        super("Danish");
    }
}
