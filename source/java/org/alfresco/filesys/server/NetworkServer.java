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
package org.alfresco.filesys.server;

import java.net.InetAddress;
import java.util.Vector;

import org.alfresco.filesys.server.auth.CifsAuthenticator;
import org.alfresco.filesys.server.auth.acl.AccessControlManager;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.ShareMapper;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.core.SharedDeviceList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Network Server Base Class
 * <p>
 * Base class for server implementations for different protocols.
 */
public abstract class NetworkServer
{
    private static final Log logger = LogFactory.getLog("org.alfresco.filesys");

    // Protocol name

    private String m_protoName;

    // Server version

    private String m_version;

    // Server configuration
    private ServerConfiguration m_config;

    // Debug enabled flag and debug flags

    private boolean m_debug;
    private int m_debugFlags;

    // List of addresses that the server is bound to

    private InetAddress[] m_ipAddr;

    // Server shutdown flag and server active flag

    private boolean m_shutdown = false;
    private boolean m_active = false;

    // Server error exception details

    private Exception m_exception;

    // Server events listener

    private ServerListener m_listener;

    // Session listener list

    private Vector<SessionListener> m_sessListeners;

    /**
     * Class constructor
     * 
     * @param proto String
     * @param config ServerConfiguration
     */
    public NetworkServer(String proto, ServerConfiguration config)
    {
        m_protoName = proto;
        m_config = config;
    }

    /**
     * Returns the server configuration.
     * 
     * @return ServerConfiguration
     */
    public final ServerConfiguration getConfiguration()
    {
        return m_config;
    }

    /**
     * Return the authenticator for this server
     * 
     * @return CifsAuthenticator
     */
    public final CifsAuthenticator getAuthenticator()
    {
        return getConfiguration().getAuthenticator();
    }

    /**
     * Determine if an access control manager is configured
     * 
     * @return boolean
     */
    public final boolean hasAccessControlManager()
    {
        return getConfiguration().getAccessControlManager() != null ? true : false;
    }

    /**
     * Return the access control manager
     * 
     * @return AccessControlManager
     */
    public final AccessControlManager getAccessControlManager()
    {
        return getConfiguration().getAccessControlManager();
    }

    /**
     * Return the main server name
     * 
     * @return String
     */
    public final String getServerName()
    {
        return m_config.getServerName();
    }

    /**
     * Return the list of IP addresses that the server is bound to.
     * 
     * @return java.net.InetAddress[]
     */
    public final InetAddress[] getServerAddresses()
    {
        return m_ipAddr;
    }

    /**
     * Return the share mapper
     * 
     * @return ShareMapper
     */
    public final ShareMapper getShareMapper()
    {
        return m_config.getShareMapper();
    }

    /**
     * Return the available shared device list.
     * 
     * @param host String
     * @param sess SrvSession
     * @return SharedDeviceList
     */
    public final SharedDeviceList getShareList(String host, SrvSession sess)
    {
        return getConfiguration().getShareMapper().getShareList(host, sess, false);
    }

    /**
     * Return the complete shared device list.
     * 
     * @param host String
     * @param sess SrvSession
     * @return SharedDeviceList
     */
    public final SharedDeviceList getFullShareList(String host, SrvSession sess)
    {
        return getConfiguration().getShareMapper().getShareList(host, sess, true);
    }

    /**
     * Find the shared device with the specified name.
     * 
     * @param host Host name from the UNC path
     * @param name Name of the shared device to find.
     * @param typ Shared device type
     * @param sess Session details
     * @param create Create share flag, false indicates lookup only
     * @return SharedDevice with the specified name and type, else null.
     * @exception Exception
     */
    public final SharedDevice findShare(String host, String name, int typ, SrvSession sess, boolean create)
            throws Exception
    {

        // Search for the specified share

        SharedDevice dev = getConfiguration().getShareMapper().findShare(host, name, typ, sess, create);

        // Return the shared device, or null

        return dev;
    }

    /**
     * Determine if the SMB server is active.
     * 
     * @return boolean
     */
    public final boolean isActive()
    {
        return m_active;
    }

    /**
     * Return the server version string, in 'n.n.n' format
     * 
     * @return String
     */

    public final String isVersion()
    {
        return m_version;
    }

    /**
     * Check if there is a stored server exception
     * 
     * @return boolean
     */
    public final boolean hasException()
    {
        return m_exception != null ? true : false;
    }

    /**
     * Return the stored exception
     * 
     * @return Exception
     */
    public final Exception getException()
    {
        return m_exception;
    }

    /**
     * Clear the stored server exception
     */
    public final void clearException()
    {
        m_exception = null;
    }

    /**
     * Return the server protocol name
     * 
     * @return String
     */
    public final String getProtocolName()
    {
        return m_protoName;
    }

    /**
     * Determine if debug output is enabled
     * 
     * @return boolean
     */
    public final boolean hasDebug()
    {
        return m_debug;
    }

    /**
     * Determine if the specified debug flag is enabled
     * 
     * @return boolean
     */
    public final boolean hasDebugFlag(int flg)
    {
        return (m_debugFlags & flg) != 0 ? true : false;
    }

