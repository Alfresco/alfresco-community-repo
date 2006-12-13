package org.alfresco.repo.search.impl.lucene.analysis;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

public class SwedishSnowballAnalyser extends SnowballAnalyzer
{

    public SwedishSnowballAnalyser()
    {
        super("Danish");
    }
}
