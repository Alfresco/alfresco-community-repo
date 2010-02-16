/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.config;

import org.alfresco.jlan.ftp.FTPAuthenticator;

// TODO: Auto-generated Javadoc
/**
 * The Class FTPConfigBean.
 * 
 * @author dward
 */
public class FTPConfigBean
{

    /** The server enabled. */
    private boolean serverEnabled;

    /** The bind to. */
    private String bindTo;

    /** The port. */
    private Integer port;

    /** The allow anonymous. */
    private boolean allowAnonymous;

    /** The anonymous account. */
    private String anonymousAccount;

    /** The root directory. */
    private String rootDirectory;

    /** The debug flags. */
    private String debugFlags;

    /** The char set. */
    private String charSet;

    /** The authenticator. */
    private FTPAuthenticator authenticator;

    /** Is IP v6 enabled? */
    private boolean ipv6Enabled;

    // Data port range
    
    private int dataPortFrom;
    private int dataPortTo;
    
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
     * Gets the bind to.
     * 
     * @return the bind to
     */
    public String getBindTo()
    {
        return bindTo;
    }

    /**
     * Sets the bind to.
     * 
     * @param bindTo
     *            the new bind to
     */
    public void setBindTo(String bindTo)
    {
        this.bindTo = bindTo;
    }

    /**
     * Gets the port.
     * 
     * @return the port
     */
    public Integer getPort()
    {
        return port;
    }

    /**
     * Sets the port.
     * 
     * @param port
     *            the new port
     */
    public void setPort(Integer port)
    {
        this.port = port;
    }

    /**
     * Checks if is allow anonymous.
     * 
     * @return true, if is allow anonymous
     */
    public boolean getAllowAnonymous()
    {
        return allowAnonymous;
    }

    /**
     * Sets the allow anonymous.
     * 
     * @param allowAnonymous
     *            the new allow anonymous
     */
    public void setAllowAnonymous(boolean allowAnonymous)
    {
        this.allowAnonymous = allowAnonymous;
    }

    /**
     * Gets the anonymous account.
     * 
     * @return the anonymous account
     */
    public String getAnonymousAccount()
    {
        return anonymousAccount;
    }

    /**
     * Sets the anonymous account.
     * 
     * @param anonymousAccount
     *            the new anonymous account
     */
    public void setAnonymousAccount(String anonymousAccount)
    {
        this.anonymousAccount = anonymousAccount;
    }

    /**
     * Gets the root directory.
     * 
     * @return the root directory
     */
    public String getRootDirectory()
    {
        return rootDirectory;
    }

    /**
     * Sets the root directory.
     * 
     * @param rootDirectory
     *            the new root directory
     */
    public void setRootDirectory(String rootDirectory)
    {
        this.rootDirectory = rootDirectory;
    }

    /**
     * Gets the debug flags.
     * 
     * @return the debug flags
     */
    public String getDebugFlags()
    {
        return debugFlags;
    }

    /**
     * Sets the debug flags.
     * 
     * @param debugFlags
     *            the new debug flags
     */
    public void setDebugFlags(String debugFlags)
    {
        this.debugFlags = debugFlags;
    }

    /**
     * Gets the char set.
     * 
     * @return the char set
     */
    public String getCharSet()
    {
        return charSet;
    }

    /**
     * Sets the char set.
     * 
     * @param charSet
     *            the new char set
     */
    public void setCharSet(String charSet)
    {
        this.charSet = charSet;
    }

    /**
     * Gets the authenticator.
     * 
     * @return the authenticator
     */
    public FTPAuthenticator getAuthenticator()
    {
        return authenticator;
    }

    /**
     * Sets the authenticator.
     * 
     * @param authenticator
     *            the new authenticator
     */
    public void setAuthenticator(FTPAuthenticator authenticator)
    {
        this.authenticator = authenticator;
    }

    /**
     * Checks if IP v6 is enabled.
     * 
     * @return <code>true</code> if IP v6 is enabled
     */
    public boolean getIpv6Enabled()
    {
        return ipv6Enabled;
    }

    /**
     * Indicates whether IP v6 should be enabled.
     * 
     * @param ipv6Enabled
     *            <code>true</code> if IP v6 should be enabled
     */
    public void setIpv6Enabled(boolean ipv6Enabled)
    {
        this.ipv6Enabled = ipv6Enabled;
    }
    
    /**
     * Return the data port range from port
     * 
     * @return int
     */
    public int getDataPortFrom() {
    	return dataPortFrom;
    }
    
    /**
     * Set the data port range from port
     * 
     * @param fromPort int
     */
    public void setDataPortFrom(int fromPort) {
    	dataPortFrom = fromPort;
    }
    
    /**
     * Return the data port to range port
     * 
     * @return int
     */
    public int getDataPortTo() {
    	return dataPortTo;
    }
    
    /**
     * Set the data port range to port
     * 
     * @param toPort int
     */
    public void setDataPortTo(int toPort) {
    	dataPortTo = toPort;
    }
}