    /**
     * Check if the shutdown flag is set
     * 
     * @return boolean
     */
    public final boolean hasShutdown()
    {
        return m_shutdown;
    }

    /**
     * Set/clear the server active flag
     * 
     * @param active boolean
     */
    protected void setActive(boolean active)
    {
        m_active = active;
    }

    /**
     * Set the stored server exception
     * 
     * @param ex Exception
     */
    protected final void setException(Exception ex)
    {
        m_exception = ex;
    }

    /**
     * Set the addresses that the server is bound to
     * 
     * @param adds InetAddress[]
     */
    protected final void setServerAddresses(InetAddress[] addrs)
    {
        m_ipAddr = addrs;
    }

    /**
     * Set the server version
     * 
     * @param ver String
     */
    protected final void setVersion(String ver)
    {
        m_version = ver;
    }

    /**
     * Enable/disable debug output for the server
     * 
     * @param dbg boolean
     */
    protected final void setDebug(boolean dbg)
    {
        m_debug = dbg;
    }

    /**
     * Set the debug flags
     * 
     * @param flags int
     */
    protected final void setDebugFlags(int flags)
    {
        m_debugFlags = flags;
        setDebug(flags == 0 ? false : true);
    }

    /**
     * Set/clear the shutdown flag
     * 
     * @param ena boolean
     */
    protected final void setShutdown(boolean ena)
    {
        m_shutdown = ena;
    }

    /**
     * Add a server listener to this server
     * 
     * @param l ServerListener
     */
    public final void addServerListener(ServerListener l)
    {
        m_listener = l;
    }

    /**
     * Remove the server listener
     * 
     * @param l ServerListener
     */
    public final void removeServerListener(ServerListener l)
    {
        if (m_listener == l)
            m_listener = null;
    }

    /**
     * Add a new session listener to the network server.
     * 
     * @param l SessionListener
     */
    public final void addSessionListener(SessionListener l)
    {

        // Check if the session listener list is allocated

        if (m_sessListeners == null)
            m_sessListeners = new Vector<SessionListener>();
        m_sessListeners.add(l);
    }

    /**
     * Remove a session listener from the network server.
     * 
     * @param l SessionListener
     */
    public final void removeSessionListener(SessionListener l)
    {

        // Check if the listener list is valid

        if (m_sessListeners == null)
            return;
        m_sessListeners.removeElement(l);
    }

    /**
     * Fire a server event to the registered listener
     * 
     * @param event int
     */
    protected final void fireServerEvent(int event)
    {

        // Check if there is a listener registered with this server

        if (m_listener != null)
        {
            try
            {
                m_listener.serverStatusEvent(this, event);
            }
            catch (Exception ex)
            {
            }
        }
    }

    /**
     * Start the network server
     */
    public abstract void startServer();

    /**
     * Shutdown the network server
     * 
     * @param immediate boolean
     */
    public abstract void shutdownServer(boolean immediate);

    /**
     * Trigger a closed session event to all registered session listeners.
     * 
     * @param sess SrvSession
     */
    protected final void fireSessionClosedEvent(SrvSession sess)
    {

        // Check if there are any listeners

        if (m_sessListeners == null || m_sessListeners.size() == 0)
            return;

        // Inform all registered listeners

        for (int i = 0; i < m_sessListeners.size(); i++)
        {

            // Get the current session listener

            try
            {
                SessionListener sessListener = (SessionListener) m_sessListeners.elementAt(i);
                sessListener.sessionClosed(sess);
            }
            catch (Exception ex)
            {
                logger.error("Session listener error [closed]: ", ex);
            }
        }
    }

    /**
     * Trigger a new session event to all registered session listeners.
     * 
     * @param sess SrvSession
     */
    protected final void fireSessionLoggedOnEvent(SrvSession sess)
    {

        // Check if there are any listeners

        if (m_sessListeners == null || m_sessListeners.size() == 0)
            return;

        // Inform all registered listeners

        for (int i = 0; i < m_sessListeners.size(); i++)
        {

            // Get the current session listener

            try
            {
                SessionListener sessListener = (SessionListener) m_sessListeners.elementAt(i);
                sessListener.sessionLoggedOn(sess);
            }
            catch (Exception ex)
            {
                logger.error("Session listener error [logon]: ", ex);
            }
        }
    }

    /**
     * Trigger a new session event to all registered session listeners.
     * 
     * @param sess SrvSession
     */
    protected final void fireSessionOpenEvent(SrvSession sess)
    {

        // Check if there are any listeners

        if (m_sessListeners == null || m_sessListeners.size() == 0)
            return;

        // Inform all registered listeners

        for (int i = 0; i < m_sessListeners.size(); i++)
        {

            // Get the current session listener

            try
            {
                SessionListener sessListener = (SessionListener) m_sessListeners.elementAt(i);
                sessListener.sessionCreated(sess);
            }
            catch (Exception ex)
            {
                logger.error("Session listener error [open]: ", ex);
            }
        }
    }
}
