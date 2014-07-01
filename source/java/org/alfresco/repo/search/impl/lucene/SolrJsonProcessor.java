package org.alfresco.repo.search.impl.lucene;

import org.json.JSONObject;

/**
 * Processes Json returned from Solr
 *
 * @author Gethin James
 */
public interface SolrJsonProcessor<T extends JSONResult>
{
    public T getResult(JSONObject json);
}
