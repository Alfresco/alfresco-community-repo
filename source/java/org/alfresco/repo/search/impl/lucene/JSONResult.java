package org.alfresco.repo.search.impl.lucene;

/**
 * Json returned from Solr
 *
 * @author Gethin James
 * @since 5.0
 */
public interface JSONResult
{
    public Long getQueryTime();
    public long getNumberFound();
}
