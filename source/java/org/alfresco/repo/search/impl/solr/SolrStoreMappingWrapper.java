package org.alfresco.repo.search.impl.solr;

import org.alfresco.util.Pair;
import org.apache.commons.httpclient.HttpClient;

/**
 * @author Andy
 *
 */
public interface SolrStoreMappingWrapper
{

    /**
     * @return
     */
    Pair<HttpClient, String> getHttpClientAndBaseUrl();

    /**
     * @return
     */
    boolean isSharded();

    /**
     * @return
     */
    String getShards();

}
