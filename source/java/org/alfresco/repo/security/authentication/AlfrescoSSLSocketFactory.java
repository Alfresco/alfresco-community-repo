/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.security.authentication;

import org.alfresco.error.AlfrescoRuntimeException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * SSL socket factory that uses custom trustStore
 * <br>The factory should be first initialized
 *
 * @author alex.mukha
 * @since 5.0
 */
public class AlfrescoSSLSocketFactory extends SSLSocketFactory
{
    private static SSLContext context;

    public AlfrescoSSLSocketFactory()
    {
    }

    /**
     * Initialize the factory with custom trustStore
     * @param trustStore
     */
    public static synchronized void initTrustedSSLSocketFactory(final KeyStore trustStore)
    {
        try
        {
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);
            context = SSLContext.getInstance("SSL");
            context.init(null, trustManagerFactory.getTrustManagers(), SecureRandom.getInstance("SHA1PRNG"));
        }
        catch (NoSuchAlgorithmException nsae)
        {
            throw new AlfrescoRuntimeException("The SSL socket factory cannot be initialized.", nsae);
        }
        catch (KeyStoreException kse)
        {
            throw new AlfrescoRuntimeException("The SSL socket factory cannot be initialized.", kse);
        }
        catch (KeyManagementException kme)
        {
            throw new AlfrescoRuntimeException("The SSL socket factory cannot be initialized.", kme);
        }
    }

    public static synchronized SocketFactory getDefault()
    {
        if (context == null)
        {
            throw new AlfrescoRuntimeException("The factory was not initialized.");
        }
        return new AlfrescoSSLSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites()
    {
        return context.getSocketFactory().getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites()
    {
        return context.getSocketFactory().getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException
    {
        return context.getSocketFactory().createSocket(socket, s, i, b);
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException, UnknownHostException
    {
        return context.getSocketFactory().createSocket(s, i);
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i2) throws IOException, UnknownHostException
    {
        return context.getSocketFactory().createSocket(s, i, inetAddress, i2);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException
    {
        return context.getSocketFactory().createSocket(inetAddress, i);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) throws IOException
    {
        return context.getSocketFactory().createSocket(inetAddress, i, inetAddress2, i2);
    }
}
