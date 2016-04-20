package org.alfresco.repo.search.impl.solr;

import org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryLanguage;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.StatsParameters;
import org.alfresco.service.cmr.search.StatsResultSet;

/**
 * @author Andy
 *
 */
public class SolrQueryLanguage extends AbstractLuceneQueryLanguage
{
    private SolrQueryHTTPClient solrQueryHTTPClient;

    
    
    public void setSolrQueryHTTPClient(SolrQueryHTTPClient solrQueryHTTPClient)
    {
        this.solrQueryHTTPClient = solrQueryHTTPClient;
    }



    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI#executeQuery(org.alfresco.service.cmr.search.SearchParameters, org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl)
     */
    @Override
    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
       return solrQueryHTTPClient.executeQuery(searchParameters, getName());
    }
    
    /**
     * Executes a stats query using solr.
     * @param searchParameters StatsParameters
     * @return StatsResultSet
     */
    public StatsResultSet executeStatsQuery(StatsParameters searchParameters)
    {
       return solrQueryHTTPClient.executeStatsQuery( searchParameters);

    }

}
