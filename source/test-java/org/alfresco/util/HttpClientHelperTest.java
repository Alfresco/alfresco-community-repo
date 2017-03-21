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

import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * Tests for {@link HttpClientHelper}
 * 
 * @author Ancuta Morarasu
 */
public class HttpClientHelperTest extends TestCase
{
    private static final String HTTP = "http";
    private static final int DEFAULT_HTTP_PORT = 8888;
    private static final int DEFAULT_HTTPS_PORT = 444;

    @Override
    protected void tearDown() throws Exception
    {
        clearHTTPSystemProperties();
    }
    
    public void testHTTPProxySettings()
    {
        String host = "testHost";
        Integer port = 8080;
        
        setHTTPSystemProperties(host, port, "user1", "password", null);
        ProxyHost proxyHost = HttpClientHelper.createProxyHost("http.proxyHost", "http.proxyPort", DEFAULT_HTTP_PORT);
        UsernamePasswordCredentials proxyCredentials = (UsernamePasswordCredentials) HttpClientHelper.createProxyCredentials("http.proxyUser", "http.proxyPassword");
        
        assertEquals(HTTP, proxyHost.getProtocol().getScheme());
        assertEquals(host, proxyHost.getHostName());
        assertEquals(port, Integer.valueOf(proxyHost.getPort()));
        assertEquals("user1", proxyCredentials.getUserName());
        assertEquals("password", proxyCredentials.getPassword()); 
        
        // Test default port and no credentials
        setHTTPSystemProperties(host, null, null, null, null);
        proxyHost = HttpClientHelper.createProxyHost("http.proxyHost", "http.proxyPort", DEFAULT_HTTP_PORT);
        proxyCredentials = (UsernamePasswordCredentials) HttpClientHelper.createProxyCredentials("http.proxyUser", "http.proxyPassword");
        
        assertEquals(HTTP, proxyHost.getProtocol().getScheme());
        assertEquals(host, proxyHost.getHostName());
        assertEquals(DEFAULT_HTTP_PORT, proxyHost.getPort());
        assertNull(proxyCredentials); 
    }
    
    public void testHTTPSProxySettings()
    {
        String host = "testHost";
        Integer port = 8444;
        
        setHTTPSSystemProperties(host, port, "user1", "password", null);
        ProxyHost proxyHost = HttpClientHelper.createProxyHost("https.proxyHost", "https.proxyPort", DEFAULT_HTTPS_PORT);
        UsernamePasswordCredentials proxyCredentials = (UsernamePasswordCredentials) HttpClientHelper.createProxyCredentials("https.proxyUser", "https.proxyPassword");
        
        // Proxy hosts always use plain HTTP connection when communicating with clients (by commons.httpclient doc)
        assertEquals(HTTP, proxyHost.getProtocol().getScheme());
        assertEquals(host, proxyHost.getHostName());
        assertEquals(port, Integer.valueOf(proxyHost.getPort()));
        assertEquals("user1", proxyCredentials.getUserName());
        assertEquals("password", proxyCredentials.getPassword()); 
        
        // Test default port and no credentials
        setHTTPSSystemProperties(host, null, null, null, null);
        proxyHost = HttpClientHelper.createProxyHost("https.proxyHost", "https.proxyPort", DEFAULT_HTTPS_PORT);
        proxyCredentials = (UsernamePasswordCredentials) HttpClientHelper.createProxyCredentials("https.proxyUser", "https.proxyPassword");
        
        assertEquals(host, proxyHost.getHostName());
        assertEquals(DEFAULT_HTTPS_PORT, proxyHost.getPort());
        assertNull(proxyCredentials);
    }
    
    public void testNonProxyHosts()
    {
        String host = "testHost";
        Integer port = 8080;
        String nonProxyHosts = "local*|127.0.*|100.100.100.*|11.*.*23|www.foo.*|";
        
        setHTTPSystemProperties(host, port, null, null, nonProxyHosts);
        
        assertEquals(false, HttpClientHelper.requiresProxy("localhost"));
        assertEquals(false, HttpClientHelper.requiresProxy("100.100.100.222"));
        assertEquals(false, HttpClientHelper.requiresProxy("11.11.11.123"));
        assertEquals(false, HttpClientHelper.requiresProxy("www.foo.com"));
        assertEquals(true, HttpClientHelper.requiresProxy("search.com"));
        assertEquals(true, HttpClientHelper.requiresProxy("11.11.11.111"));
        assertEquals(true, HttpClientHelper.requiresProxy("foo.com"));
        
        // No host bypasses proxy
        setHTTPSystemProperties(host, port, null, null, null);
        
        assertEquals(true, HttpClientHelper.requiresProxy("search.com"));
        assertEquals(true, HttpClientHelper.requiresProxy("127.0.0.1"));
        assertEquals(true, HttpClientHelper.requiresProxy("localhost"));
        
        // Any host bypasses proxy 
        setHTTPSystemProperties(host, port, null, null, "*");
        
        assertEquals(false, HttpClientHelper.requiresProxy("search.com"));
        assertEquals(false, HttpClientHelper.requiresProxy("127.0.0.1"));
    }
    
    private void setHTTPSystemProperties(String host, Integer port, String user, String password, String nonProxyHosts)
    {
        clearHTTPSystemProperties();
        if (host != null) { System.setProperty("http.proxyHost", host); }
        if (port != null) { System.setProperty("http.proxyPort", port.toString()); }
        if (user != null) { System.setProperty("http.proxyUser", user); }
        if (password != null) { System.setProperty("http.proxyPassword", password); }
        if (nonProxyHosts != null) { System.setProperty("http.nonProxyHosts", nonProxyHosts); }
    }
    
    private void setHTTPSSystemProperties(String host, Integer port, String user, String password, String nonProxyHosts)
    {
        clearHTTPSystemProperties();
        if (host != null) { System.setProperty("https.proxyHost", host); }
        if (port != null) { System.setProperty("https.proxyPort", port.toString()); }
        if (user != null) { System.setProperty("https.proxyUser", user); }
        if (password != null) { System.setProperty("https.proxyPassword", password); }
        if (nonProxyHosts != null) { System.setProperty("http.nonProxyHosts", nonProxyHosts); }
    }
    
    private void clearHTTPSystemProperties()
    {
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("http.proxyUser");
        System.clearProperty("http.proxyPassword");
        
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("https.proxyUser");
        System.clearProperty("https.proxyPassword");
        
        System.clearProperty("http.nonProxyHosts");
    }
    
}
