/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
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

    private Integer sessionTimeout;

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

    // Data port range

    private int dataPortFrom;
    private int dataPortTo;

    // Externally seen IP address
    private String externalAddress;

    // FTPS configuration
    //
    // Keystore/truststore details

    private String m_keyStorePath;
    private String m_keyStoreType;
    private String m_keyStorePass;

    private String m_trustStorePath;
    private String m_trustStoreType;
    private String m_trustStorePass;

    // Only allow FTPS/encrypted session logons

    private boolean m_requireSecureSess;

    // SSL engine debug enable

    private boolean m_sslDebug;

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
     * Sets the timeout for socket session.
     * 
     * @param sessionTimeout
     *            the new timeout
     */
    public void setSessionTimeout(Integer sessionTimeout)
    {
        this.sessionTimeout = sessionTimeout;
    }

    /**
     * Gets the sesion timeout
     * 
     * @return Integer
     */
    public Integer getSessionTimeout()
    {
        return sessionTimeout;
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
     * Return the data port range from port
     * 
     * @return int
     */
    public int getDataPortFrom()
    {
        return dataPortFrom;
    }

    /**
     * Set the data port range from port
     * 
     * @param fromPort
     *            int
     */
    public void setDataPortFrom(int fromPort)
    {
        dataPortFrom = fromPort;
    }

    /**
     * Return the data port to range port
     * 
     * @return int
     */
    public int getDataPortTo()
    {
        return dataPortTo;
    }

    /**
     * Set the data port range to port
     * 
     * @param toPort
     *            int
     */
    public void setDataPortTo(int toPort)
    {
        dataPortTo = toPort;
    }

    /**
     * Return the IP Address to use in NAT setup
     */
    public String getExternalAddress()
    {
        return externalAddress;
    }

    /**
     * Set the IP Address to use in NAT setup
     */
    public void setExternalAddress(String externalAddress)
    {
        this.externalAddress = externalAddress;
    }

    /**
     * Return the key store path
     * 
     * @return String
     */
    public final String getKeyStorePath()
    {
        return m_keyStorePath;
    }

    /**
     * Return the key store type
     * 
     * @return String
     */
    public final String getKeyStoreType()
    {
        return m_keyStoreType;
    }

    /**
     * Return the trust store path
     * 
     * @return String
     */
    public final String getTrustStorePath()
    {
        return m_trustStorePath;
    }

    /**
     * Return the trust store type
     * 
     * @return String
     */
    public final String getTrustStoreType()
    {
        return m_trustStoreType;
    }

    /**
     * Return the passphrase for the key store
     * 
     * @return String
     */
    public final String getKeyStorePassphrase()
    {
        return m_keyStorePass;
    }

    /**
     * Return the passphrase for the trust store
     * 
     * @return String
     */
    public final String getTrustStorePassphrase()
    {
        return m_trustStorePass;
    }

    /**
     * Determine if only secure sessions will be allowed to logon
     * 
     * @return boolean
     */
    public final boolean hasRequireSecureSession()
    {
        return m_requireSecureSess;
    }

    /**
     * Set/clear the require secure sessions flag
     * 
     * @param reqSec
     *            boolean
     */
    public final void setRequireSecureSession(boolean reqSec)
    {
        m_requireSecureSess = reqSec;
    }

    /**
     * Set the key store path
     * 
     * @param path
     *            String
     */
    public final void setKeyStorePath(String path)
    {
        m_keyStorePath = path;
    }

    /**
     * Set the key store type
     * 
     * @param typ
     *            String
     */
    public final void setKeyStoreType(String typ)
    {
        m_keyStoreType = typ;
    }

    /**
     * Set the trust store path
     * 
     * @param path
     *            String
     */
    public final void setTrustStorePath(String path)
    {
        m_trustStorePath = path;
    }

    /**
     * Set the trust store type
     * 
     * @param typ
     *            String
     */
    public final void setTrustStoreType(String typ)
    {
        m_trustStoreType = typ;
    }

    /**
     * Set the key store passphrase
     * 
     * @param phrase
     *            String
     */
    public final void setKeyStorePassphrase(String phrase)
    {
        m_keyStorePass = phrase;
    }

    /**
     * Set the trust store passphrase
     * 
     * @param phrase
     *            String
     */
    public final void setTrustStorePassphrase(String phrase)
    {
        m_trustStorePass = phrase;
    }

    /**
     * Check if SSLEngine debug output should be enabled
     * 
     * @return boolean
     */
    public final boolean hasSslEngineDebug()
    {
        return m_sslDebug;
    }

    /**
     * Enable SSLEngine class debug output
     * 
     * @param sslDebug
     *            boolean
     */
    public final void setSslEngineDebug(boolean sslDebug)
    {
        m_sslDebug = sslDebug;
    }
}
