/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.encryption.AlfrescoKeyStore;
import org.alfresco.encryption.AlfrescoKeyStoreImpl;
import org.alfresco.encryption.EncryptionUtils;
import org.alfresco.encryption.KeyResourceLoader;
import org.alfresco.encryption.KeyStoreParameters;
import org.alfresco.encryption.ssl.AuthSSLProtocolSocketFactory;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;
import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.DefaultHttpParamsFactory;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A factory to create HttpClients and AlfrescoHttpClients based on the setting of the 'secureCommsType' property.
 * 
 * @since 4.0
 */
public class HttpClientFactory
{
    /**
     * Communication type for HttpClient:
     * - NONE is plain http
     * - SECRET is plain http with a shared secret via request header
     * - HTTPS is mTLS with client authentication (certificates are required)
     */
    public static enum SecureCommsType
    {
        HTTPS, NONE, SECRET;
        
        public static SecureCommsType getType(String type)
        {
            switch (type.toLowerCase())
            {
                case "https": return HTTPS;
                case "none": return NONE;
                case "secret": return SECRET;
                default: throw new IllegalArgumentException("Invalid communications type");
                    
            }
        }
    };

    private static final Log logger = LogFactory.getLog(HttpClientFactory.class);

    private SSLEncryptionParameters sslEncryptionParameters;
    private KeyResourceLoader keyResourceLoader;
    private SecureCommsType secureCommsType;

    // for md5 http client (no longer used but kept for now)
    private KeyStoreParameters keyStoreParameters;
    private MD5EncryptionParameters encryptionParameters;

    private String host;
    private int port;
    private int sslPort;
    
    private AlfrescoKeyStore sslKeyStore;
    private AlfrescoKeyStore sslTrustStore;
    private ProtocolSocketFactory sslSocketFactory;

    private int maxTotalConnections = 40;

    private int maxHostConnections = 40;
    
    private Integer socketTimeout = null;

    private int connectionTimeout = 0;
    
    // Shared secret parameters
    private String sharedSecret;
    private String sharedSecretHeader = DEFAULT_SHAREDSECRET_HEADER;

    // Default name for HTTP Request Header when using shared secret communication
    public static final String DEFAULT_SHAREDSECRET_HEADER = "X-Alfresco-Search-Secret";
    
    public HttpClientFactory()
    {
    }
    
    /**
     * Default constructor for legacy subsystems.
     */
    public HttpClientFactory(SecureCommsType secureCommsType, SSLEncryptionParameters sslEncryptionParameters,
                KeyResourceLoader keyResourceLoader, KeyStoreParameters keyStoreParameters,
                MD5EncryptionParameters encryptionParameters, String host, int port, int sslPort,
                int maxTotalConnections, int maxHostConnections, int socketTimeout)
    {
        this.secureCommsType = secureCommsType;
        this.sslEncryptionParameters = sslEncryptionParameters;
        this.keyResourceLoader = keyResourceLoader;
        this.keyStoreParameters = keyStoreParameters;
        this.encryptionParameters = encryptionParameters;
        this.host = host;
        this.port = port;
        this.sslPort = sslPort;
        this.maxTotalConnections = maxTotalConnections;
        this.maxHostConnections = maxHostConnections;
        this.socketTimeout = socketTimeout;
        init();
    }

    /**
     * Recommended constructor for subsystems supporting Shared Secret communication.
     * This constructor supports Shared Secret ("secret") communication method additionally to the legacy ones: "none" and "https".
     */
    public HttpClientFactory(SecureCommsType secureCommsType, SSLEncryptionParameters sslEncryptionParameters,
                KeyResourceLoader keyResourceLoader, KeyStoreParameters keyStoreParameters,
                MD5EncryptionParameters encryptionParameters, String sharedSecret, String sharedSecretHeader, 
                String host, int port, int sslPort, int maxTotalConnections, int maxHostConnections, int socketTimeout)
    {
        this(secureCommsType, sslEncryptionParameters, keyResourceLoader, keyStoreParameters, encryptionParameters,
                    host, port, sslPort, maxTotalConnections, maxHostConnections, socketTimeout);
        this.sharedSecret = sharedSecret;
        this.sharedSecretHeader = sharedSecretHeader;
    }

