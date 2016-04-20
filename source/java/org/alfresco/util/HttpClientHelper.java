package org.alfresco.util;

import org.alfresco.httpclient.HttpClientFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.connector.RemoteClient;

/**
 * Helper class to provide access to Thread Local instances of HttpClient.
 * These instances will have been set up in a way that optimises the
 *  performance for one thread doing a fetch and then using the result.
 * You must call releaseConnection() when you're done with the request,
 *  otherwise things will break for the next request in this thread!
 * 
 * TODO Merge me back to Spring Surf, which is where this code has been
 *  pulled out from (was in {@link RemoteClient} but not available externally)
 */
public class HttpClientHelper
{   
    private static Log logger = LogFactory.getLog(HttpClientHelper.class);
            
    // HTTP Client instance - per thread
    private static ThreadLocal<HttpClient> httpClient = new ThreadLocal<HttpClient>()
    {
        @Override
        protected HttpClient initialValue()
        {
            logger.debug("Creating HttpClient instance for thread: " + Thread.currentThread().getName());
            return new HttpClient(new HttpClientFactory.NonBlockingHttpParams());
        }
    };
    
    /**
     * Returns an initialised HttpClient instance for the current thread, which
     *  will have been configured for optimal settings
     */
    public static HttpClient getHttpClient()
    {
        return httpClient.get();
    }
}
