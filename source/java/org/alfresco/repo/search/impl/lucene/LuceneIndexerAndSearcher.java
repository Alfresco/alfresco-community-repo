package org.alfresco.repo.search.impl.lucene;

import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.IndexerException;

public interface LuceneIndexerAndSearcher extends IndexerAndSearcher, LuceneConfig
{
    public int prepare() throws IndexerException;
    public void commit() throws IndexerException;
    public void rollback();
    
    
    public interface ReadOnlyWork<Result>
    {
        public Result doWork() throws Exception;
    }

    public <R> R doReadOnly(ReadOnlyWork<R> lockWork);
 
    
}
