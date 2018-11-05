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

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.AbstractServerConfigurationBean;
import org.alfresco.filesys.alfresco.AlfrescoContext;
import org.alfresco.filesys.alfresco.ExtendedDiskInterface;
import org.alfresco.filesys.config.acl.AccessControlListBean;
import org.alfresco.filesys.repo.BufferedContentDiskDriver;
import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.filesys.repo.ContentDiskDriver2;
import org.alfresco.filesys.repo.LegacyFileStateDriver;
import org.alfresco.filesys.repo.NonTransactionalRuleContentDiskDriver;
import org.alfresco.jlan.ftp.FTPAuthenticator;
import org.alfresco.jlan.ftp.FTPConfigSection;
import org.alfresco.jlan.ftp.FTPPath;
import org.alfresco.jlan.ftp.InvalidPathException;
import org.alfresco.jlan.server.auth.acl.AccessControlList;
import org.alfresco.jlan.server.auth.passthru.DomainMapping;
import org.alfresco.jlan.server.auth.passthru.RangeDomainMapping;
import org.alfresco.jlan.server.auth.passthru.SubnetDomainMapping;
import org.alfresco.jlan.server.config.CoreServerConfigSection;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.SecurityConfigSection;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.core.ShareMapper;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FilesystemsConfigSection;
import org.alfresco.jlan.server.filesys.cache.FileStateLockManager;
import org.alfresco.jlan.server.filesys.cache.StandaloneFileStateCache;
import org.alfresco.jlan.server.thread.ThreadRequestPool;
import org.alfresco.jlan.util.IPAddress;
import org.alfresco.jlan.util.MemorySize;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.extensions.config.element.GenericConfigElement;


/**
 * Alfresco File Server Configuration Bean Class
 * <p>
 * Acts as an adaptor between JLAN's configuration requirements and the spring configuration of
 * the Alfresco filesystem subsystem.
 * <p>
 * Also contains an amount of initialisation logic. 
 * 
 * @author gkspencer
 * @author dward
 * @author mrogers
 */
public class ServerConfigurationBean extends AbstractServerConfigurationBean implements DisposableBean
{
    private FTPConfigBean ftpConfigBean;
    private List<DeviceContext> filesystemContexts;
    private SecurityConfigBean securityConfigBean;
    private CoreServerConfigBean coreServerConfigBean;

    private ThreadRequestPool threadPool;

    /**
     * Default constructor
     */
    public ServerConfigurationBean()
    {
        super("");
    }

    /**
     * Class constructor
     * 
     * @param srvName
     *            String
     */
    public ServerConfigurationBean(String srvName)
    {
        super(srvName);
    }

    public void setFtpConfigBean(FTPConfigBean ftpConfigBean)
    {
        this.ftpConfigBean = ftpConfigBean;
    }
    
    public void setFilesystemContexts(List<DeviceContext> filesystemContexts)
    {
        this.filesystemContexts = filesystemContexts;
    }

    public void setSecurityConfigBean(SecurityConfigBean securityConfigBean)
    {
        this.securityConfigBean = securityConfigBean;
    }

    public void setCoreServerConfigBean(CoreServerConfigBean coreServerConfigBean)
    {
        this.coreServerConfigBean = coreServerConfigBean;
    }

