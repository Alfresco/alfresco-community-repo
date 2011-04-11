/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferProgress.Status;
import org.alfresco.util.json.ExceptionJsonSerializer;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit test for HttpClientTransmitterImpl
 *
 * @author Brian Remmington
 */
public class HttpClientTransmitterImplTest extends TestCase 
{
    
    
    private static final String TARGET_HOST = "my.testhost.com";
    private static final String HTTP_PROTOCOL = "HTTP";
    private static final String HTTPS_PROTOCOL = "HTTPS";
    private static final String TRANSFER_SERVICE_PATH = "/api/transfer";
    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;
    private static final String TARGET_USERNAME = "transferuser";
    private static final char[] TARGET_PASSWORD = "password".toCharArray();
    
    private HttpClientTransmitterImpl transmitter;
    private HttpClient mockedHttpClient;
    private TransferTargetImpl target;
    private MockableHttpMethodFactory mockedHttpMethodFactory;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.transmitter = new HttpClientTransmitterImpl();
        this.mockedHttpClient = mock(HttpClient.class);
        this.mockedHttpMethodFactory = new MockableHttpMethodFactory();
        transmitter.setHttpClient(mockedHttpClient);
        transmitter.setHttpMethodFactory(mockedHttpMethodFactory);

        this.target = new TransferTargetImpl();
        target.setEndpointHost(TARGET_HOST);
        target.setEndpointProtocol(HTTP_PROTOCOL);
        target.setEndpointPath(TRANSFER_SERVICE_PATH);
        target.setEndpointPort(HTTP_PORT);
        target.setUsername(TARGET_USERNAME);
        target.setPassword(TARGET_PASSWORD);
    }

    /**
     * Test create target.
     * 
     * @throws Exception
     */
    public void testSuccessfulVerifyTargetOverHttp() throws Exception
    {
        //Stub HttpClient so that executeMethod returns a 200 response
        when(mockedHttpClient.executeMethod(any(HostConfiguration.class), any(HttpMethod.class), 
                any(HttpState.class))).thenReturn(200);
        
        //Call verifyTarget
        transmitter.verifyTarget(target);
        
        ArgumentCaptor<HostConfiguration> hostConfig = ArgumentCaptor.forClass(HostConfiguration.class);
        ArgumentCaptor<HttpMethod> httpMethod = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpState> httpState = ArgumentCaptor.forClass(HttpState.class);
        
        verify(mockedHttpClient).executeMethod(hostConfig.capture(), httpMethod.capture(), httpState.capture());
        
        assertTrue("post method", httpMethod.getValue() instanceof PostMethod);
        assertEquals("host name", TARGET_HOST, hostConfig.getValue().getHost());
        assertEquals("port", HTTP_PORT, hostConfig.getValue().getPort());
        assertEquals("protocol", HTTP_PROTOCOL.toLowerCase(), 
                hostConfig.getValue().getProtocol().getScheme().toLowerCase());
        assertEquals("path", TRANSFER_SERVICE_PATH + "/test", httpMethod.getValue().getPath());
    }
    
    public void testSuccessfulVerifyTargetOverHttps() throws Exception
    {
        
        //Stub HttpClient so that executeMethod returns a 200 response
        when(mockedHttpClient.executeMethod(any(HostConfiguration.class), any(HttpMethod.class), 
                any(HttpState.class))).thenReturn(200);

        target.setEndpointProtocol(HTTPS_PROTOCOL);
        target.setEndpointPort(HTTPS_PORT);
        
        //Call verifyTarget
        transmitter.verifyTarget(target);
        
        ArgumentCaptor<HostConfiguration> hostConfig = ArgumentCaptor.forClass(HostConfiguration.class);
        ArgumentCaptor<HttpMethod> httpMethod = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpState> httpState = ArgumentCaptor.forClass(HttpState.class);
        
        verify(mockedHttpClient).executeMethod(hostConfig.capture(), httpMethod.capture(), httpState.capture());
        
        assertEquals("port", HTTPS_PORT, hostConfig.getValue().getPort());
        assertTrue("socket factory", 
                hostConfig.getValue().getProtocol().getSocketFactory() instanceof SecureProtocolSocketFactory);
        assertEquals("protocol", HTTPS_PROTOCOL.toLowerCase(), 
                hostConfig.getValue().getProtocol().getScheme().toLowerCase());
    }

    public void testHttpsVerifyTargetWithCustomSocketFactory() throws Exception
    {
        //Override the default SSL socket factory with our own custom one...
        CustomSocketFactory socketFactory = new CustomSocketFactory();
        transmitter.setHttpsSocketFactory(socketFactory);
        
        target.setEndpointProtocol(HTTPS_PROTOCOL);
        target.setEndpointPort(HTTPS_PORT);
        
        //Stub HttpClient so that executeMethod returns a 200 response
        when(mockedHttpClient.executeMethod(any(HostConfiguration.class), any(HttpMethod.class), 
                any(HttpState.class))).thenReturn(200);

        //Call verifyTarget
        transmitter.verifyTarget(target);
        
        ArgumentCaptor<HostConfiguration> hostConfig = ArgumentCaptor.forClass(HostConfiguration.class);
        ArgumentCaptor<HttpMethod> httpMethod = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpState> httpState = ArgumentCaptor.forClass(HttpState.class);
        
        verify(mockedHttpClient).executeMethod(hostConfig.capture(), httpMethod.capture(), httpState.capture());
        
        assertEquals("port", HTTPS_PORT, hostConfig.getValue().getPort());
        //test that the socket factory passed to HttpClient is our custom one (intentional use of '==')
        assertTrue("socket factory", hostConfig.getValue().getProtocol().getSocketFactory() == socketFactory);
        assertEquals("protocol", HTTPS_PROTOCOL.toLowerCase(), 
                hostConfig.getValue().getProtocol().getScheme().toLowerCase());
    }

    
    public void testVerifyTargetWithInvalidProtocol() throws Exception
    {
        target.setEndpointProtocol("invalidprotocol");
        try
        {
            transmitter.verifyTarget(target);
            fail("invalid protocol");
        }
        catch(TransferException ex)
        {
            //expected
        }
    }
    
    public void testUnauthorisedVerifyTarget() throws Exception
    {
        //Stub HttpClient so that executeMethod returns a 401 response
        when(mockedHttpClient.executeMethod(any(HostConfiguration.class), any(HttpMethod.class), 
                any(HttpState.class))).thenReturn(401);
        
        try
        {
            transmitter.verifyTarget(target);
        }
        catch (TransferException ex)
        {
            //expected
        }
    }
    
    public void testGetStatusErrorRehydration() throws Exception
    {
        final ExceptionJsonSerializer errorSerializer = new ExceptionJsonSerializer();
        final TransferException expectedException = new TransferException("my message id", new Object[] {"param1", "param2"}); 
        when(mockedHttpClient.executeMethod(any(HostConfiguration.class), any(HttpMethod.class), 
                any(HttpState.class))).thenReturn(200);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable
            {
                JSONObject progressObject = new JSONObject();
                progressObject.put("transferId", "mytransferid");
                progressObject.put("status", Status.ERROR);
                progressObject.put("currentPosition", 1);
                progressObject.put("endPosition", 10);
                JSONObject errorObject = errorSerializer.serialize(expectedException);
                progressObject.put("error", errorObject);
                return progressObject.toString();
            }
        }).when(mockedHttpMethodFactory.latestPostMethod).getResponseBodyAsString();
        
        Transfer transfer = new Transfer();
        transfer.setTransferId("mytransferid");
        transfer.setTransferTarget(target);
        TransferProgress progress = transmitter.getStatus(transfer);
        assertTrue(progress.getError() != null);
        assertEquals(expectedException.getClass(), progress.getError().getClass());
        TransferException receivedException = (TransferException)progress.getError();
        assertEquals(expectedException.getMsgId(), receivedException.getMsgId());
        assertTrue(Arrays.deepEquals(expectedException.getMsgParams(), receivedException.getMsgParams()));
    }
    
    public void testBeginFailure() throws Exception
    {
        final ExceptionJsonSerializer errorSerializer = new ExceptionJsonSerializer();
        final TransferException expectedException = new TransferException("my message id", new Object[] {"param1", "param2"}); 
        when(mockedHttpClient.executeMethod(any(HostConfiguration.class), any(HttpMethod.class), 
                any(HttpState.class))).thenReturn(500);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable
            {
                JSONObject errorObject = errorSerializer.serialize(expectedException);
                return errorObject.toString();
            }
        }).when(mockedHttpMethodFactory.latestPostMethod).getResponseBodyAsString();
        
        try
        {
            transmitter.begin(target, "1234");
            fail();
        }
        catch(TransferException ex)
        {
            assertEquals(expectedException.getClass(), ex.getClass());
            assertEquals(expectedException.getMsgId(), ex.getMsgId());
            assertTrue(Arrays.deepEquals(expectedException.getMsgParams(), ex.getMsgParams()));
        }
    }
    
    private static class CustomSocketFactory implements SecureProtocolSocketFactory 
    {

        /* (non-Javadoc)
         * @see org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory#createSocket(java.net.Socket, java.lang.String, int, boolean)
         */
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
                UnknownHostException
        {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int)
         */
        public Socket createSocket(String host, int port) throws IOException, UnknownHostException
        {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int)
         */
        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException,
                UnknownHostException
        {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int, org.apache.commons.httpclient.params.HttpConnectionParams)
         */
        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort,
                HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException
        {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    private static class MockableHttpMethodFactory implements HttpMethodFactory
    {
        private PostMethod latestPostMethod;
        
        public MockableHttpMethodFactory()
        {
            reset();
        }
        
        @Override
        public PostMethod createPostMethod()
        {
            return latestPostMethod;
        }
        
        public void reset()
        {
            latestPostMethod = spy(new PostMethod());; 
        }
    }
}
