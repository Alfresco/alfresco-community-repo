/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
