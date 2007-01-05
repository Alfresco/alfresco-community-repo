/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.filesys.ftp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Enumeration;

import org.alfresco.filesys.server.ServerListener;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.SharedDeviceList;
import org.alfresco.filesys.server.filesys.NetworkFileServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Create an FTP server on the specified port. The default server port is 21.
 * 
 * @author GKSpencer
 */
public class FTPNetworkServer extends NetworkFileServer implements Runnable
{

    // Debug logging

    private static final Log logger = LogFactory.getLog("org.alfresco.ftp.protocol");

    // Constants
    //
    // Session Thread group

    private static final ThreadGroup THREAD_GROUP_SESSION = new ThreadGroup("FTP_SESSION_GROUP");
    
    // Listen backlog for the server socket

    protected static final int LISTEN_BACKLOG = 10;

    // Default FTP server port

    protected static final int SERVER_PORT = 21;

    // Server socket

    private ServerSocket m_srvSock;

    // Active session list

    private FTPSessionList m_sessions;

    // List of available shares

    private SharedDeviceList m_shares;

    // Next available session id

    private int m_sessId;

    // Root path for new sessions

    private FTPPath m_rootPath;

    // FTP server thread

    private Thread m_srvThread;

    // Local server address string, in FTP format (ie. n,n,n,n)

    private String m_localFTPaddress;

    // SITE command interface
    
    private FTPSiteInterface m_siteInterface;
    
    // Default character encoding to use for file names
    
    private String m_charSet;
    
    /**
     * Class constructor
     * 
     * @param serviceResgistry ServiceRegistry
     * @param config ServerConfiguration
     */
    public FTPNetworkServer(ServerConfiguration config)
    {
        super("FTP", config);

        // Allocate the session lists

        m_sessions = new FTPSessionList();

        // Enable debug

        if (getConfiguration().getFTPDebug() != 0)
            setDebug(true);

        // Create the root path, if configured

        if (getConfiguration().hasFTPRootPath())
        {

            try
            {

                // Create the root path

                m_rootPath = new FTPPath(getConfiguration().getFTPRootPath());
            }
            catch (InvalidPathException ex)
            {
                logger.error(ex);
            }
        }
        
        // Set the default character set
        
        m_charSet = config.getFTPCharacterSet();
        if ( m_charSet == null)
        	m_charSet = Charset.defaultCharset().name();
    }

    /**
     * Add a new session to the server
     * 
     * @param sess FTPSrvSession
     */
    protected final void addSession(FTPSrvSession sess)
    {

        // Add the session to the session list

        m_sessions.addSession(sess);

        // Propagate the debug settings to the new session

        if (hasDebug())
        {

            // Enable session debugging, output to the same stream as the server

            sess.setDebug(getConfiguration().getFTPDebug());
        }
    }

    /**
     * emove a session from the server
     * 
     * @param sess FTPSrvSession
     */
    protected final void removeSession(FTPSrvSession sess)
    {

        // Remove the session from the active session list

        if (m_sessions.removeSession(sess) != null)
        {

            // Inform listeners that a session has closed

            fireSessionClosedEvent(sess);
        }
    }

    /**
     * Allocate a local port for a data session
     * 
     * @param sess FTPSrvSession
     * @param remAddr InetAddress
     * @param remPort int
     * @return FTPDataSession
     * @exception IOException
     */
    protected final FTPDataSession allocateDataSession(FTPSrvSession sess, InetAddress remAddr, int remPort)
            throws IOException
    {
        // Create a new FTP data session

        FTPDataSession dataSess = null;
        if (remAddr != null)
        {

            // Create a normal data session

            dataSess = new FTPDataSession(sess, remAddr, remPort);
        }
        else
        {

            // Create a passive data session

            dataSess = new FTPDataSession(sess, getBindAddress());
        }

        // Return the data session

        return dataSess;
    }

    /**
     * Release a data session
     * 
     * @param dataSess FTPDataSession
     */
    protected final void releaseDataSession(FTPDataSession dataSess)
    {

        // Close the data session

        dataSess.closeSession();
    }

