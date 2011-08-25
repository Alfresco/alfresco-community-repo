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
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * This <i><b>Spring</b> depended</i> class allows to control the binding of a RMI Registry to some port and concrete
 * local host, e.g.: <code>localhost</code>, <code>192.168.0.1</code> etc. <s>Host may be configured with the
 * <code>-Djava.rmi.server.hostname</code> system property</s><br />
 * <br />
 * <i><b>NOTE:</b> Please look at <a href="http://issues.alfresco.com/jira/browse/ALF-4357">ALF-4357</a> for more
 * information</i>
 * 
 * @author Dmitry Velichkevich
 * @see InitializingBean <b>Spring</b> dependence
 * @see RMIServerSocketFactory
 * @see RMIClientSocketFactory
 */
public class HostConfigurableSocketFactory implements RMIServerSocketFactory, RMIClientSocketFactory, Serializable
{
    private static final long serialVersionUID = 1L;
    private static Log logger = LogFactory.getLog(HostConfigurableSocketFactory.class);

    /**
     * How many retries to attempt if the socket is in use.
     * Default is zero (no retries)
     */
    private int retries = 0;
    /**
     * How long to wait between retries, in miliseconds?
     */
    private int retryInterval = 250;
    
    private InetAddress host;
    
    public void setHost(String host)
    {
        try
        {
            InetAddress hostAddress = InetAddress.getByName(host);
            if (!hostAddress.isAnyLocalAddress())
            {
                this.host = hostAddress;
            }
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e.toString());
        }
    }
    
    /**
     * How many retries to attempt if the socket is in use.
     * Default is zero (no retries)
     */
    public void setRetries(int retries)
    {
       this.retries = retries;
    }
    
    /**
     * How long to wait between retries, in miliseconds?
     */
    public void setRetryInterval(int retryInterval)
    {
       this.retryInterval = retryInterval;
    }

    public Socket createSocket(String host, int port) throws IOException
    {
        return new Socket(host, port);
    }

    public ServerSocket createServerSocket(int port) throws IOException
    {
        ServerSocket socket = null;
        for(int i=0; socket == null && i<retries+1; i++)
        {
           try
           {
              socket = new ServerSocket(port, 50, this.host);
           }
           catch(BindException e)
           {
              if(i >= retries)
              {
                 // We're out of retries, abort
                 throw e;
              }
              else
              {
                 // Sleep and try again
                 logger.warn("Port in-use, retrying", e);
                 try
                 {
                    Thread.sleep(retryInterval);
                 }
                 catch(InterruptedException ie) {}
              }
           }
        }
        return socket;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        return (obj instanceof HostConfigurableSocketFactory)
                && EqualsHelper.nullSafeEquals(this.host, ((HostConfigurableSocketFactory) obj).host);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return this.host == null ? 0 : this.host.hashCode();
    }
}
