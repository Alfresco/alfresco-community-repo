package org.alfresco.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;

import org.alfresco.encryption.EncryptionUtils;
import org.alfresco.encryption.Encryptor;
import org.alfresco.encryption.KeyProvider;
import org.alfresco.encryption.KeyResourceLoader;
import org.alfresco.util.Pair;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple HTTP client to connect to the Alfresco server.
 * 
 * @since 4.0
 */
public class SecureHttpClient //extends AbstractHttpClient
{
//    private static final Log logger = LogFactory.getLog(SecureHttpClient.class);
//
//    private Encryptor encryptor;
//    private EncryptionUtils encryptionUtils;
//    private EncryptionService encryptionService;
//    private EncryptionParameters encryptionParameters;
//    
//    /**
//     * For testing purposes.
//     * 
//     * @param solrResourceLoader
//     * @param alfrescoHost
//     * @param alfrescoPort
//     * @param encryptionParameters
//     */
//    public SecureHttpClient(HttpClientFactory httpClientFactory, String host, int port, EncryptionService encryptionService)
//    {
//        super(httpClientFactory, host, port);
//        this.encryptionUtils = encryptionService.getEncryptionUtils();
//        this.encryptor = encryptionService.getEncryptor();
//        this.encryptionService = encryptionService;
//        this.encryptionParameters = encryptionService.getEncryptionParameters();
//    }
//    
//    public SecureHttpClient(HttpClientFactory httpClientFactory, KeyResourceLoader keyResourceLoader, String host, int port,
//            EncryptionParameters encryptionParameters)
//    {
//        super(httpClientFactory, host, port);
//        this.encryptionParameters = encryptionParameters;
//        this.encryptionService = new EncryptionService(alfrescoHost, alfrescoPort, keyResourceLoader, encryptionParameters);
//        this.encryptionUtils = encryptionService.getEncryptionUtils();
//        this.encryptor = encryptionService.getEncryptor();
//    }
//    
//    protected HttpMethod createMethod(Request req) throws IOException
//    {
//        byte[] message = null;
//        HttpMethod method = super.createMethod(req);
//
//        if(req.getMethod().equalsIgnoreCase("POST"))
//        {
//            message = req.getBody();
//            // encrypt body
//            Pair<byte[], AlgorithmParameters> encrypted = encryptor.encrypt(KeyProvider.ALIAS_SOLR, null, message);
//            encryptionUtils.setRequestAlgorithmParameters(method, encrypted.getSecond());
//
//            ByteArrayRequestEntity requestEntity = new ByteArrayRequestEntity(encrypted.getFirst(), "application/octet-stream");
//            ((PostMethod)method).setRequestEntity(requestEntity);
//        }
//
//        encryptionUtils.setRequestAuthentication(method, message);
//
//        return method;
//    }
//    
//    protected HttpMethod sendRemoteRequest(Request req) throws AuthenticationException, IOException
//    {
//        HttpMethod method = super.sendRemoteRequest(req);
//
//        // check that the request returned with an ok status
//        if(method.getStatusCode() == HttpStatus.SC_UNAUTHORIZED)
//        {
//            throw new AuthenticationException(method);
//        }
//        
//        return method;
//    }
//
//    /**
//     * Send Request to the repository
//     */
//    public Response sendRequest(Request req) throws AuthenticationException, IOException
//    {
//        HttpMethod method = super.sendRemoteRequest(req);
//        return new SecureHttpMethodResponse(method, httpClient.getHostConfiguration(), encryptionUtils);
//    }
//
//    public static class SecureHttpMethodResponse extends HttpMethodResponse
//    {
//        protected HostConfiguration hostConfig;
//        protected EncryptionUtils encryptionUtils;
//        // Need to get as a byte array because we need to read the request twice, once for authentication
//        // and again by the web service.
//        protected byte[] decryptedBody;
//
//        public SecureHttpMethodResponse(HttpMethod method, HostConfiguration hostConfig, 
//                EncryptionUtils encryptionUtils) throws AuthenticationException, IOException
//        {
//            super(method);
//            this.hostConfig = hostConfig;
//            this.encryptionUtils = encryptionUtils;
//
//            if(method.getStatusCode() == HttpStatus.SC_OK)
//            {
//                this.decryptedBody = encryptionUtils.decryptResponseBody(method);
//                // authenticate the response
//                if(!authenticate())
//                {
//                    throw new AuthenticationException(method);
//                }
//            }
//        }
//        
//        protected boolean authenticate() throws IOException
//        {
//            return encryptionUtils.authenticateResponse(method, hostConfig.getHost(), decryptedBody);
//        }
//        
//        public InputStream getContentAsStream() throws IOException
//        {
//            if(decryptedBody != null)
//            {
//                return new ByteArrayInputStream(decryptedBody);
//            }
//            else
//            {
//                return null;
//            }
//        }
//    }

}
