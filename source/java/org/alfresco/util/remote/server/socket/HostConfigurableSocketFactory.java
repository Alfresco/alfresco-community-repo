/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.util.remote.server.socket;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;

/**
 * This <i><b>Spring</b> depended</i> class allows to control the binding of a RMI Registry to some port and concrete local host, e.g.: <code>localhost</code>,
 * <code>192.168.0.1</code> etc. Host may be configured with the <code>-Djava.rmi.server.hostname</code> system property<br />
 * <br />
 * <i><b>NOTE:</b> The system property configuration has the highest priority</i>
 * 
 * @author Dmitry Velichkevich
 * @see InitializingBean <b>Spring</b> dependence
 * @see RMIServerSocketFactory
 * @see RMIClientSocketFactory
 */
public class HostConfigurableSocketFactory implements RMIServerSocketFactory, RMIClientSocketFactory, InitializingBean, Serializable
{
    private static final long serialVersionUID = 4115227360496369889L;

    private static final String SERVER_HOSTNAME_PROPERTY = "java.rmi.server.hostname";

    private InetAddress host;

    public void setHost(String host)
    {
        try
        {
            this.host = InetAddress.getByName(host);
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e.toString());
        }
    }

    public void setHost(InetAddress host)
    {
        this.host = host;
    }

    /**
     * @return {@link String} value which represents either a <i>Host Name</i> or a <i>Host (IP) Address</i> if <i>Host Name</i> is not reachable
     */
    public String getHost()
    {
        if (null != host.getHostName())
        {
            return host.getHostName();
        }
        return host.getHostAddress();
    }

    public Socket createSocket(String host, int port) throws IOException
    {
        return new Socket(this.host, port);
    }

    public ServerSocket createServerSocket(int port) throws IOException
    {
        return new ServerSocket(port, 0, host);
    }

    /**
     * Checks whether the -Djava.rmi.server.hostname system property presented and sets a host from this property if it is true
     */
    public void afterPropertiesSet() throws Exception
    {
        Properties properties = System.getProperties();
        if (properties.containsKey(SERVER_HOSTNAME_PROPERTY))
        {
            setHost(properties.getProperty(SERVER_HOSTNAME_PROPERTY));
        }
    }
}
