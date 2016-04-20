package org.alfresco.repo.search.impl.lucene;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;

/**
 * @author Andy
 *
 */
public abstract class AbstractIndexerAndSearcher implements IndexerAndSearcher
{

    private  Map<String, LuceneQueryLanguageSPI> queryLanguages = new HashMap<String, LuceneQueryLanguageSPI>();
    
    @Override
    public void registerQueryLanguage(LuceneQueryLanguageSPI queryLanguage)
    {
        this.queryLanguages.put(queryLanguage.getName().toLowerCase(), queryLanguage);
    }

    
    @Override
    public Map<String, LuceneQueryLanguageSPI> getQueryLanguages()
    {
        return queryLanguages;
    }

}