    /**
     * Get the shared device list
     * 
     * @return SharedDeviceList
     */
    public final SharedDeviceList getShareList()
    {

        // Check if the share list has been populated

        if (m_shares == null)
            m_shares = getConfiguration().getShareMapper()
                    .getShareList(getConfiguration().getServerName(), null, false);

        // Return the share list

        return m_shares;
    }

    /**
     * Check if the FTP server is to be bound to a specific network adapter
     * 
     * @return boolean
     */
    public final boolean hasBindAddress()
    {
        return getConfiguration().getFTPBindAddress() != null ? true : false;
    }

    /**
     * Return the address that the FTP server should bind to
     * 
     * @return InetAddress
     */
    public final InetAddress getBindAddress()
    {
        return getConfiguration().getFTPBindAddress();
    }

    /**
     * Check if the root path is set
     * 
     * @return boolean
     */
    public final boolean hasRootPath()
    {
        return m_rootPath != null ? true : false;
    }

    /**
     * Check if anonymous logins are allowed
     * 
     * @return boolean
     */
    public final boolean allowAnonymous()
    {
        return getConfiguration().allowAnonymousFTP();
    }

    /**
     * Return the anonymous login user name
     * 
     * @return String
     */
    public final String getAnonymousAccount()
    {
        return getConfiguration().getAnonymousFTPAccount();
    }

    /**
     * Return the local FTP server address string in n,n,n,n format
     * 
     * @return String
     */
    public final String getLocalFTPAddressString()
    {
        return m_localFTPaddress;
    }

    /**
     * Return the next available session id
     * 
     * @return int
     */
    protected final synchronized int getNextSessionId()
    {
        return m_sessId++;
    }

