package org.alfresco.repo.search.impl.solr;

import org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryLanguage;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * @author Andy
 *
 */
public class SolrXPathQueryLanguage extends AbstractLuceneQueryLanguage
{
    SolrQueryLanguage solrQueryLanguage;
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI#executeQuery(org.alfresco.service.cmr.search.SearchParameters, org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl)
     */
    @Override
    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
        String query = "PATH:\""+searchParameters.getQuery()+"\"";
        SearchParameters sp = searchParameters.copy();
        sp.setLanguage(SearchService.LANGUAGE_SOLR_FTS_ALFRESCO);
        sp.setQuery(query);
        return solrQueryLanguage.executeQuery(sp, admLuceneSearcher);
    }

    public SolrQueryLanguage getSolrQueryLanguage()
    {
        return solrQueryLanguage;
    }

    public void setSolrQueryLanguage(SolrQueryLanguage solrQueryLanguage)
    {
        this.solrQueryLanguage = solrQueryLanguage;
    }

}
