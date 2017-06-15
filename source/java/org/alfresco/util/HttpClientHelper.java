/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.util;

import java.util.StringTokenizer;

import org.alfresco.httpclient.HttpClientFactory;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.lang.StringUtils;
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
 *  
 * This class also provides support for creating proxy configurations, 
 * taking into account System properties like:
 * </p>
 * <ul>
 *  <li>http.proxyHost</li>
 *  <li>http.proxyPort</li>
 *  <li>http.nonProxyHosts</li>
 *  <li>http.proxyUser</li>
 *  <li>http.proxyPassword</li>
 * </ul>
 * <p>
 *  
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
    
    /**
     * Create proxy host for the given system host and port properties.
     * If the properties are not set, no proxy will be created.
     * 
     * @param hostProperty the name of the system property for the proxy server (<code>http.proxyHost</code> or <code>https.proxyHost</code>)
     * @param portProperty the name of the system property for the proxy server port (http.proxyPort)
     * @param defaultPort 
     * 
     * @return ProxyHost if appropriate properties have been set, null otherwise
     */
    public static ProxyHost createProxyHost(final String hostProperty, final String portProperty, final int defaultPort)
    {
        final String proxyHost = System.getProperty(hostProperty);
        ProxyHost proxy = null;
        if (proxyHost != null && proxyHost.length() != 0)
        {
            final String strProxyPort = System.getProperty(portProperty);
            if (strProxyPort == null || strProxyPort.length() == 0)
            {
                proxy = new ProxyHost(proxyHost, defaultPort);
            }
            else
            {
                proxy = new ProxyHost(proxyHost, Integer.parseInt(strProxyPort));
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("ProxyHost: " + proxy.toString());
            }
        }
        return proxy;
    }
    
    /**
     * Create the proxy credentials for the given proxy user and password properties.
     * If the properties are not set, not credentials will be created.
     * @param proxyUserProperty the name of the system property for the proxy user
     * @param proxyPasswordProperty the name of the system property for the proxy password
     * @return Credentials if appropriate properties have been set, null otherwise
     */
    public static Credentials createProxyCredentials(final String proxyUserProperty, final String proxyPasswordProperty) 
    {
        final String proxyUser = System.getProperty(proxyUserProperty);
        final String proxyPassword = System.getProperty(proxyPasswordProperty);
        Credentials credentials = null;
        if (StringUtils.isNotBlank(proxyUser))
        {
            credentials = new UsernamePasswordCredentials(proxyUser, proxyPassword);
        }
        return credentials;
    }
    
    /**
     * Return true unless the given target host is specified in the <code>http.nonProxyHosts</code> system property.
     * See http://docs.oracle.com/javase/8/docs/api/java/net/doc-files/net-properties.html
     * @param targetHost    Non-null host name to verify
     * @return true if not specified in the list, false if it is specified and therefore should be excluded from proxy
     */
    public static boolean requiresProxy(final String targetHost)
    {
        boolean requiresProxy = true;
        final String nonProxyHosts = System.getProperty("http.nonProxyHosts");
        if (nonProxyHosts != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(nonProxyHosts, "|");
            while (tokenizer.hasMoreTokens())
            {
                String pattern = tokenizer.nextToken();
                pattern = pattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
                if (targetHost.matches(pattern))
                {
                    requiresProxy = false;
                    break;
                }
            }
        }
        return requiresProxy;
    }
}
