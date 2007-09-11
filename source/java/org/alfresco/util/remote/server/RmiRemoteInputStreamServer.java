/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.util.remote.server;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.UUID;

import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.remoting.rmi.RmiServiceExporter;

/**
 * Concrete implementation of a remoting InputStream based on RMI.
 * 
 * @author <a href="mailto:Michael.Shavnev@effective-soft.com">Michael Shavnev</a>
 * @since Alfresco 2.2
 */
public class RmiRemoteInputStreamServer extends AbstractRemoteInputStreamServer
{
    private RmiServiceExporter rmiServiceExporter;

    public RmiRemoteInputStreamServer(InputStream inputStream)
    {
        super(inputStream);
    }

    public String start(String host, int port) throws RemoteException
    {
        String name = inputStream.getClass().getName() + UUID.randomUUID();
        rmiServiceExporter = new RmiServiceExporter();
        rmiServiceExporter.setServiceName(name);
        rmiServiceExporter.setRegistryPort(port);
        rmiServiceExporter.setRegistryHost(host);
        rmiServiceExporter.setServiceInterface(RemoteInputStreamServer.class);
        rmiServiceExporter.setService(this);
        rmiServiceExporter.afterPropertiesSet();
        return name;
    }

    /**
     * Closes the stream and the RMI connection to the peer.
     */
    public void close() throws IOException
    {
        try
        {
            inputStream.close();
        }
        finally
        {
            if (rmiServiceExporter != null)
            {
                try
                {
                    rmiServiceExporter.destroy();
                }
                catch (Throwable e)
                {
                    throw new IOException(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Utility method to lookup a remote stream peer over RMI.
     */
    public static RemoteInputStreamServer obtain(String host, int port, String name) throws RemoteException
    {
        RmiProxyFactoryBean rmiProxyFactoryBean = new RmiProxyFactoryBean();
        rmiProxyFactoryBean.setServiceUrl("rmi://" + host + ":" + port + "/" + name);
        rmiProxyFactoryBean.setServiceInterface(RemoteInputStreamServer.class);
        rmiProxyFactoryBean.setRefreshStubOnConnectFailure(true);
        try
        {
            rmiProxyFactoryBean.afterPropertiesSet();
        }
        catch (Exception e)
        {
            throw new RemoteException("Error create rmi proxy");
        }
        return (RemoteInputStreamServer) rmiProxyFactoryBean.getObject();
    }
}
