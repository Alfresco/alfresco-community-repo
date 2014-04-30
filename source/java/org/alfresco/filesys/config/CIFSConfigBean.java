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
package org.alfresco.filesys.config;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.jlan.server.auth.ICifsAuthenticator;
import org.alfresco.jlan.smb.server.VirtualCircuitList;

import static org.alfresco.filesys.AbstractServerConfigurationBean.MaxSessionTimeout;

// TODO: Auto-generated Javadoc
/**
 * The Class CIFSConfigBean.
 * 
 * @author dward
 */
public class CIFSConfigBean
{

    /** The server enabled. */
    private boolean serverEnabled;

    /** The disable native code. */
    private boolean disableNativeCode;

    /** The broadcast addess. */
    private String broadcastAddress;

    /** The server name. */
    private String serverName;

    /** The domain name. */
    private String domainName;

    /** The server comment. */
    private String serverComment;

    /** The bind to adapter. */
    private String bindToAdapter;

    /** The bind to address. */
    private String bindToAddress;

    /** The authenticator. */
    private ICifsAuthenticator authenticator;

    /** The host accouncer enabled. */
    private boolean hostAccouncerEnabled;

    /** The host accounce interval. */
    private Integer hostAccounceInterval;

    /** The net biossmb. */
    private NetBIOSSMBConfigBean netBIOSSMB;

    /** The tcpip smb. */
    private TcpipSMBConfigBean tcpipSMB;

    /** The win32 net bios. */
    private Win32NetBIOSConfigBean win32NetBIOS;

    /** The win32 host announcer enabled. */
    private boolean win32HostAnnouncerEnabled;

    /** The win32 host announce interval. */
    private Integer win32HostAnnounceInterval;

    /** The WINS config. */
    private WINSConfigBean winsConfig;

    /** The session debug flags. */
    private String sessionDebugFlags;

    /** The disable nio. */
    private boolean disableNIO;

    /** The session timeout. */
    private Integer sessionTimeout;

    // Maximum virtual circuits per session
    
    private int m_maxVC = VirtualCircuitList.DefMaxCircuits;
    
    /**
     * Checks if is server enabled.
     * 
     * @return true, if is server enabled
     */
    public boolean getServerEnabled()
    {
        return serverEnabled;
    }

    /**
     * Sets the server enabled.
     * 
     * @param serverEnabled
     *            the new server enabled
     */
    public void setServerEnabled(boolean serverEnabled)
    {
        this.serverEnabled = serverEnabled;
    }

    /**
     * Checks if is disable native code.
     * 
     * @return true, if is disable native code
     */
    public boolean getDisableNativeCode()
    {
        return disableNativeCode;
    }

    /**
     * Sets the disable native code.
     * 
     * @param disableNativeCode
     *            the new disable native code
     */
    public void setDisableNativeCode(boolean disableNativeCode)
    {
        this.disableNativeCode = disableNativeCode;
    }

    /**
     * Gets the broadcast address.
     * 
     * @return the broadcast address
     */
    public String getBroadcastAddress()
    {
        return broadcastAddress;
    }

    /**
     * Sets the broadcast address.
     * 
     * @param broadcastAddress
     *            the new broadcast address
     */
    public void setBroadcastAddress(String broadcastAddress)
    {
        this.broadcastAddress = broadcastAddress;
    }

    /**
     * Gets the server name.
     * 
     * @return the server name
     */
    public String getServerName()
    {
        return serverName;
    }

