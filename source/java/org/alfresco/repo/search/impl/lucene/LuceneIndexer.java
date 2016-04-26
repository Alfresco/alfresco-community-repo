package org.alfresco.repo.search.impl.lucene;

import java.util.Set;

import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.TransactionSynchronisationAwareIndexer;
import org.alfresco.repo.search.impl.lucene.index.IndexInfo;

/**
 * @author Andy Hind
 */
public interface LuceneIndexer extends Indexer, TransactionSynchronisationAwareIndexer
{ 
    public String getDeltaId();
    public Set<String> getDeletions();
    public Set<String> getContainerDeletions();
    public boolean getDeleteOnlyNodes();   
    public <R> R doReadOnly(IndexInfo.LockWork <R> lockWork);
}
