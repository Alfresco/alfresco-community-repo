/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication.identityservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.management.subsystems.AbstractChainedSubsystemTest;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.management.subsystems.DefaultChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceConfig;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.rotation.HardcodedPublicKeyLocator;
import org.keycloak.common.util.Base64;
import org.keycloak.common.util.Time;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.representations.AccessToken;
import org.springframework.context.ApplicationContext;

/**
 * Tests the Identity Service based authentication subsystem.
 * 
 * @author Gavin Cornwell
 */
public class IdentityServiceRemoteUserMapperTest extends AbstractChainedSubsystemTest
{
    private static final String REMOTE_USER_MAPPER_BEAN_NAME = "remoteUserMapper";
    private static final String DEPLOYMENT_BEAN_NAME = "identityServiceDeployment";
    private static final String CONFIG_BEAN_NAME = "identityServiceConfig";
    
    private static final String TEST_USER_USERNAME = "testuser";
    private static final String TEST_USER_EMAIL = "testuser@mail.com";
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BASIC_PREFIX = "Basic ";
    
    private static final String CONFIG_SILENT_ERRORS = "identity-service.authentication.validation.failure.silent";
    
    private static final String PASSWORD_GRANT_RESPONSE = "{" +
            "\"access_token\": \"%s\"," +
            "\"expires_in\": 300," +
            "\"refresh_expires_in\": 1800," +
            "\"refresh_token\": \"%s\"," +
            "\"token_type\": \"bearer\"," +
            "\"not-before-policy\": 0," +
            "\"session_state\": \"71c2c5ba-9c98-49fc-882f-dedcf80ee1b5\"}";
    
    ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    DefaultChildApplicationContextManager childApplicationContextManager;
    ChildApplicationContextFactory childApplicationContextFactory;
    
    private KeyPair keyPair;
    private IdentityServiceConfig identityServiceConfig;

    @Override
    protected void setUp() throws Exception
    {
        // switch authentication to use token auth
        childApplicationContextManager = (DefaultChildApplicationContextManager) ctx.getBean("Authentication");
        childApplicationContextManager.stop();
        childApplicationContextManager.setProperty("chain", "identity-service1:identity-service");
        childApplicationContextFactory = getChildApplicationContextFactory(childApplicationContextManager, "identity-service1");
        
        // generate keys for test
        this.keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        
        // hardcode the realm public key in the deployment bean to stop it fetching keys
        applyHardcodedPublicKey(this.keyPair.getPublic());
        
        // extract config
        this.identityServiceConfig = (IdentityServiceConfig)childApplicationContextFactory.
                    getApplicationContext().getBean(CONFIG_BEAN_NAME);
    }

    @Override
    protected void tearDown() throws Exception
    {
        childApplicationContextManager.destroy();
        childApplicationContextManager = null;
        childApplicationContextFactory = null;
    }