    public void init()
    {
        this.sslKeyStore = new AlfrescoKeyStoreImpl(sslEncryptionParameters.getKeyStoreParameters(),  keyResourceLoader);
        this.sslTrustStore = new AlfrescoKeyStoreImpl(sslEncryptionParameters.getTrustStoreParameters(), keyResourceLoader);
        this.sslSocketFactory = new AuthSSLProtocolSocketFactory(sslKeyStore, sslTrustStore, keyResourceLoader);
        
        // Setup the Apache httpclient library to use our concurrent HttpParams factory
        DefaultHttpParams.setHttpParamsFactory(new NonBlockingHttpParamsFactory());
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getHost()
    {
        return host;
    }
    
    public void setPort(int port)
    {
        this.port = port;
    }
    
    public int getPort()
    {
        return port;
    }

    public void setSslPort(int sslPort)
    {
        this.sslPort = sslPort;
    }

    public boolean isSSL()
    {
        return secureCommsType == SecureCommsType.HTTPS;
    }

    public void setSecureCommsType(String type)
    {
        try
        {
            this.secureCommsType = SecureCommsType.getType(type);
        }
        catch(IllegalArgumentException e)
        {
            throw new AlfrescoRuntimeException("", e);
        }
    }
    
    public void setSSLEncryptionParameters(SSLEncryptionParameters sslEncryptionParameters)
    {
        this.sslEncryptionParameters = sslEncryptionParameters;
    }

    public void setKeyStoreParameters(KeyStoreParameters keyStoreParameters)
    {
        this.keyStoreParameters = keyStoreParameters;
    }

    public void setEncryptionParameters(MD5EncryptionParameters encryptionParameters)
    {
        this.encryptionParameters = encryptionParameters;
    }

    public void setKeyResourceLoader(KeyResourceLoader keyResourceLoader)
    {
        this.keyResourceLoader = keyResourceLoader;
    }
    
    /**
     * @return the maxTotalConnections
     */
    public int getMaxTotalConnections()
    {
        return maxTotalConnections;
    }

    /**
     * @param maxTotalConnections the maxTotalConnections to set
     */
    public void setMaxTotalConnections(int maxTotalConnections)
    {
        this.maxTotalConnections = maxTotalConnections;
    }

    /**
     * @return the maxHostConnections
     */
    public int getMaxHostConnections()
    {
        return maxHostConnections;
    }

    /**
     * @param maxHostConnections the maxHostConnections to set
     */
    public void setMaxHostConnections(int maxHostConnections)
    {
        this.maxHostConnections = maxHostConnections;
    }

    /**
     * Sets the default socket timeout (<tt>SO_TIMEOUT</tt>) in milliseconds which is the
     * timeout for waiting for data. A timeout value of zero is interpreted as an infinite
     * timeout.
     *
     * @param socketTimeout Timeout in milliseconds
     */
    public void setSocketTimeout(Integer socketTimeout)
    {
        this.socketTimeout = socketTimeout;
    }

    /**
     * Attempts to connect to a server will timeout after this period (millis).
     * Default is zero (the timeout is not used).
     * 
     * @param connectionTimeout time in millis.
     */
    public void setConnectionTimeout(int connectionTimeout)
    {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Shared secret used for SECRET communication
     * @param secret shared secret word
     */
    public void setSharedSecret(String sharedSecret)
    {
        this.sharedSecret = sharedSecret;
    }
    
    /**
     * @return Shared secret used for SECRET communication
     */
    public String getSharedSecret()
    {
        return sharedSecret;
    }
    
    /**
     * HTTP Request header used for SECRET communication
     * @param sharedSecretHeader HTTP Request header
     */
    public void setSharedSecretHeader(String sharedSecretHeader)
    {
        this.sharedSecretHeader = sharedSecretHeader;
    }
    
    /**
     * @return HTTP Request header used for SECRET communication
     */
    public String getSharedSecretHeader()
    {
        return sharedSecretHeader;
    }

    protected RequestHeadersHttpClient constructHttpClient()
    {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        RequestHeadersHttpClient httpClient = new RequestHeadersHttpClient(connectionManager);
        HttpClientParams params = httpClient.getParams();
        params.setBooleanParameter(HttpConnectionParams.TCP_NODELAY, true);
        params.setBooleanParameter(HttpConnectionParams.STALE_CONNECTION_CHECK, true);
        if (socketTimeout != null) 
        {
            params.setSoTimeout(socketTimeout);
        }
        HttpConnectionManagerParams connectionManagerParams = httpClient.getHttpConnectionManager().getParams();
        connectionManagerParams.setMaxTotalConnections(maxTotalConnections);
        connectionManagerParams.setDefaultMaxConnectionsPerHost(maxHostConnections);
        connectionManagerParams.setConnectionTimeout(connectionTimeout);

        return httpClient;
    }
    
    protected RequestHeadersHttpClient getHttpsClient()
    {
       return getHttpsClient(host, sslPort);
    }
    
    protected RequestHeadersHttpClient getHttpsClient(String httpsHost, int httpsPort)
    {
        // Configure a custom SSL socket factory that will enforce mutual authentication
        RequestHeadersHttpClient httpClient = constructHttpClient();
        // Default port is 443 for the HostFactory, when including customised port (like 8983) the port name is skipped from "getHostURL" string
        HttpHostFactory hostFactory = new HttpHostFactory(new Protocol("https", sslSocketFactory, HttpsURL.DEFAULT_PORT));
        httpClient.setHostConfiguration(new HostConfigurationWithHostFactory(hostFactory));
        httpClient.getHostConfiguration().setHost(httpsHost, httpsPort, "https");
        return httpClient;
    }

    protected RequestHeadersHttpClient getDefaultHttpClient()
    {
        return getDefaultHttpClient(host, port);
    }
    
    protected RequestHeadersHttpClient getDefaultHttpClient(String httpHost, int httpPort)
    {
        RequestHeadersHttpClient httpClient = constructHttpClient();
        httpClient.getHostConfiguration().setHost(httpHost, httpPort);
        return httpClient;
    }
       
    /**
     * Build HTTP Client using default headers
     * @return RequestHeadersHttpClient including default header for shared secret method
     */
    protected RequestHeadersHttpClient constructSharedSecretHttpClient()
    {
        RequestHeadersHttpClient client = constructHttpClient();
        client.setDefaultHeaders(Map.of(sharedSecretHeader, sharedSecret));
        return client;
    }
    
    protected RequestHeadersHttpClient getSharedSecretHttpClient()
    {
        return getSharedSecretHttpClient(host, port);
    }
    
    protected RequestHeadersHttpClient getSharedSecretHttpClient(String httpHost, int httpPort)
    {
        RequestHeadersHttpClient httpClient = constructSharedSecretHttpClient();
        httpClient.getHostConfiguration().setHost(httpHost, httpPort);
        return httpClient;       
    }
    
    protected AlfrescoHttpClient getAlfrescoHttpsClient()
    {
        return new HttpsClient(getHttpsClient());
    }

    protected AlfrescoHttpClient getAlfrescoHttpClient()
    {
        return new DefaultHttpClient(getDefaultHttpClient());
    }
    
    protected AlfrescoHttpClient getAlfrescoSharedSecretClient()
    {
        return new DefaultHttpClient(getSharedSecretHttpClient());
    }
    
    protected HttpClient getMD5HttpClient(String host, int port)
    {
        HttpClient httpClient = constructHttpClient();
        httpClient.getHostConfiguration().setHost(host, port);
        return httpClient;
    }
    
    
    public AlfrescoHttpClient getRepoClient(String host, int port)
    {
        switch (secureCommsType)
        {
            case HTTPS: return getAlfrescoHttpsClient();
            case NONE: return getAlfrescoHttpClient();
            case SECRET: return getAlfrescoSharedSecretClient();
            default: throw new AlfrescoRuntimeException("Invalid Solr secure communications type configured in [solr|alfresco].secureComms, should be 'ssl', 'none' or 'secret'");
        }
    }
 
    public RequestHeadersHttpClient getHttpClient()
    {       
        switch (secureCommsType)
        {
            case HTTPS: return getHttpsClient();
            case NONE: return getDefaultHttpClient();
            case SECRET: return getSharedSecretHttpClient();
            default: throw new AlfrescoRuntimeException("Invalid Solr secure communications type configured in [solr|alfresco].secureComms, should be 'ssl', 'none' or 'secret'");
        }
    }
    
    public RequestHeadersHttpClient getHttpClient(String host, int port)
    {
        switch (secureCommsType)
        {
            case HTTPS: return getHttpsClient(host, port);
            case NONE: return getDefaultHttpClient(host, port);
            case SECRET: return getSharedSecretHttpClient(host, port);
            default: throw new AlfrescoRuntimeException("Invalid Solr secure communications type configured in [solr|alfresco].secureComms, should be 'ssl', 'none' or 'secret'");
        }
    }
    
    /**
     * A secure client connection to the repository.
     * 
     * @since 4.0
     *
     */
    class HttpsClient extends AbstractHttpClient
    {
        public HttpsClient(HttpClient httpClient)
        {
            super(httpClient);
        }

        /**
         * Send Request to the repository
         */
        public Response sendRequest(Request req) throws AuthenticationException, IOException
        {
            HttpMethod method = super.sendRemoteRequest(req);
            return new HttpMethodResponse(method);
        }
    }
    
    /**
     * Simple HTTP client to connect to the Alfresco server. Simply wraps a HttpClient.
     * 
     * @since 4.0
     */
    class DefaultHttpClient extends AbstractHttpClient
    {        
        public DefaultHttpClient(HttpClient httpClient)
        {
            super(httpClient);
        }

        /**
         * Send Request to the repository
         */
        public Response sendRequest(Request req) throws AuthenticationException, IOException
        {
            HttpMethod method = super.sendRemoteRequest(req);
            return new HttpMethodResponse(method);
        }
    }
    

    
    static class SecureHttpMethodResponse extends HttpMethodResponse
    {
        protected HostConfiguration hostConfig;
        protected EncryptionUtils encryptionUtils;
        // Need to get as a byte array because we need to read the request twice, once for authentication
        // and again by the web service.
        protected byte[] decryptedBody;

        public SecureHttpMethodResponse(HttpMethod method, HostConfiguration hostConfig, 
                EncryptionUtils encryptionUtils) throws AuthenticationException, IOException
        {
            super(method);
            this.hostConfig = hostConfig;
            this.encryptionUtils = encryptionUtils;

            if(method.getStatusCode() == HttpStatus.SC_OK)
            {
                this.decryptedBody = encryptionUtils.decryptResponseBody(method);
                // authenticate the response
                if(!authenticate())
                {
                    throw new AuthenticationException(method);
                }
            }
        }
        
        protected boolean authenticate() throws IOException
        {
            return encryptionUtils.authenticateResponse(method, hostConfig.getHost(), decryptedBody);
        }
        
        public InputStream getContentAsStream() throws IOException
        {
            if(decryptedBody != null)
            {
                return new ByteArrayInputStream(decryptedBody);
            }
            else
            {
                return null;
            }
        }
    }

    private static class HttpHostFactory
    {
        private Map<String, Protocol> protocols;

        public HttpHostFactory(Protocol httpsProtocol)
        {
            protocols = new HashMap<String, Protocol>(2);
            protocols.put("https", httpsProtocol);
        }
 
        /** Get a host for the given parameters. This method need not be thread-safe. */
        public HttpHost getHost(String host, int port, String scheme)
        {
            if(scheme == null)
            {
                scheme = "http";
            }
            Protocol protocol = protocols.get(scheme);
            if(protocol == null)
            {
                protocol = Protocol.getProtocol("http");
                if(protocol == null)
                {
                    throw new IllegalArgumentException("Unrecognised scheme parameter");
                }
            }

            return new HttpHost(host, port, protocol);
        }
    }
    
    private static class HostConfigurationWithHostFactory extends HostConfiguration
    {
        private final HttpHostFactory factory;

        public HostConfigurationWithHostFactory(HttpHostFactory factory)
        {
            this.factory = factory;
        }

        public synchronized void setHost(String host, int port, String scheme)
        {
            setHost(factory.getHost(host, port, scheme));
        }

        public synchronized void setHost(String host, int port)
        {
            setHost(factory.getHost(host, port, "http"));
        }
        
        @SuppressWarnings("unused")
        public synchronized void setHost(URI uri)
        {
            try {
                setHost(uri.getHost(), uri.getPort(), uri.getScheme());
            } catch(URIException e) {
                throw new IllegalArgumentException(e.toString());
            }
        }
    }

    /**
     * An extension of the DefaultHttpParamsFactory that uses a RRW lock pattern rather than
     * full synchronization around the parameter CRUD - to avoid locking on many reads. 
     * 
     * @author Kevin Roast
     */
    public static class NonBlockingHttpParamsFactory extends DefaultHttpParamsFactory
    {
        private volatile HttpParams httpParams;
        
        /* (non-Javadoc)
         * @see org.apache.commons.httpclient.params.DefaultHttpParamsFactory#getDefaultParams()
         */
        @Override
        public HttpParams getDefaultParams()
        {
            if (httpParams == null)
            {
                synchronized (this)
                {
                    if (httpParams == null)
                    {
                        httpParams = createParams();
                    }
                }
            }
            
            return httpParams;
        }
        
        /**
         * NOTE: This is a copy of the code in {@link DefaultHttpParamsFactory}
         *       Unfortunately this is required because although the factory pattern allows the 
         *       override of the default param creation, it does not allow the class of the actual
         *       HttpParam implementation to be changed.
         */
        @Override
        protected HttpParams createParams()
        {
            HttpClientParams params = new NonBlockingHttpParams(null);
            
            params.setParameter(HttpMethodParams.USER_AGENT, "Spring Surf via Apache HttpClient/3.1");
            params.setVersion(HttpVersion.HTTP_1_1);
            params.setConnectionManagerClass(SimpleHttpConnectionManager.class);
            params.setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            params.setHttpElementCharset("US-ASCII");
            params.setContentCharset("ISO-8859-1");
            params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
            
            List<String> datePatterns = Arrays.asList(
                    new String[] {
                            DateUtil.PATTERN_RFC1123,
                            DateUtil.PATTERN_RFC1036,
                            DateUtil.PATTERN_ASCTIME,
                            "EEE, dd-MMM-yyyy HH:mm:ss z",
                            "EEE, dd-MMM-yyyy HH-mm-ss z",
                            "EEE, dd MMM yy HH:mm:ss z",
                            "EEE dd-MMM-yyyy HH:mm:ss z",
                            "EEE dd MMM yyyy HH:mm:ss z",
                            "EEE dd-MMM-yyyy HH-mm-ss z",
                            "EEE dd-MMM-yy HH:mm:ss z",
                            "EEE dd MMM yy HH:mm:ss z",
                            "EEE,dd-MMM-yy HH:mm:ss z",
                            "EEE,dd-MMM-yyyy HH:mm:ss z",
                            "EEE, dd-MM-yyyy HH:mm:ss z",                
                    }
            );
            params.setParameter(HttpMethodParams.DATE_PATTERNS, datePatterns);
            
            String agent = null;
            try
            {
                agent = System.getProperty("httpclient.useragent");
            }
            catch (SecurityException ignore)
            {
            }
            if (agent != null)
            {
                params.setParameter(HttpMethodParams.USER_AGENT, agent);
            }
            
            String preemptiveDefault = null;
            try
            {
                preemptiveDefault = System.getProperty("httpclient.authentication.preemptive");
            }
            catch (SecurityException ignore)
            {
            }
            if (preemptiveDefault != null)
            {
                preemptiveDefault = preemptiveDefault.trim().toLowerCase();
                if (preemptiveDefault.equals("true"))
                {
                    params.setParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, Boolean.TRUE);
                }
                else if (preemptiveDefault.equals("false"))
                {
                    params.setParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, Boolean.FALSE);
                }
            }
            
            String defaultCookiePolicy = null;
            try
            {
                defaultCookiePolicy = System.getProperty("apache.commons.httpclient.cookiespec");
            }
            catch (SecurityException ignore)
            {
            }
            if (defaultCookiePolicy != null)
            {
                if ("COMPATIBILITY".equalsIgnoreCase(defaultCookiePolicy))
                {
                    params.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
                }
                else if ("NETSCAPE_DRAFT".equalsIgnoreCase(defaultCookiePolicy))
                {
                    params.setCookiePolicy(CookiePolicy.NETSCAPE);
                }
                else if ("RFC2109".equalsIgnoreCase(defaultCookiePolicy))
                {
                    params.setCookiePolicy(CookiePolicy.RFC_2109);
                }
            }
            
            return params;
        }
    }
    
