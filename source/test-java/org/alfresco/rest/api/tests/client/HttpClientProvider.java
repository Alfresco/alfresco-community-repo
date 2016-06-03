package org.alfresco.rest.api.tests.client;

import org.apache.commons.httpclient.HttpClient;

/**
 * Provides {@link HttpClient} instance to be used to perform HTTP-calls.
 *
 * @author Frederik Heremans
 */
public interface HttpClientProvider
{
    /**
     * @return the {@link HttpClient} instance to use for the next HTTP-call.
     */
    HttpClient getHttpClient();
    
    /**
     * @param path relative path of the URL from alfresco host.
     * @return full URL including hostname and port for the given path.
     */
    String getFullAlfrescoUrlForPath(String path);
}