    /**
     * Sets the server name.
     * 
     * @param serverName
     *            the new server name
     */
    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }

    /**
     * Gets the domain name.
     * 
     * @return the domain name
     */
    public String getDomainName()
    {
        return domainName;
    }

    /**
     * Sets the domain name.
     * 
     * @param domainName
     *            the new domain name
     */
    public void setDomainName(String domainName)
    {
        this.domainName = domainName;
    }

    /**
     * Gets the server comment.
     * 
     * @return the server comment
     */
    public String getServerComment()
    {
        return serverComment;
    }

    /**
     * Sets the server comment.
     * 
     * @param serverComment
     *            the new server comment
     */
    public void setServerComment(String serverComment)
    {
        this.serverComment = serverComment;
    }

    /**
     * Gets the bind to adapter.
     * 
     * @return the bind to adapter
     */
    public String getBindToAdapter()
    {
        return bindToAdapter;
    }

    /**
     * Sets the bind to adapter.
     * 
     * @param bindToAdapter
     *            the new bind to adapter
     */
    public void setBindToAdapter(String bindToAdapter)
    {
        this.bindToAdapter = bindToAdapter;
    }

    /**
     * Gets the bind to address.
     * 
     * @return the bind to address
     */
    public String getBindToAddress()
    {
        return bindToAddress;
    }

    /**
     * Sets the bind to address.
     * 
     * @param bindToAddress
     *            the new bind to address
     */
    public void setBindToAddress(String bindToAddress)
    {
        this.bindToAddress = bindToAddress;
    }

    /**
     * Gets the authenticator.
     * 
     * @return the authenticator
     */
    public ICifsAuthenticator getAuthenticator()
    {
        return authenticator;
    }

    /**
     * Sets the authenticator.
     * 
     * @param authenticator
     *            the new authenticator
     */
    public void setAuthenticator(ICifsAuthenticator authenticator)
    {
        this.authenticator = authenticator;
    }

    /**
     * Checks if is host accouncer enabled.
     * 
     * @return true, if is host accouncer enabled
     */
    public boolean getHostAccouncerEnabled()
    {
        return hostAccouncerEnabled;
    }

    /**
     * Sets the host accouncer enabled.
     * 
     * @param hostAccouncerEnabled
     *            the new host accouncer enabled
     */
    public void setHostAccouncerEnabled(boolean hostAccouncerEnabled)
    {
        this.hostAccouncerEnabled = hostAccouncerEnabled;
    }

    /**
     * Gets the host accounce interval.
     * 
     * @return the host accounce interval
     */
    public Integer getHostAccounceInterval()
    {
        return hostAccounceInterval;
    }

    /**
     * Sets the host accounce interval.
     * 
     * @param hostAccounceInterval
     *            the new host accounce interval
     */
    public void setHostAccounceInterval(Integer hostAccounceInterval)
    {
        this.hostAccounceInterval = hostAccounceInterval;
    }

    /**
     * Gets the net biossmb.
     * 
     * @return the net biossmb
     */
    public NetBIOSSMBConfigBean getNetBIOSSMB()
    {
        return netBIOSSMB;
    }

    /**
     * Sets the net biossmb.
     * 
     * @param netBIOSSMB
     *            the new net biossmb
     */
    public void setNetBIOSSMB(NetBIOSSMBConfigBean netBIOSSMB)
    {
        this.netBIOSSMB = netBIOSSMB;
    }

    /**
     * Gets the tcpip smb.
     * 
     * @return the tcpip smb
     */
    public TcpipSMBConfigBean getTcpipSMB()
    {
        return tcpipSMB;
    }

    /**
     * Return the maxmimum virtual circuits per session
     * 
     * @return int
     */
    public int getMaximumVirtualCircuits() {
    	return m_maxVC;
    }
    
    /**
     * Sets the tcpip smb.
     * 
     * @param tcpipSMB
     *            the new tcpip smb
     */
    public void setTcpipSMB(TcpipSMBConfigBean tcpipSMB)
    {
        this.tcpipSMB = tcpipSMB;
    }

    /**
     * Gets the win32 net bios.
     * 
     * @return the win32 net bios
     */
    public Win32NetBIOSConfigBean getWin32NetBIOS()
    {
        return win32NetBIOS;
    }

    /**
     * Sets the win32 net bios.
     * 
     * @param win32NetBIOS
     *            the new win32 net bios
     */
    public void setWin32NetBIOS(Win32NetBIOSConfigBean win32NetBIOS)
    {
        this.win32NetBIOS = win32NetBIOS;
    }

    /**
     * Checks if is win32 host announcer enabled.
     * 
     * @return true, if is win32 host announcer enabled
     */
    public boolean getWin32HostAnnouncerEnabled()
    {
        return win32HostAnnouncerEnabled;
    }

    /**
     * Sets the win32 host announcer enabled.
     * 
     * @param win32HostAnnouncerEnabled
     *            the new win32 host announcer enabled
     */
    public void setWin32HostAnnouncerEnabled(boolean win32HostAnnouncerEnabled)
    {
        this.win32HostAnnouncerEnabled = win32HostAnnouncerEnabled;
    }

    /**
     * Gets the win32 host announce interval.
     * 
     * @return the win32 host announce interval
     */
    public Integer getWin32HostAnnounceInterval()
    {
        return win32HostAnnounceInterval;
    }

    /**
     * Sets the win32 host announce interval.
     * 
     * @param win32HostAnnounceInterval
     *            the new win32 host announce interval
     */
    public void setWin32HostAnnounceInterval(Integer win32HostAnnounceInterval)
    {
        this.win32HostAnnounceInterval = win32HostAnnounceInterval;
    }

    /**
     * Gets the wINS config.
     * 
     * @return the wINS config
     */
    public WINSConfigBean getWINSConfig()
    {
        return winsConfig;
    }

    /**
     * Sets the wINS config.
     * 
     * @param config
     *            the new wINS config
     */
    public void setWINSConfig(WINSConfigBean config)
    {
        winsConfig = config;
    }

    /**
     * Gets the session debug flags.
     * 
     * @return the session debug flags
     */
    public String getSessionDebugFlags()
    {
        return sessionDebugFlags;
    }

    /**
     * Sets the session debug flags.
     * 
     * @param sessionDebugFlags
     *            the new session debug flags
     */
    public void setSessionDebugFlags(String sessionDebugFlags)
    {
        this.sessionDebugFlags = sessionDebugFlags;
    }

    /**
     * Checks if is disable nio.
     * 
     * @return true, if is disable nio
     */
    public boolean getDisableNIO()
    {
        return disableNIO;
    }

    /**
     * Sets the disable nio.
     * 
     * @param disableNIO
     *            the new disable nio
     */
    public void setDisableNIO(boolean disableNIO)
    {
        this.disableNIO = disableNIO;
    }

    /**
     * Gets the session timeout.
     * 
     * @return the session timeout
     */
    public Integer getSessionTimeout()
    {
        return sessionTimeout;
    }

    /**
     * Sets the session timeout.
     * 
     * @param sessionTimeout
     *            the new session timeout
     */
    public void setSessionTimeout(Integer sessionTimeout)
    {
        validateSessionTimeout(sessionTimeout);
        this.sessionTimeout = sessionTimeout;
    }

    /**
     * Validates the session timeout.
     * 
     * @param sessionTimeout
     *            the session timeout to validate
     */
    public void validateSessionTimeout(Integer sessionTimeout)
    {
        if (sessionTimeout < 0 || sessionTimeout > MaxSessionTimeout)
            throw new AlfrescoRuntimeException("Session timeout out of range (0 - " + MaxSessionTimeout + ")");
    }

    /**
     * Set the maximum virtual circuits per session
     * 
     * @param maxVC int
     */
    public void setMaximumVirtualCircuits( int maxVC) {
    	m_maxVC = maxVC;
    }
}
