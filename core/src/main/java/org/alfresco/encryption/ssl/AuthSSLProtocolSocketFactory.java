/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.encryption.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.alfresco.encryption.AlfrescoKeyStore;
import org.alfresco.encryption.KeyResourceLoader;
import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Mutual Authentication against an Alfresco repository.
 * 
 * AuthSSLProtocolSocketFactory can be used to validate the identity of the HTTPS 
 * server against a list of trusted certificates and to authenticate to the HTTPS 
 * server using a private key. 
 * </p>
 * 
 * <p>
 * Adapted from code here: http://svn.apache.org/viewvc/httpcomponents/oac.hc3x/trunk/src/contrib/org/apache/commons/httpclient/contrib/ssl/AuthSSLX509TrustManager.java?revision=608014&view=co
 * </p>
 * 
 * <p>
 * AuthSSLProtocolSocketFactory will enable server authentication when supplied with
 * a {@link KeyStore truststore} file containing one or several trusted certificates. 
 * The client secure socket will reject the connection during the SSL session handshake 
 * if the target HTTPS server attempts to authenticate itself with a non-trusted 
 * certificate.
 * </p>
 * 
 * <p>
 * AuthSSLProtocolSocketFactory will enable client authentication when supplied with
 * a {@link KeyStore keystore} file containg a private key/public certificate pair. 
 * The client secure socket will use the private key to authenticate itself to the target 
 * HTTPS server during the SSL session handshake if requested to do so by the server. 
 * The target HTTPS server will in its turn verify the certificate presented by the client
 * in order to establish client's authenticity
 * </p>
 * 
 * 
 * @since 4.0
 */
public class AuthSSLProtocolSocketFactory implements SecureProtocolSocketFactory
{
    /** Log object for this class. */
    private static final Log logger = LogFactory.getLog(AuthSSLProtocolSocketFactory.class);

    private SSLContext sslcontext = null;

    private AlfrescoKeyStore keyStore = null;
    private AlfrescoKeyStore trustStore = null;

    /**
     * Constructor for AuthSSLProtocolSocketFactory. Either a keystore or truststore file
     * must be given. Otherwise SSL context initialization error will result.
     * 
     * @param sslKeyStore SSL parameters to use.
     * @param keyResourceLoader loads key resources from an arbitrary source e.g. classpath
     */
    public AuthSSLProtocolSocketFactory(AlfrescoKeyStore sslKeyStore, AlfrescoKeyStore sslTrustStore, KeyResourceLoader keyResourceLoader)
    {
        super();
        this.keyStore = sslKeyStore;
        this.trustStore = sslTrustStore;
    }

    private SSLContext createSSLContext()
    {
        KeyManager[] keymanagers = keyStore.createKeyManagers();;
        TrustManager[] trustmanagers = trustStore.createTrustManagers();

        try
        {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(keymanagers, trustmanagers, null);
            return sslcontext;
        }
        catch(Throwable e)
        {
            throw new AlfrescoRuntimeException("Unable to create SSL context", e);
        }
    }

    private SSLContext getSSLContext()
    {
        try
        {
            if(this.sslcontext == null)
            {
                this.sslcontext = createSSLContext();
            }
            return this.sslcontext;
        }
        catch(Throwable e)
        {
            throw new AlfrescoRuntimeException("Unable to create SSL context", e);
        }
    }

    /**
     * Attempts to get a new socket connection to the given host within the given time limit.
     * <p>
     * To circumvent the limitations of older JREs that do not support connect timeout a 
     * controller thread is executed. The controller thread attempts to create a new socket 
     * within the given limit of time. If socket constructor does not return until the 
     * timeout expires, the controller terminates and throws an {@link ConnectTimeoutException}
     * </p>
     *  
     * @param host the host name/IP
     * @param port the port on the host
     * @param localAddress the local host name/IP to bind the socket to
     * @param localPort the port on the local machine
     * @param params {@link HttpConnectionParams Http connection parameters}
     * 
     * @return Socket a new socket
     * 
     * @throws IOException if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     * determined
     */
    public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort,
            final HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException
    {
        SSLSocket sslSocket = null;

        if(params == null)
        {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        SocketFactory socketfactory = getSSLContext().getSocketFactory();
        if(timeout == 0)
        {
            sslSocket = (SSLSocket)socketfactory.createSocket(host, port, localAddress, localPort);
        }
        else
        {
            sslSocket = (SSLSocket)socketfactory.createSocket();
            SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
            SocketAddress remoteaddr = new InetSocketAddress(host, port);
            sslSocket.bind(localaddr);
            sslSocket.connect(remoteaddr, timeout);
        }

        return sslSocket;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort)
    throws IOException, UnknownHostException
    {
        SSLSocket sslSocket = (SSLSocket)getSSLContext().getSocketFactory().createSocket(host, port, clientHost, clientPort);
        return sslSocket;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException
    {
        SSLSocket sslSocket = (SSLSocket)getSSLContext().getSocketFactory().createSocket(host, port);
        return sslSocket;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
     */
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
    throws IOException, UnknownHostException
    {
        SSLSocket sslSocket = (SSLSocket)getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
        return sslSocket;
    }
}