    /**
     * Process the FTP server configuration
     */
    protected void processFTPServerConfig()
    {
        // If the configuration section is not valid then FTP is disabled

        if (ftpConfigBean == null)
        {
            removeConfigSection(FTPConfigSection.SectionName);
            return;
        }

        // Check if the server has been disabled

        if (!ftpConfigBean.getServerEnabled())
        {
            removeConfigSection(FTPConfigSection.SectionName);
            return;
        }

        // Create the FTP configuration section

        FTPConfigSection ftpConfig = new FTPConfigSection(this);

        try
        {
            // Check for a bind address

            String bindText = ftpConfigBean.getBindTo();
            if (bindText != null && bindText.length() > 0 && !bindText.equals(BIND_TO_IGNORE))
            {

                // Validate the bind address

                try
                {

                    // Check the bind address

                    InetAddress bindAddr = InetAddress.getByName(bindText);

                    // Set the bind address for the FTP server

                    ftpConfig.setFTPBindAddress(bindAddr);
                }
                catch (UnknownHostException ex)
                {
                    throw new AlfrescoRuntimeException("Unable to find FTP bindto address, " + bindText, ex);
                }
            }

            // Check for an FTP server port

            Integer port = ftpConfigBean.getPort();
            if (port != null)
            {
                ftpConfig.setFTPPort(port);
                if (ftpConfig.getFTPPort() <= 0 || ftpConfig.getFTPPort() >= 65535)
                    throw new AlfrescoRuntimeException("FTP server port out of valid range");
            }
            else
            {

                // Use the default FTP port

                ftpConfig.setFTPPort(DefaultFTPServerPort);
            }

            // Check for an FTP server timeout for connection to client
            Integer sessionTimeout = ftpConfigBean.getSessionTimeout();
            if (sessionTimeout != null)
            {
                ftpConfig.setFTPSrvSessionTimeout(sessionTimeout);
                if (ftpConfig.getFTPSrvSessionTimeout() < 0)
                    throw new AlfrescoRuntimeException("FTP server session timeout must have positive value or zero");
            }
            else
            {

                // Use the default timeout

                ftpConfig.setFTPSrvSessionTimeout(DefaultFTPSrvSessionTimeout);
            }

            // Check if anonymous login is allowed

            if (ftpConfigBean.getAllowAnonymous())
            {

                // Enable anonymous login to the FTP server

                ftpConfig.setAllowAnonymousFTP(true);

                // Check if an anonymous account has been specified

                String anonAcc = ftpConfigBean.getAnonymousAccount();
                if (anonAcc != null && anonAcc.length() > 0)
                {

                    // Set the anonymous account name

                    ftpConfig.setAnonymousFTPAccount(anonAcc);

                    // Check if the anonymous account name is valid

                    if (ftpConfig.getAnonymousFTPAccount() == null || ftpConfig.getAnonymousFTPAccount().length() == 0)
                    {
                        throw new AlfrescoRuntimeException("Anonymous FTP account invalid");
                    }
                }
                else
                {

                    // Use the default anonymous account name

                    ftpConfig.setAnonymousFTPAccount(DefaultFTPAnonymousAccount);
                }
            }
            else
            {

                // Disable anonymous logins

                ftpConfig.setAllowAnonymousFTP(false);
            }

            // Check if a root path has been specified

            String rootPath = ftpConfigBean.getRootDirectory();
            if (rootPath != null && rootPath.length() > 0)
            {
                try
                {

                    // Parse the path

                    new FTPPath(rootPath);

                    // Set the root path

                    ftpConfig.setFTPRootPath(rootPath);
                }
                catch (InvalidPathException ex)
                {
                    throw new AlfrescoRuntimeException("Invalid FTP root directory, " + rootPath);
                }
            }

            // Check for FTP debug flags

            String flags = ftpConfigBean.getDebugFlags();
            int ftpDbg = 0;

            if (flags != null)
            {

                // Parse the flags

                flags = flags.toUpperCase();
                StringTokenizer token = new StringTokenizer(flags, ",");

                while (token.hasMoreTokens())
                {

                    // Get the current debug flag token

                    String dbg = token.nextToken().trim();

                    // Find the debug flag name

                    int idx = 0;

                    while (idx < m_ftpDebugStr.length && m_ftpDebugStr[idx].equalsIgnoreCase(dbg) == false)
                        idx++;

                    if (idx >= m_ftpDebugStr.length)
                        throw new AlfrescoRuntimeException("Invalid FTP debug flag, " + dbg);

                    // Set the debug flag

                    ftpDbg += 1 << idx;
                }

                // Set the FTP debug flags

                ftpConfig.setFTPDebug(ftpDbg);
            }

            // Check if a character set has been specified

            String charSet = ftpConfigBean.getCharSet();
            if (charSet != null && charSet.length() > 0)
            {

                try
                {

                    // Validate the character set name

                    Charset.forName(charSet);

                    // Set the FTP character set

                    ftpConfig.setFTPCharacterSet(charSet);
                }
                catch (IllegalCharsetNameException ex)
                {
                    throw new AlfrescoRuntimeException("FTP Illegal character set name, " + charSet);
                }
                catch (UnsupportedCharsetException ex)
                {
                    throw new AlfrescoRuntimeException("FTP Unsupported character set name, " + charSet);
                }
            }

            // Check if an authenticator has been specified

            FTPAuthenticator auth = ftpConfigBean.getAuthenticator();
            if (auth != null)
            {

                // Initialize and set the authenticator class

                ftpConfig.setAuthenticator(auth);
            }
            else
                throw new AlfrescoRuntimeException("FTP authenticator not specified");

            // Check if a data port range has been specified
            
            if ( ftpConfigBean.getDataPortFrom() != 0 && ftpConfigBean.getDataPortTo() != 0) {
            	
            	// Range check the data port values
            	
            	int rangeFrom = ftpConfigBean.getDataPortFrom();
            	int rangeTo   = ftpConfigBean.getDataPortTo();
            	
            	if ( rangeFrom != 0 && rangeTo != 0) {
            		
            		// Validate the FTP data port range
            	
	    			if ( rangeFrom < 1024 || rangeFrom > 65535)
	    				throw new InvalidConfigurationException("Invalid FTP data port range from value, " + rangeFrom);
	
	    			if ( rangeTo < 1024 || rangeTo > 65535)
	    				throw new InvalidConfigurationException("Invalid FTP data port range to value, " + rangeTo);
	
	    			if ( rangeFrom >= rangeTo)
	    				throw new InvalidConfigurationException("Invalid FTP data port range, " + rangeFrom + "-" + rangeTo);
	
	    			// Set the FTP data port range
	
	    			ftpConfig.setFTPDataPortLow(rangeFrom);
	    			ftpConfig.setFTPDataPortHigh(rangeTo);
	    			
	    			// Log the data port range
	    			
	    			logger.info("FTP server data ports restricted to range " + rangeFrom + ":" + rangeTo);
            	}
            }

            // Check for an external address
            String ExtAddr = ftpConfigBean.getExternalAddress();
            if (ExtAddr != null && ExtAddr.length() > 0)
            {
                ftpConfig.setFTPExternalAddress(ExtAddr);
            }

            // FTPS parameter parsing
    		//
    		// Check if a key store path has been specified
    		
    		if ( ftpConfigBean.getKeyStorePath() != null && ftpConfigBean.getKeyStorePath().length() > 0) {

    			// Get the path to the key store, check that the file exists

    			String keyStorePath = ftpConfigBean.getKeyStorePath();
    			File keyStoreFile = new File( keyStorePath);
    			
    			if ( keyStoreFile.exists() == false)
    				throw new InvalidConfigurationException("FTPS key store file does not exist, " + keyStorePath);
    			else if ( keyStoreFile.isDirectory())
    				throw new InvalidConfigurationException("FTPS key store path is a directory, " + keyStorePath);
    			
    			// Set the key store path
    			
    			ftpConfig.setKeyStorePath( keyStorePath);
    		
	    		// Check if the key store type has been specified
	    		
	    		if ( ftpConfigBean.getKeyStoreType() != null && ftpConfigBean.getKeyStoreType().length() > 0) {
	    			
	    			// Get the key store type, and validate
	    			
	    			String keyStoreType = ftpConfigBean.getKeyStoreType();
	    			
	    			if ( keyStoreType == null || keyStoreType.length() == 0)
	    				throw new InvalidConfigurationException("FTPS key store type is invalid");
	    			
	    			try {
	    				KeyStore.getInstance( keyStoreType);
	    			}
	    			catch ( KeyStoreException ex) {
	    				throw new InvalidConfigurationException("FTPS key store type is invalid, " + keyStoreType, ex);
	    			}
	    			
	    			// Set the key store type
	    			
	    			ftpConfig.setKeyStoreType( keyStoreType);
	    		}

	    		// Check if the key store passphrase has been specified
	    		
	    		if ( ftpConfigBean.getKeyStorePassphrase() != null && ftpConfigBean.getKeyStorePassphrase().length() > 0) {

	    			// Set the key store passphrase
	    			
	    			ftpConfig.setKeyStorePassphrase( ftpConfigBean.getKeyStorePassphrase());
	    		}
    		}
    		
    		// Check if the trust store path has been specified
    		
    		if ( ftpConfigBean.getTrustStorePath() != null && ftpConfigBean.getTrustStorePath().length() > 0) {

    			// Get the path to the trust store, check that the file exists
    			
    			String trustStorePath = ftpConfigBean.getTrustStorePath();
    			File trustStoreFile = new File( trustStorePath);
    			
    			if ( trustStoreFile.exists() == false)
    				throw new InvalidConfigurationException("FTPS trust store file does not exist, " + trustStorePath);
    			else if ( trustStoreFile.isDirectory())
    				throw new InvalidConfigurationException("FTPS trust store path is a directory, " + trustStorePath);
    			
    			// Set the trust store path
    			
    			ftpConfig.setTrustStorePath( trustStorePath);

	    		// Check if the trust store type has been specified
	    		
	    		if ( ftpConfigBean.getTrustStoreType() != null && ftpConfigBean.getTrustStoreType().length() > 0) {
	    			
	    			// Get the trust store type, and validate
	    			
	    			String trustStoreType = ftpConfigBean.getTrustStoreType();
	    			
	    			if ( trustStoreType == null || trustStoreType.length() == 0)
	    				throw new InvalidConfigurationException("FTPS trust store type is invalid");
	    			
	    			try {
	    				KeyStore.getInstance( trustStoreType);
	    			}
	    			catch ( KeyStoreException ex) {
	    				throw new InvalidConfigurationException("FTPS trust store type is invalid, " + trustStoreType, ex);
	    			}
	    			
	    			// Set the trust store type
	    			
	    			ftpConfig.setTrustStoreType( trustStoreType);
	    		}
    		
	    		// Check if the trust store passphrase has been specified
	    		
	    		if ( ftpConfigBean.getTrustStorePassphrase() != null && ftpConfigBean.getTrustStorePassphrase().length() > 0) {
	
	    			// Set the trust store passphrase
	    			
	    			ftpConfig.setTrustStorePassphrase( ftpConfigBean.getTrustStorePassphrase());
	    		}
    		}
    		
    		// Check if only secure sessions should be allowed to logon
    		
    		if ( ftpConfigBean.hasRequireSecureSession()) {

    			// Only allow secure sessions to logon to the FTP server

    			ftpConfig.setRequireSecureSession( true);
    		}
    		
    		// Check that all the required FTPS parameters have been set
    		// MNT-7301 FTPS server requires unnecessarly to have a trustStore while a keyStore should be sufficient
    		if ( ftpConfig.getKeyStorePath() != null) {
    			
    			// Make sure all parameters are set
    			
    			if ( ftpConfig.getKeyStorePath() == null)
    				throw new InvalidConfigurationException("FTPS configuration requires keyStore to be set");
    		}
    		
    		// Check if SSLEngine debug output should be enabled
    		
    		if ( ftpConfigBean.hasSslEngineDebug()) {

    			// Enable SSLEngine debug output

    			System.setProperty("javax.net.debug", "ssl,handshake");
    		}
        }
        catch (InvalidConfigurationException ex)
        {
            throw new AlfrescoRuntimeException(ex.getMessage());
        }
    }