    public void testKeycloakConfig() throws Exception
    {
        //Get the host of the IDS test server
        String ip = "localhost";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if(Pattern.matches("([0-9]{1,3}\\.){3}[0-9]{1,3}", addr.getHostAddress())){
                        ip = addr.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        // check string overrides
        assertEquals("identity-service.auth-server-url", "http://"+ip+":8999/auth",
                    this.identityServiceConfig.getAuthServerUrl());
        
        assertEquals("identity-service.realm", "alfresco",
                    this.identityServiceConfig.getRealm());

        assertEquals("identity-service.realm-public-key",
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvWLQxipXNe6cLnVPGy7l" +
                    "BgyR51bDiK7Jso8Rmh2TB+bmO4fNaMY1ETsxECSM0f6NTV0QHks9+gBe+pB6JNeM" +
                    "uPmaE/M/MsE9KUif9L2ChFq3zor6s2foFv2DTiTkij+1aQF9fuIjDNH4FC6L252W" +
                    "ydZzh+f73Xuy5evdPj+wrPYqWyP7sKd+4Q9EIILWAuTDvKEjwyZmIyfM/nUn6ltD" +
                    "P6W8xMP0PoEJNAAp79anz2jk2HP2PvC2qdjVsphdTk3JG5qQMB0WJUh4Kjgabd4j" +
                    "QJ77U8gTRswKgNHRRPWhruiIcmmkP+zI0ozNW6rxH3PF4L7M9rXmfcmUcBcKf+Yx" +
                    "jwIDAQAB",
                    this.identityServiceConfig.getRealmKey());
        
        assertEquals("identity-service.ssl-required", "external", 
                    this.identityServiceConfig.getSslRequired());
        
        assertEquals("identity-service.resource", "test", 
                    this.identityServiceConfig.getResource());
        
        assertEquals("identity-service.cors-allowed-headers", "Authorization", 
                    this.identityServiceConfig.getCorsAllowedHeaders());
        
        assertEquals("identity-service.cors-allowed-methods", "POST, PUT, DELETE, GET", 
                    this.identityServiceConfig.getCorsAllowedMethods());
        
        assertEquals("identity-service.cors-exposed-headers", "WWW-Authenticate, My-custom-exposed-Header", 
                    this.identityServiceConfig.getCorsExposedHeaders());
        
        assertEquals("identity-service.truststore", 
                    "classpath:/alfresco/subsystems/identityServiceAuthentication/keystore.jks", 
                    this.identityServiceConfig.getTruststore());
        
        assertEquals("identity-service.truststore-password", "password", 
                    this.identityServiceConfig.getTruststorePassword());
        
        assertEquals("identity-service.client-keystore", 
                    "classpath:/alfresco/subsystems/identityServiceAuthentication/keystore.jks", 
                    this.identityServiceConfig.getClientKeystore());
        
        assertEquals("identity-service.client-keystore-password", "password", 
                    this.identityServiceConfig.getClientKeystorePassword());
        
        assertEquals("identity-service.client-key-password", "password", 
                    this.identityServiceConfig.getClientKeyPassword());
        
        assertEquals("identity-service.token-store", "SESSION", 
                    this.identityServiceConfig.getTokenStore());
        
        assertEquals("identity-service.principal-attribute", "preferred_username", 
                    this.identityServiceConfig.getPrincipalAttribute());
        
        // check number overrides
        assertEquals("identity-service.confidential-port", 100, 
                    this.identityServiceConfig.getConfidentialPort());
        
        assertEquals("identity-service.cors-max-age", 1000, 
                    this.identityServiceConfig.getCorsMaxAge());
        
        assertEquals("identity-service.connection-pool-size", 5, 
                    this.identityServiceConfig.getConnectionPoolSize());
        
        assertEquals("identity-service.register-node-period", 50, 
                    this.identityServiceConfig.getRegisterNodePeriod());
        
        assertEquals("identity-service.token-minimum-time-to-live", 10, 
                    this.identityServiceConfig.getTokenMinimumTimeToLive());
        
        assertEquals("identity-service.min-time-between-jwks-requests", 60, 
                    this.identityServiceConfig.getMinTimeBetweenJwksRequests());
        
        assertEquals("identity-service.public-key-cache-ttl", 3600, 
                    this.identityServiceConfig.getPublicKeyCacheTtl());

        assertEquals("identity-service.client-connection-timeout", 3000,
                this.identityServiceConfig.getClientConnectionTimeout());

        assertEquals("identity-service.client-socket-timeout", 1000,
                this.identityServiceConfig.getClientSocketTimeout());

        // check boolean overrides
        assertFalse("identity-service.public-client", 
                    this.identityServiceConfig.isPublicClient());
        
        assertTrue("identity-service.use-resource-role-mappings", 
                    this.identityServiceConfig.isUseResourceRoleMappings());
        
        assertTrue("identity-service.enable-cors", 
                    this.identityServiceConfig.isCors());
        
        assertTrue("identity-service.expose-token", 
                    this.identityServiceConfig.isExposeToken());
        
        assertTrue("identity-service.bearer-only", 
                    this.identityServiceConfig.isBearerOnly());
        
        assertTrue("identity-service.autodetect-bearer-only", 
                    this.identityServiceConfig.isAutodetectBearerOnly());
        
        assertTrue("identity-service.enable-basic-auth", 
                    this.identityServiceConfig.isEnableBasicAuth());
        
        assertTrue("identity-service.allow-any-hostname", 
                    this.identityServiceConfig.isAllowAnyHostname());
        
        assertTrue("identity-service.disable-trust-manager", 
                    this.identityServiceConfig.isDisableTrustManager());
        
        assertTrue("identity-service.always-refresh-token", 
                    this.identityServiceConfig.isAlwaysRefreshToken());
        
        assertTrue("identity-service.register-node-at-startup", 
                    this.identityServiceConfig.isRegisterNodeAtStartup());
        
        assertTrue("identity-service.enable-pkce", 
                    this.identityServiceConfig.isPkce());
        
        assertTrue("identity-service.ignore-oauth-query-parameter", 
                    this.identityServiceConfig.isIgnoreOAuthQueryParameter());
        
        assertTrue("identity-service.turn-off-change-session-id-on-login", 
                    this.identityServiceConfig.getTurnOffChangeSessionIdOnLogin());

        // check credentials overrides
        Map<String, Object> credentials = this.identityServiceConfig.getCredentials();
        assertNotNull("Expected a credentials map", credentials);
        assertFalse("Expected to retrieve a populated credentials map", credentials.isEmpty());
        assertEquals("identity-service.credentials.secret", "11111", credentials.get("secret"));
        assertEquals("identity-service.credentials.provider", "secret", credentials.get("provider"));
    }
    
    public void testValidToken() throws Exception
    {
        // create token
        String jwt = generateToken(false);
        
        // create mock request object
        HttpServletRequest mockRequest = createMockTokenRequest(jwt);
        
        // validate correct user was found
        assertEquals(TEST_USER_USERNAME, ((RemoteUserMapper) childApplicationContextFactory.getApplicationContext().getBean(
              REMOTE_USER_MAPPER_BEAN_NAME)).getRemoteUser(mockRequest));
    }
    
    public void testWrongPublicKey() throws Exception
    {
        // generate and apply an incorrect public key
        childApplicationContextFactory.stop();
        applyHardcodedPublicKey(KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic());
        
        // create token
        String jwt = generateToken(false);
        
        // create mock request object
        HttpServletRequest mockRequest = createMockTokenRequest(jwt);
        
        // ensure null is returned if the public key is wrong
        assertNull(((RemoteUserMapper) childApplicationContextFactory.getApplicationContext().getBean(
              REMOTE_USER_MAPPER_BEAN_NAME)).getRemoteUser(mockRequest));
    }
    
    public void testWrongPublicKeyWithError() throws Exception
    {
        // generate and apply an incorrect public key
        childApplicationContextFactory.stop();
        childApplicationContextFactory.setProperty(CONFIG_SILENT_ERRORS, "false");
        applyHardcodedPublicKey(KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic());
        
        // create token
        String jwt = generateToken(false);
        
        // create mock request object
        HttpServletRequest mockRequest = createMockTokenRequest(jwt);
        
        // ensure user mapper falls through instead of throwing an exception
        String user = ((RemoteUserMapper)childApplicationContextFactory.getApplicationContext().getBean(
                REMOTE_USER_MAPPER_BEAN_NAME)).getRemoteUser(mockRequest);
        assertEquals("Returned user should be null when wrong public key is used.",  null, user);
    }
    
    public void testInvalidJwt() throws Exception
    {
        // create mock request object
        HttpServletRequest mockRequest = createMockTokenRequest("thisisnotaJWT");
        
        // ensure null is returned if the JWT is invalid
        assertNull(((RemoteUserMapper) childApplicationContextFactory.getApplicationContext().getBean(
              REMOTE_USER_MAPPER_BEAN_NAME)).getRemoteUser(mockRequest));
    }
    
    public void testMissingToken() throws Exception
    {
        // create mock request object
        HttpServletRequest mockRequest = createMockTokenRequest("");
        
        // ensure null is returned if the token is missing
        assertNull(((RemoteUserMapper) childApplicationContextFactory.getApplicationContext().getBean(
              REMOTE_USER_MAPPER_BEAN_NAME)).getRemoteUser(mockRequest));
    }
    
    public void testExpiredToken() throws Exception
    {
        // create token
        String jwt = generateToken(true);
        
        // create mock request object
        HttpServletRequest mockRequest = createMockTokenRequest(jwt);
        
        // ensure null is returned if the token has expired
        assertNull(((RemoteUserMapper) childApplicationContextFactory.getApplicationContext().getBean(
              REMOTE_USER_MAPPER_BEAN_NAME)).getRemoteUser(mockRequest));
    }
    
    public void testExpiredTokenWithError() throws Exception
    {
        // turn on validation failure reporting
        childApplicationContextFactory.stop();
        childApplicationContextFactory.setProperty(CONFIG_SILENT_ERRORS, "false");
        applyHardcodedPublicKey(this.keyPair.getPublic());
        
        // create token
        String jwt = generateToken(true);
        
        // create mock request object
        HttpServletRequest mockRequest = createMockTokenRequest(jwt);
        
        // ensure an exception is thrown with correct description
        String user = ((RemoteUserMapper)childApplicationContextFactory.getApplicationContext().getBean(
                        REMOTE_USER_MAPPER_BEAN_NAME)).getRemoteUser(mockRequest);
        assertEquals("Returned user should be null when the token is expired.", null, user);
    }
    
    public void testMissingHeader() throws Exception
    {        
        // create mock request object with no Authorization header
        HttpServletRequest mockRequest = createMockTokenRequest(null);
        
        // ensure null is returned if the header was missing
        assertNull(((RemoteUserMapper) childApplicationContextFactory.getApplicationContext().getBean(
              REMOTE_USER_MAPPER_BEAN_NAME)).getRemoteUser(mockRequest));
    }
    
    /**
     * Utility method for creating a mocked Servlet request with a token.
     * 
     * @param token The token to add to the Authorization header
     * @return The mocked request object
     */
    private HttpServletRequest createMockTokenRequest(String token)
    {
        // Mock a request with the token in the Authorization header (if supplied)
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        
        Vector<String> authHeaderValues = new Vector<>(1);
        if (token != null)
        {
            authHeaderValues.add(BEARER_PREFIX + token);
        }
        
        when(mockRequest.getHeaders(AUTHORIZATION_HEADER)).thenReturn(authHeaderValues.elements());
        
        return mockRequest;
    }
    
    /**
     * Utility method for creating a mocked Servlet request with basic auth.
     * 
     * @return The mocked request object
     */
    @SuppressWarnings("unchecked")
    private HttpServletRequest createMockBasicRequest()
    {
        // Mock a request with the token in the Authorization header (if supplied)
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        
        Vector<String> authHeaderValues = new Vector<>(1);
        String userPwd = TEST_USER_USERNAME + ":" + TEST_USER_USERNAME;
        authHeaderValues.add(BASIC_PREFIX + Base64.encodeBytes(userPwd.getBytes()));
        
        // NOTE: as getHeaders gets called twice provide two separate Enumeration objects so that
        // an empty result is not returned for the second invocation.
        when(mockRequest.getHeaders(AUTHORIZATION_HEADER)).thenReturn(authHeaderValues.elements(), 
                    authHeaderValues.elements());
        
        return mockRequest;
    }
    
    private HttpClient createMockHttpClient() throws Exception
    {
        // mock HttpClient object and set on keycloak deployment to avoid basic auth
        // attempting to get a token using HTTP POST
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        StatusLine mockStatusLine = mock(StatusLine.class);
        HttpEntity mockHttpEntity = mock(HttpEntity.class);
        
        // for the purpose of this test use the same token for access and refresh
        String token = generateToken(false);
        String jsonResponse = String.format(PASSWORD_GRANT_RESPONSE, token, token);
        ByteArrayInputStream jsonResponseStream = new ByteArrayInputStream(jsonResponse.getBytes());
        
        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);
        when(mockStatusLine.getStatusCode()).thenReturn(200);
        when(mockHttpEntity.getContent()).thenReturn(jsonResponseStream);
        
        return mockHttpClient;
    }
    