    /**
     * @author Kevin Roast
     */
    public static class NonBlockingHttpParams extends HttpClientParams
    {
        private HashMap<String, Object> parameters = new HashMap<String, Object>(8);
        private ReadWriteLock paramLock = new ReentrantReadWriteLock();
        
        public NonBlockingHttpParams()
        {
            super();
        }
        
        public NonBlockingHttpParams(HttpParams defaults)
        {
            super(defaults);
        }
        
        @Override
        public Object getParameter(final String name)
        {
            // See if the parameter has been explicitly defined
            Object param = null;
            paramLock.readLock().lock();
            try
            {
                param = this.parameters.get(name);
            }
            finally
            {
                paramLock.readLock().unlock();
            }
            if (param == null)
            {
                // If not, see if defaults are available
                HttpParams defaults = getDefaults();
                if (defaults != null)
                {
                    // Return default parameter value
                    param = defaults.getParameter(name);
                }
            }
            return param;
        }
        
        @Override
        public void setParameter(final String name, final Object value)
        {
            paramLock.writeLock().lock();
            try
            {
                this.parameters.put(name, value);
            }
            finally
            {
                paramLock.writeLock().unlock();
            }
        }
        
        @Override
        public boolean isParameterSetLocally(final String name)
        {
            paramLock.readLock().lock();
            try
            {
                return (this.parameters.get(name) != null);
            }
            finally
            {
                paramLock.readLock().unlock();
            }
        }
        
        @Override
        public void clear()
        {
            paramLock.writeLock().lock();
            try
            {
                this.parameters.clear();
            }
            finally
            {
                paramLock.writeLock().unlock();
            }
        }
        
        @Override
        public Object clone() throws CloneNotSupportedException
        {
            NonBlockingHttpParams clone = (NonBlockingHttpParams)super.clone();
            paramLock.readLock().lock();
            try
            {
                clone.parameters = (HashMap) this.parameters.clone();
            }
            finally
            {
                paramLock.readLock().unlock();
            }
            clone.setDefaults(getDefaults());
            return clone;
        }
    }
}