    /**
     * Process the filesystems configuration
     */
    protected void processFilesystemsConfig()
    {
        // Create the filesystems configuration section

        FilesystemsConfigSection fsysConfig = new FilesystemsConfigSection(this);

        // Access the security configuration section

        SecurityConfigSection secConfig = (SecurityConfigSection) getConfigSection(SecurityConfigSection.SectionName);

        // Process the filesystems list

        if (this.filesystemContexts != null)
        {

            // Add the filesystems

            for (DeviceContext filesystem : this.filesystemContexts)
            {

                // Get the current filesystem configuration

                try
                {
                    // Check the filesystem type and use the appropriate driver

                    DiskSharedDevice filesys = null;

                    // Create a new filesystem driver instance and register a context for
                    // the new filesystem

                    ExtendedDiskInterface filesysDriver = getRepoDiskInterface();
                    ContentContext filesysContext = (ContentContext) filesystem;

                    // Create state cache here and inject
                    StandaloneFileStateCache standaloneCache = new StandaloneFileStateCache();
                    standaloneCache.initializeCache( new GenericConfigElement( ""), this);
                    filesysContext.setStateCache(standaloneCache);
                    
                    if ( filesysContext.hasStateCache()) {
                        
                        // Register the state cache with the reaper thread
                        // has many side effects including initialisation of the cache    
                        fsysConfig.addFileStateCache( filesystem.getDeviceName(), filesysContext.getStateCache());
                        
                        // Create the lock manager for the context.
                        FileStateLockManager lockMgr = new FileStateLockManager(filesysContext.getStateCache());
                        filesysContext.setLockManager(lockMgr); 
                        filesysContext.setOpLockManager(lockMgr);
                    }
                    
                    if (!ftpConfigBean.getServerEnabled() && isContentDiskDriver2(filesysDriver))
                    {
                        ((ContentContext) filesystem).setDisableNodeMonitor(true);
                    }
                    
                    filesysDriver.registerContext(filesystem);

                    // Check if an access control list has been specified

                    AccessControlList acls = null;
                    AccessControlListBean accessControls = filesysContext.getAccessControlList();
                    if (accessControls != null)
                    {
                        // Parse the access control list
                        acls = accessControls.toAccessControlList(secConfig);
                    }
                    else if (secConfig.hasGlobalAccessControls())
                    {

                        // Use the global access control list for this disk share
                        acls = secConfig.getGlobalAccessControls();
                    }

                    // Create the shared filesystem

                    filesys = new DiskSharedDevice(filesystem.getDeviceName(), filesysDriver, filesysContext);
                    filesys.setConfiguration( this);

                    // Add any access controls to the share

                    filesys.setAccessControlList(acls);


                    
                    // Check if change notifications should be enabled
                    
                    if ( filesysContext.getDisableChangeNotifications() == false)
                        filesysContext.enableChangeHandler( true);
                    
                    // Start the filesystem

                    filesysContext.startFilesystem(filesys);

                    // Add the new filesystem

                    fsysConfig.addShare(filesys);
                }
                catch (DeviceContextException ex)
                {
                    throw new AlfrescoRuntimeException("Error creating filesystem " + filesystem.getDeviceName(), ex);
                }
                catch (InvalidConfigurationException ex)
                {
                    throw new AlfrescoRuntimeException(ex.getMessage(), ex);
                }
            }
        }
        else
        {
            // No filesystems defined

            logger.warn("No filesystems defined");
        }

        // home folder share mapper could be declared in security config
    }

