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
package org.alfresco.repo.security.authentication.ldap;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SSL Socket Factory that adds Hostname Verification to sockets
 */

public class AlfrescoLdapSSLSocketFactory extends SocketFactory
{
    private static Log logger = LogFactory.getLog(AlfrescoLdapSSLSocketFactory.class);
    
    private static Boolean useJava6CodeBase = null;
    private static Boolean useJava7CodeBase = null;

    public static SocketFactory getDefault()

    {
        return new AlfrescoLdapSSLSocketFactory();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException
    {
        SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
        addHostNameVerification(sslSocket);
        return sslSocket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException
    {
        SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
        addHostNameVerification(sslSocket);
        return sslSocket;
    }

    @Override
    public Socket createSocket(String address, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException
    {
        SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(address, port, localAddress, localPort);
        addHostNameVerification(sslSocket);
        return sslSocket;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException
    {
        SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(address, port, localAddress, localPort);
        addHostNameVerification(sslSocket);
        return sslSocket;
    }

    private void addHostNameVerification(SSLSocket sslSocket)
    {
        if (useJava6CodeBase == null || useJava6CodeBase)
        {
            //Try to use SSLSocketImpl.trySetHostnameVerification method that is supported by java6 and lower
            try
            {
                Method m = sslSocket.getClass().getMethod("trySetHostnameVerification", String.class);
                m.invoke(sslSocket, "LDAP");
                useJava6CodeBase = true;
                useJava7CodeBase = false;
            }
            catch (Throwable e)
            {
                useJava6CodeBase = false;
            }
        }

        if (useJava7CodeBase == null || useJava7CodeBase)
        {
            //Try to use sslParams.setEndpointIdentificationAlgorithm method that is supported by java 7 and higher
            try
            {
                SSLParameters sslParams = new SSLParameters();
                Method m = sslParams.getClass().getMethod("setEndpointIdentificationAlgorithm", String.class);
                m.invoke(sslParams, "LDAPS");
                sslSocket.setSSLParameters(sslParams);
                useJava6CodeBase = false;
                useJava7CodeBase = true;
            }
            catch (Throwable ee)
            {
                useJava7CodeBase = false;
                
                if(useJava6CodeBase == false && logger.isWarnEnabled())
                {
                    logger.warn("AlfrescoLdapSSLSocketFactory: Unable to turn on Hostname Verification");
                }
            }
        }
    }
}