    /**
     * Return the FTP server port
     * 
     * @return int
     */
    public final int getPort()
    {
        return getConfiguration().getFTPPort();
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
     * Return the root path for new sessions
     * 
     * @return FTPPath
     */
    public final FTPPath getRootPath()
    {
        return m_rootPath;
    }

    /**
     * Get the character set to use for file name encoding/decoding
     * 
     * @return String
     */
    public final String getCharacterSet()
    {
    	return m_charSet;
    }
    
    /**
     * Notify the server that a user has logged on.
     * 
     * @param sess SrvSession
     */
    protected final void sessionLoggedOn(SrvSession sess)
    {

        // Notify session listeners that a user has logged on.

        fireSessionLoggedOnEvent(sess);
    }

    /**
     * Start the SMB server.
     */
    public void run()
    {

        // Debug

        if (logger.isDebugEnabled() && hasDebug())
        {
            logger.debug("FTP Server starting on port " + getPort());
            if ( getCharacterSet() != null)
            	logger.debug( "Using character set " + getCharacterSet());
        }

        // Create a server socket to listen for incoming FTP session requests

        try
        {

            // Create the server socket to listen for incoming FTP session requests

            if (hasBindAddress())
                m_srvSock = new ServerSocket(getPort(), LISTEN_BACKLOG, getBindAddress());
            else
                m_srvSock = new ServerSocket(getPort(), LISTEN_BACKLOG);

            // DEBUG

            if (logger.isDebugEnabled() && hasDebug())
            {
                String ftpAddr = "ALL";
                
                if (hasBindAddress())
                    ftpAddr = getBindAddress().getHostAddress();
                logger.debug("FTP Binding to local address " + ftpAddr);
            }

            // If a bind address is set then we can set the FTP local address

            if (hasBindAddress())
                m_localFTPaddress = getBindAddress().getHostAddress().replace('.', ',');

            // Indicate that the server is active

            setActive(true);
            fireServerEvent(ServerListener.ServerActive);

            // Wait for incoming connection requests

            while (hasShutdown() == false)
            {

                // Wait for a connection

                Socket sessSock = getSocket().accept();

                // Set the local address string in FTP format (n,n,n,n), if not already set

                if (m_localFTPaddress == null)
                {
                    if (sessSock.getLocalAddress() != null)
                        m_localFTPaddress = sessSock.getLocalAddress().getHostAddress().replace('.', ',');
                }

                // Set socket options

                sessSock.setTcpNoDelay(true);

                // Debug

                if (logger.isDebugEnabled() && hasDebug())
                    logger.debug("FTP session request received from "
                            + sessSock.getInetAddress().getHostAddress());

                // Create a server session for the new request, and set the session id.

                FTPSrvSession srvSess = new FTPSrvSession(sessSock, this);
                srvSess.setSessionId(getNextSessionId());
                srvSess.setUniqueId("FTP" + srvSess.getSessionId());

                // Initialize the root path for the new session, if configured

                if (hasRootPath())
                    srvSess.setRootPath(getRootPath());

                // Add the session to the active session list

                addSession(srvSess);

                // Inform listeners that a new session has been created

                fireSessionOpenEvent(srvSess);

                // Start the new session in a seperate thread

                Thread srvThread = new Thread(THREAD_GROUP_SESSION, srvSess);
                srvThread.setDaemon(true);
                srvThread.setName("Sess_FTP" + srvSess.getSessionId() + "_"
                        + sessSock.getInetAddress().getHostAddress());
                srvThread.start();

                // Sleep for a while

                try
                {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException ex)
                {
                }
            }
        }
        catch (SocketException ex)
        {

            // Do not report an error if the server has shutdown, closing the server socket
            // causes an exception to be thrown.

            if (hasShutdown() == false)
            {
                logger.error("FTP Socket error", ex);

                // Inform listeners of the error, store the exception

                setException(ex);
                fireServerEvent(ServerListener.ServerError);
            }
        }
        catch (Exception ex)
        {

            // Do not report an error if the server has shutdown, closing the server socket
            // causes an exception to be thrown.

            if (hasShutdown() == false)
            {
                logger.error("FTP Server error", ex);
            }

            // Inform listeners of the error, store the exception

            setException(ex);
            fireServerEvent(ServerListener.ServerError);
        }

        // Close the active sessions

        Enumeration enm = m_sessions.enumerate();

        while (enm.hasMoreElements())
        {

            // Get the session id and associated session

            Integer sessId = (Integer) enm.nextElement();
            FTPSrvSession sess = m_sessions.findSession(sessId);

            // DEBUG

            if (logger.isDebugEnabled() && hasDebug())
                logger.debug("FTP Close session, id = " + sess.getSessionId());

            // Close the session

            sess.closeSession();
        }

        // Debug

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("FTP Server shutting down ...");

        // Indicate that the server has shutdown, inform listeners

        setActive(false);
        fireServerEvent(ServerListener.ServerShutdown);
    }

    /**
     * Shutdown the FTP server
     * 
     * @param immediate boolean
     */
    public void shutdownServer(boolean immediate)
    {

        // Set the shutdown flag

        setShutdown(true);

        // Close the FTP server listening socket to wakeup the main FTP server thread

        try
        {
            if (getSocket() != null)
                getSocket().close();
        }
        catch (IOException ex)
        {
        }

        // Wait for the main server thread to close

        if (m_srvThread != null)
        {

            try
            {
                m_srvThread.join(3000);
            }
            catch (Exception ex)
            {
            }
        }

        // Fire a shutdown notification event

        fireServerEvent(ServerListener.ServerShutdown);
    }

    /**
     * Start the FTP server in a seperate thread
     */
    public void startServer()
    {

        // Create a seperate thread to run the FTP server

        m_srvThread = new Thread(this);
        m_srvThread.setName("FTP Server");
        m_srvThread.start();

        // Fire a server startup event

        fireServerEvent(ServerListener.ServerStartup);
    }
    
    /**
     * Check if the site interface is valid
     * 
     * @return boolean
     */
    public final boolean hasSiteInterface()
    {
    	return m_siteInterface != null ? true : false;
    }
    
    /**
     * Return the site interface
     * 
     * @return FTPSiteInterface
     */
    public final FTPSiteInterface getSiteInterface()
    {
    	return m_siteInterface;
    }
    
    /**
     * Set the site specific commands interface
     * 
     * @param siteInterface FTPSiteInterface
     */
    public final void setSiteInterface( FTPSiteInterface siteInterface)
    {
    	m_siteInterface = siteInterface;
    }
}
