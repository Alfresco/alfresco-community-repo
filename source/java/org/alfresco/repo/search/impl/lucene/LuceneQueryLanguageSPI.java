package org.alfresco.repo.search.impl.lucene;

import java.util.List;

import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.apache.lucene.search.Searcher;

/**
 * @author andyh
 *
 */
public interface LuceneQueryLanguageSPI
{
    /**
     * The unique name for the query language
     * @return - the unique name
     */
    public String getName();
    
    /**
     * Execute the query
     * @param searchParameters SearchParameters
     * @param admLuceneSearcher ADMLuceneSearcherImpl
     * @return - the query results
     */
    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher);
    
    /**
     * Register
     */
    public void setFactories(List<IndexerAndSearcher> factories);
}
