/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.smb.server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Session Socket Handler Abstract Class
 * 
 * @author GKSpencer
 */
public abstract class SessionSocketHandler implements Runnable
{
    // Debug logging

    protected static Log logger = LogFactory.getLog("org.alfresco.smb.protocol");

    // Define the listen backlog for the server socket

    protected static final int LISTEN_BACKLOG = 10;

    // Server that the socket handler is associated with

    private SMBServer m_server;

    // Address/post to use

    private int m_port;
    private InetAddress m_bindAddr;

    // Server socket

    private ServerSocket m_srvSock;

    // Debug output enable

    private boolean m_debug;

    // Socket handler thread shutdown flag

    private boolean m_shutdown;

    // Session socket handler name

    private String m_name;

    // Session id

    private static int m_sessId;

    /**
     * Class constructor
     * 
     * @param name String
     * @param srv SMBServer
     * @param port int
     * @param bindAddr InetAddress
     * @param debug boolean
     */
    public SessionSocketHandler(String name, SMBServer srv, int port, InetAddress bindAddr, boolean debug)
    {
        m_name = name;
        m_server = srv;
        m_port = port;
        m_bindAddr = bindAddr;
        m_debug = debug;
    }

    /**
     * Class constructor
     * 
     * @param name String
     * @param srv SMBServer
     * @param debug boolean
     */
    public SessionSocketHandler(String name, SMBServer srv, boolean debug)
    {
        m_name = name;
        m_server = srv;
        m_debug = debug;
    }

    /**
     * Return the handler name
     * 
     * @return String
     */
    public final String getName()
    {
        return m_name;
    }

    /**
     * Return the server
     * 
     * @return SMBServer
     */
    protected final SMBServer getServer()
    {
        return m_server;
    }

    /**
     * Return the port
     * 
     * @return int
     */
    protected final int getPort()
    {
        return m_port;
    }

    /**
     * Determine if the socket handler should bind to a particular address
     * 
     * @return boolean
     */
    protected final boolean hasBindAddress()
    {
        return m_bindAddr != null ? true : false;
    }

    /**
     * Return the bind address return InetAddress
     */
    protected final InetAddress getBindAddress()
    {
        return m_bindAddr;
    }

    /**
     * Return the next session id
     * 
     * @return int
     */
    protected final synchronized int getNextSessionId()
    {
        return m_sessId++;
    }

    /**
     * Determine if debug output is enabled
     * 
     * @return boolean
     */
    protected final boolean hasDebug()
    {
        return m_debug;
    }

    /**
     * Return the server socket
     * 
     * @return ServerSocket
     */
    protected final ServerSocket getSocket()
    {
        return m_srvSock;
    }

    /**
     * Set the server socket
     * 
     * @param sock ServerSocket
     */
    protected final void setSocket(ServerSocket sock)
    {
        m_srvSock = sock;
    }

    /**
     * Determine if the shutdown flag is set
     * 
     * @return boolean
     */
    protected final boolean hasShutdown()
    {
        return m_shutdown;
    }

    /**
     * Clear the shutdown request flag
     */
    protected final void clearShutdown()
    {
        m_shutdown = false;
    }

    /**
     * Request the socket handler to shutdown
     */
    public void shutdownRequest()
    {

        // Indicate that the server is closing

        m_shutdown = true;

        try
        {

            // Close the server socket so that any pending receive is cancelled

            if (m_srvSock != null)
                m_srvSock.close();
        }
        catch (SocketException ex)
        {
        }
        catch (Exception ex)
        {
        }
    }

    /**
     * Initialize the session socket handler
     * 
     * @exception Exception
     */
    public void initialize() throws Exception
    {

        // Check if the server should bind to a particular local address, or all local addresses

        ServerSocket srvSock = null;

        if (hasBindAddress())
            srvSock = new ServerSocket(getPort(), LISTEN_BACKLOG, getBindAddress());
        else
            srvSock = new ServerSocket(getPort(), LISTEN_BACKLOG);
        setSocket(srvSock);

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("Binding " + getName() + " session handler to local address : "
                    + (hasBindAddress() ? getBindAddress().getHostAddress() : "ALL"));
    }

    /**
     * @see Runnable#run()
     */
    public abstract void run();

    /**
     * Return the session socket handler as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");
        str.append(getName());
        str.append(",");
        str.append(getServer().getServerName());
        str.append(",");
        str.append(getBindAddress() != null ? getBindAddress().getHostAddress() : "<All>");
        str.append(":");
        str.append(getPort());
        str.append("]");

        return str.toString();
    }
}