    /**
     * Utility method to create tokens for testing.
     * 
     * @param expired Determines whether to create an expired JWT
     * @return The string representation of the JWT
     */
    private String generateToken(boolean expired) throws Exception
    {
        String issuerUrl = this.identityServiceConfig.getAuthServerUrl() + "/realms/" + this.identityServiceConfig.getRealm();
        
        AccessToken token = new AccessToken();
        token.type("Bearer");
        token.id("1234");
        token.subject("abc123");
        token.issuer(issuerUrl);
        token.setPreferredUsername(TEST_USER_USERNAME);
        token.setEmail(TEST_USER_EMAIL);
        token.setGivenName("Joe");
        token.setFamilyName("Bloggs");
        
        if (expired)
        {
            token.expiration(Time.currentTime() - 60);
        }

        String jwt = new JWSBuilder()
                .jsonContent(token)
                .rsa256(keyPair.getPrivate());
        
        return jwt;
    }
    
    /**
     * Finds the keycloak deployment bean and applies a hardcoded public key locator using the 
     * provided public key.
     */
    private void applyHardcodedPublicKey(PublicKey publicKey)
    {
        KeycloakDeployment deployment  = (KeycloakDeployment)childApplicationContextFactory.getApplicationContext().
                    getBean(DEPLOYMENT_BEAN_NAME);
        HardcodedPublicKeyLocator publicKeyLocator = new HardcodedPublicKeyLocator(publicKey);
        deployment.setPublicKeyLocator(publicKeyLocator);
    }
}
