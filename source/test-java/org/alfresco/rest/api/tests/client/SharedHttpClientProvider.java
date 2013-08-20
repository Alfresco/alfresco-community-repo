/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.api.tests.client;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 *
 * A class responsible for managing one shared {@link HttpClient} instance. This uses a
 * thread-safe connection-manager instead of creating a new istance on every call.
 * 
 * This is done for the folowing reasons:
 * <br> 
 * <ul>
 *   <li>Creating new HTTPClient instances for each request has memory-overhead and most 
 *   important, opens a new connection for each request. Even though the connection is released
 *   they are kept in 'CLOSE_WAIT' state by the OS. This can, on high usage, empty out available outgoing TCP-ports and
 *   influence the testing on the client, having impact on the results.</li>
 *   <li>Using a single HTTPClient allows creating a pool of connections that can be kept alive to
 *   a certain route (route = combination of host and port) for a max number of time. This eliminates connection 
 *   setup and additional server round-trips, raising the overall throughput of http-calls (Browsers use 
 *   this mechanism all the time to lower loading-times).</li>
 * </ul>
 * 
 * @author Frederik Heremans
 */
public final class SharedHttpClientProvider implements HttpClientProvider
{
    private HttpClient client;
    private String alfrescoUrl;
    
    // Private constructor to prevent instantiation
    public SharedHttpClientProvider(String alfrescoUrl, int maxNumberOfConnections)
    {
        setAlfrescoUrl(alfrescoUrl);
        
        // Initialize manager
        MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setMaxTotalConnections(maxNumberOfConnections);
        params.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, maxNumberOfConnections);

        // Create the client
        client = new HttpClient(manager);
        client.getParams().setAuthenticationPreemptive(true);
    }
    
    /**
     * @param alfrescoUrl the URL where alfresco repo is running on.
     */
    private void setAlfrescoUrl(String alfrescoUrl)
    {
        this.alfrescoUrl = alfrescoUrl;
        // Ensure path ends with forward slash
        if(alfrescoUrl != null && !alfrescoUrl.endsWith("/"))
        {
            this.alfrescoUrl = alfrescoUrl.concat("/");
        }
    }
    
    public String getFullAlfrescoUrlForPath(String path)
    {
        if(path.startsWith("/"))
        {
            return alfrescoUrl.concat(path.substring(1, path.length()));
        }
        return alfrescoUrl.concat(path);
    }

    public HttpClient getHttpClient()
    {
        return client;
    }
}