    /**
     * Returns true if either: the disk interface is a ContentDiskDriver2; or
     * the disk interface is a {@link BufferedContentDiskDriver} and its disk
     * interface is a ContentDiskDriver2 (wrapped by several other DiskInterface objects).
     * 
     * @param diskInterface ExtendedDiskInterface
     * @return boolean
     */
    private boolean isContentDiskDriver2(ExtendedDiskInterface diskInterface)
    {
        if (diskInterface instanceof ContentDiskDriver2)
        {
            return true;
        }
        if (diskInterface instanceof BufferedContentDiskDriver)
        {
            BufferedContentDiskDriver bufferedDriver = (BufferedContentDiskDriver) diskInterface;
            ExtendedDiskInterface underlyingDriver = bufferedDriver.getDiskInterface();
            
            if (underlyingDriver instanceof LegacyFileStateDriver)
            {
                LegacyFileStateDriver legacyDriver = (LegacyFileStateDriver) underlyingDriver;
                underlyingDriver = legacyDriver.getDiskInterface();
                
                if (underlyingDriver instanceof NonTransactionalRuleContentDiskDriver)
                {
                    // This is the best we can do. The underlying driver of this driver (the
                    // NonTransactionalRuleContentDiskDriver) is a dynamic proxy and we can't
                    // say for sure if it is a ContentDiskDriver2.
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Process the security configuration
     */
    protected void processSecurityConfig()
    {
        // Create the security configuration section

        SecurityConfigSection secConfig = new SecurityConfigSection(this);

        try
        {
            // Check if global access controls have been specified

            AccessControlListBean accessControls = securityConfigBean.getGlobalAccessControl();

            if (accessControls != null)
            {
                // Parse the access control list
                AccessControlList acls = accessControls.toAccessControlList(secConfig);
                if (acls != null)
                    secConfig.setGlobalAccessControls(acls);
            }
           

            // Check if a JCE provider class has been specified
            
            String jceProvider = securityConfigBean.getJCEProvider();
            if (jceProvider != null && jceProvider.length() > 0)
            {

                // Set the JCE provider

                secConfig.setJCEProvider(jceProvider);
            }
            else
            {
                // Use the default Bouncy Castle JCE provider

                secConfig.setJCEProvider("org.bouncycastle.jce.provider.BouncyCastleProvider");
            }

            // Check if a share mapper has been specified

            ShareMapper shareMapper = securityConfigBean.getShareMapper();
            if (shareMapper != null)
            {
                // Associate the share mapper
                secConfig.setShareMapper(shareMapper);
            }
     
            // Check if any domain mappings have been specified

            List<DomainMappingConfigBean> mappings = securityConfigBean.getDomainMappings();
            if (mappings != null)
            {
                DomainMapping mapping = null;

                for (DomainMappingConfigBean domainMap : mappings)
                {
                    // Get the domain name

                    String name = domainMap.getName();

                    // Check if the domain is specified by subnet or range

                    String subnetStr = domainMap.getSubnet();
                    String rangeFromStr;
                    if (subnetStr != null && subnetStr.length() > 0)
                    {
                        String maskStr = domainMap.getMask();

                        // Parse the subnet and mask, to validate and convert to int values

                        int subnet = IPAddress.parseNumericAddress(subnetStr);
                        int mask = IPAddress.parseNumericAddress(maskStr);

                        if (subnet == 0 || mask == 0)
                            throw new AlfrescoRuntimeException("Invalid subnet/mask for domain mapping " + name);

                        // Create the subnet domain mapping

                        mapping = new SubnetDomainMapping(name, subnet, mask);
                    }
                    else if ((rangeFromStr = domainMap.getRangeFrom()) != null && rangeFromStr.length() > 0)
                    {
                        String rangeToStr = domainMap.getRangeTo();

                        // Parse the range from/to values and convert to int values

                        int rangeFrom = IPAddress.parseNumericAddress(rangeFromStr);
                        int rangeTo = IPAddress.parseNumericAddress(rangeToStr);

                        if (rangeFrom == 0 || rangeTo == 0)
                            throw new AlfrescoRuntimeException("Invalid address range domain mapping " + name);

                        // Create the subnet domain mapping

                        mapping = new RangeDomainMapping(name, rangeFrom, rangeTo);
                    }
                    else
                        throw new AlfrescoRuntimeException("Invalid domain mapping specified");

                    // Add the domain mapping

                    secConfig.addDomainMapping(mapping);
                }
            }
        }
        catch (InvalidConfigurationException ex)
        {
            throw new AlfrescoRuntimeException(ex.getMessage());
        }
    }

    /**
     * Process the core server configuration
     * 
     * @exception InvalidConfigurationException
     */
    protected void processCoreServerConfig() throws InvalidConfigurationException
    {
        // Create the core server configuration section

        CoreServerConfigSection coreConfig = new CoreServerConfigSection(this);
    	
        // Check if the server core element has been specified

        if (coreServerConfigBean == null)
        {

            // Configure a default memory pool

            coreConfig.setMemoryPool(DefaultMemoryPoolBufSizes, DefaultMemoryPoolInitAlloc, DefaultMemoryPoolMaxAlloc);

            // Configure a default thread pool size

            coreConfig.setThreadPool(DefaultThreadPoolInit, DefaultThreadPoolMax);

            threadPool = coreConfig.getThreadPool();

            return;
        }

        // Check if the thread pool size has been specified

        Integer initSize = coreServerConfigBean.getThreadPoolInit();
        if (initSize == null)
        {
            initSize = DefaultThreadPoolInit;
        }
        Integer maxSize = coreServerConfigBean.getThreadPoolMax();
        if (maxSize == null)
        {
            maxSize = DefaultThreadPoolMax;
        }

        // Range check the thread pool size

        if (initSize < ThreadRequestPool.MinimumWorkerThreads)
            throw new InvalidConfigurationException("Thread pool size below minimum allowed size");

        if (initSize > ThreadRequestPool.MaximumWorkerThreads)
            throw new InvalidConfigurationException("Thread pool size above maximum allowed size");

        // Range check the maximum thread pool size

        if (maxSize < ThreadRequestPool.MinimumWorkerThreads)
            throw new InvalidConfigurationException("Thread pool maximum size below minimum allowed size");

        if (maxSize > ThreadRequestPool.MaximumWorkerThreads)
            throw new InvalidConfigurationException("Thread pool maximum size above maximum allowed size");

        if (maxSize < initSize)
            throw new InvalidConfigurationException("Initial size is larger than maxmimum size");        

        // Configure the thread pool

        coreConfig.setThreadPool(initSize, maxSize);

        threadPool = coreConfig.getThreadPool();

        // Check if thread pool debug output is enabled

        if (coreServerConfigBean.getThreadPoolDebug())
            coreConfig.getThreadPool().setDebug(true);

        // Check if the packet sizes/allocations have been specified

        List<MemoryPacketConfigBean> packetSizes = coreServerConfigBean.getMemoryPacketSizes();
        if (packetSizes != null)
        {

            // Calculate the array size for the packet size/allocation arrays

            int elemCnt = packetSizes.size();

            // Create the packet size, initial allocation and maximum allocation arrays

            int[] pktSizes = new int[elemCnt];
            int[] initSizes = new int[elemCnt];
            int[] maxSizes = new int[elemCnt];

            int elemIdx = 0;

            // Process the packet size elements
            for (MemoryPacketConfigBean curChild : packetSizes)
            {

                // Get the packet size

                int pktSize = -1;

                Long pktSizeLong = curChild.getSize();
                if (pktSizeLong == null)
                    throw new InvalidConfigurationException("Memory pool packet size not specified");

                // Parse the packet size

                try
                {
                    pktSize = MemorySize.getByteValueInt(pktSizeLong.toString());
                }
                catch (NumberFormatException ex)
                {
                    throw new InvalidConfigurationException("Memory pool packet size, invalid size value, "
                            + pktSizeLong);
                }

                // Make sure the packet sizes have been specified in ascending order

                if (elemIdx > 0 && pktSizes[elemIdx - 1] >= pktSize)
                    throw new InvalidConfigurationException(
                            "Invalid packet size specified, less than/equal to previous packet size");

                // Get the initial allocation for the current packet size
                Integer initAlloc = curChild.getInit();
                if (initAlloc == null)
                    throw new InvalidConfigurationException("Memory pool initial allocation not specified");

                // Range check the initial allocation

                if (initAlloc < MemoryPoolMinimumAllocation)
                    throw new InvalidConfigurationException("Initial memory pool allocation below minimum of "
                            + MemoryPoolMinimumAllocation);

                if (initAlloc > MemoryPoolMaximumAllocation)
                    throw new InvalidConfigurationException("Initial memory pool allocation above maximum of "
                            + MemoryPoolMaximumAllocation);

                // Get the maximum allocation for the current packet size

                Integer maxAlloc = curChild.getMax();
                if (maxAlloc == null)
                    throw new InvalidConfigurationException("Memory pool maximum allocation not specified");

               // Range check the maximum allocation

                if (maxAlloc < MemoryPoolMinimumAllocation)
                    throw new InvalidConfigurationException("Maximum memory pool allocation below minimum of "
                            + MemoryPoolMinimumAllocation);

                if (initAlloc > MemoryPoolMaximumAllocation)
                    throw new InvalidConfigurationException("Maximum memory pool allocation above maximum of "
                            + MemoryPoolMaximumAllocation);

                // Set the current packet size elements

                pktSizes[elemIdx] = pktSize;
                initSizes[elemIdx] = initAlloc;
                maxSizes[elemIdx] = maxAlloc;

                elemIdx++;
            }

            // Check if all elements were used in the packet size/allocation arrays

            if (elemIdx < pktSizes.length)
            {

                // Re-allocate the packet size/allocation arrays

                int[] newPktSizes = new int[elemIdx];
                int[] newInitSizes = new int[elemIdx];
                int[] newMaxSizes = new int[elemIdx];

                // Copy the values to the shorter arrays

                System.arraycopy(pktSizes, 0, newPktSizes, 0, elemIdx);
                System.arraycopy(initSizes, 0, newInitSizes, 0, elemIdx);
                System.arraycopy(maxSizes, 0, newMaxSizes, 0, elemIdx);

                // Move the new arrays into place

                pktSizes = newPktSizes;
                initSizes = newInitSizes;
                maxSizes = newMaxSizes;
            }

            // Configure the memory pool

            coreConfig.setMemoryPool(pktSizes, initSizes, maxSizes);
        }
        else
        {

            // Configure a default memory pool

            coreConfig.setMemoryPool(DefaultMemoryPoolBufSizes, DefaultMemoryPoolInitAlloc, DefaultMemoryPoolMaxAlloc);
        }
    }
    
    /**
     * Initialise a runtime context - not configured through spring e.g MT.
     * 
     * TODO - what about desktop actions etc?
     * 
     * @param uniqueName String
     * @param diskCtx AlfrescoContext
     */
    public void initialiseRuntimeContext(String uniqueName, AlfrescoContext diskCtx)
    {
        logger.debug("initialiseRuntimeContext" + diskCtx);
        
        if (diskCtx.getStateCache() == null) {
          
          // Set the state cache, use a hard coded standalone cache for now
          FilesystemsConfigSection filesysConfig = (FilesystemsConfigSection) this.getConfigSection( FilesystemsConfigSection.SectionName);
  
          if ( filesysConfig != null) 
          {
              
              try 
              {
                  // Create a standalone state cache
                  StandaloneFileStateCache standaloneCache = new StandaloneFileStateCache();
                  standaloneCache.initializeCache( new GenericConfigElement( ""), this);
                  filesysConfig.addFileStateCache( diskCtx.getDeviceName(), standaloneCache);
                  diskCtx.setStateCache( standaloneCache);
                    
                  // Register the state cache with the reaper thread
                  // has many side effects including initialisation of the cache    
                 filesysConfig.addFileStateCache( diskCtx.getShareName(), diskCtx.getStateCache());
                       
                 FileStateLockManager lockMgr = new FileStateLockManager(diskCtx.getStateCache());
                 diskCtx.setLockManager(lockMgr); 
                 diskCtx.setOpLockManager(lockMgr); 
              }
              catch ( InvalidConfigurationException ex) 
              {
                  throw new AlfrescoRuntimeException( "Failed to initialize standalone state cache for " + diskCtx.getDeviceName());
              }
          }
      }
    }

    @Override
    public void destroy() throws Exception
    {
        if (threadPool != null)
        {
            threadPool.shutdownThreadPool();
            threadPool = null;
        }
    }

}
