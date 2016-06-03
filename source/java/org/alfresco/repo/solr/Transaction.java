package org.alfresco.repo.solr;

/**
 * Interface for SOLR transaction objects.
 * 
 * @since 4.0
 */
public interface Transaction
{
    public Long getId();
    public Long getCommitTimeMs();
    public int getUpdates();
    public int getDeletes();
}
