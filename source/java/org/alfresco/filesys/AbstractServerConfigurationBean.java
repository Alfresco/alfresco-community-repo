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
package org.alfresco.filesys;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Locale;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.alfresco.AlfrescoClientInfoFactory;
import org.alfresco.filesys.alfresco.ExtendedDiskInterface;
import org.alfresco.jlan.debug.DebugConfigSection;
import org.alfresco.jlan.ftp.FTPConfigSection;
import org.alfresco.jlan.netbios.NetBIOSName;
import org.alfresco.jlan.netbios.NetBIOSNameList;
import org.alfresco.jlan.netbios.NetBIOSSession;
import org.alfresco.jlan.netbios.win32.Win32NetBIOS;
import org.alfresco.jlan.oncrpc.nfs.NFSConfigSection;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.config.GlobalConfigSection;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.smb.server.CIFSConfigSection;
import org.alfresco.jlan.util.IPAddress;
import org.alfresco.jlan.util.Platform;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.extensions.config.element.GenericConfigElement;

/**
 * Alfresco File Server Configuration Bean Class
 * 
 * @author gkspencer
 */
public abstract class AbstractServerConfigurationBean extends ServerConfiguration implements
        ExtendedServerConfigurationAccessor, ApplicationListener, ApplicationContextAware
{

  // Debug logging

  protected static final Log logger = LogFactory.getLog("org.alfresco.fileserver");

  // IP address representing null
  
  public static final String BIND_TO_IGNORE = "0.0.0.0";
  
  // SMB/CIFS session debug type strings
  //
  // Must match the bit mask order
  
  protected static final String m_sessDbgStr[] = { "NETBIOS", "STATE", "RXDATA", "TXDATA", "DUMPDATA", "NEGOTIATE", "TREE", "SEARCH", "INFO", "FILE",
          "FILEIO", "TRANSACT", "ECHO", "ERROR", "IPC", "LOCK", "PKTTYPE", "DCERPC", "STATECACHE", "TIMING", "NOTIFY",
          "STREAMS", "SOCKET", "PKTPOOL", "PKTSTATS", "THREADPOOL", "BENCHMARK", "OPLOCK" };

  // FTP server debug type strings

  protected static final String m_ftpDebugStr[] = { "STATE", "RXDATA", "TXDATA", "DUMPDATA", "SEARCH", "INFO", "FILE", "FILEIO", "ERROR", "PKTTYPE",
      "TIMING", "DATAPORT", "DIRECTORY", "SSL" };

  // Default FTP server port
  
  protected static final int DefaultFTPServerPort = 21;
  
  // Default FTP anonymous account name
  
  protected static final String DefaultFTPAnonymousAccount = "anonymous";
  
  //  NFS server debug type strings
  
  protected static final String m_nfsDebugStr[] = { "RXDATA", "TXDATA", "DUMPDATA", "SEARCH", "INFO", "FILE",
    "FILEIO", "ERROR", "TIMING", "DIRECTORY", "SESSION" };
  
  // Token name to substitute current server name into the CIFS server name
  
  protected static final String TokenLocalName = "${localname}";
  
  // Default thread pool size
	
  protected static final int DefaultThreadPoolInit	= 25;
  protected static final int DefaultThreadPoolMax		= 50;
	
  // Default memory pool settings
	
  protected static final int[] DefaultMemoryPoolBufSizes  = { 256, 4096, 16384, 66000 };
  protected static final int[] DefaultMemoryPoolInitAlloc = {  20,   20,     5,     5 };
  protected static final int[] DefaultMemoryPoolMaxAlloc  = { 100,   50,    50,    50 };
	
		
  // Memory pool allocation limits
	
  protected static final int MemoryPoolMinimumAllocation	= 5;
  protected static final int MemoryPoolMaximumAllocation   = 500;
	
  // Maximum session timeout
  
  protected static final int MaxSessionTimeout    = 60 * 60;  // 1 hour
    
  // Disk interface to use for shared filesystems
  
  private ExtendedDiskInterface m_repoDiskInterface;
  
  // AVM filesystem interface
  
  private ExtendedDiskInterface m_avmDiskInterface;
  
  // Runtime platform type
  
  private Platform.Type m_platform = Platform.Type.Unchecked;

  // flag to indicate successful initialization
  
  private boolean m_initialised;

  // Main authentication service, public API
  
  private AuthenticationService m_authenticationService;

  // Authentication component, for internal functions
  
  protected AuthenticationComponent m_authenticationComponent;
  
  // Various services
  
  private NodeService m_nodeService;
  private PersonService m_personService;
  private TransactionService m_transactionService;
  protected TenantService m_tenantService;
  private SearchService m_searchService;
  private NamespaceService m_namespaceService;
  private AuthorityService m_authorityService;
  
  // Local server name and domain/workgroup name

  private String m_localName;
  private String m_localNameFull;
  private String m_localDomain;
  
  // Disable use of native code on Windows, do not use any JNI calls
  
  protected boolean m_disableNativeCode = false;
  
  /**
   * Default constructor
   */
  public AbstractServerConfigurationBean()
  {
    super ( "");
  }
  
  /**
   * Class constructor
   * 
   * @param srvName String
   */
  public AbstractServerConfigurationBean( String srvName)
  {
      super( srvName);
  }
  
  /**
   * Set the authentication service
   * 
   * @param authenticationService AuthenticationService
   */
  public void setAuthenticationService(AuthenticationService authenticationService)
  {
      m_authenticationService = authenticationService;
  }

  /**
   * Set the filesystem driver for the node service based filesystem
   * 
   * @param diskInterface DiskInterface
   */
  public void setDiskInterface(ExtendedDiskInterface diskInterface)
  {
      m_repoDiskInterface = diskInterface;
  }

  /**
   * Set the filesystem driver for the AVM based filesystem
   * 
   */
  public void setAvmDiskInterface(ExtendedDiskInterface diskInterface)
  {
      m_avmDiskInterface = diskInterface;
  }
  
  /**
   * Set the authentication component
   * 
   * @param component AuthenticationComponent
   */
  public void setAuthenticationComponent(AuthenticationComponent component)
  {
      m_authenticationComponent = component;
  }

  /**
   * Set the node service
   * 
   * @param service NodeService
   */
  public void setNodeService(NodeService service)
  {
      m_nodeService = service;
  }

  /**
   * Set the person service
   * 
   * @param service PersonService
   */
  public void setPersonService(PersonService service)
  {
      m_personService = service;
  }

  /**
   * Set the transaction service
   * 
   * @param service TransactionService
   */
  public void setTransactionService(TransactionService service)
  {
      m_transactionService = service;
  }

  /**
   * Set the tenant service
   * 
   * @param tenantService TenantService
   */
  public void setTenantService(TenantService tenantService)
  {
	  m_tenantService = tenantService;
  }

  /**
   * Set the search service
   * 
   * @param searchService SearchService
   */
  public void setSearchService(SearchService searchService)
  {
	  m_searchService = searchService;
  }
  
  /**
   * Set the namespace service
   * 
   * @param namespaceService NamespaceService
   */
  public void setNamespaceService(NamespaceService namespaceService)
  {
	  m_namespaceService = namespaceService;
  }
  
  /**
   * Set the authority service
   * 
   * @param authService AuthorityService
   */
  public void setAuthorityService(AuthorityService authService)
  {
  	m_authorityService = authService;
  }
  
  /**
   * Check if the configuration has been initialized
   * 
   * @return Returns true if the configuration was fully initialised
   */
  public boolean isInitialised()
  {
      return m_initialised;
  }

  /**
   * Check if the SMB server is enabled
   * 
   * @return boolean
   */
  public final boolean isSMBServerEnabled()
  {
      return hasConfigSection( CIFSConfigSection.SectionName);
  }

  /**
   * Check if the FTP server is enabled
   * 
   * @return boolean
   */
  public final boolean isFTPServerEnabled()
  {
      return hasConfigSection( FTPConfigSection.SectionName);
  }

  /**
   * Check if the NFS server is enabled
   * 
   * @return boolean
   */
  public final boolean isNFSServerEnabled()
  {
      return hasConfigSection( NFSConfigSection.SectionName);
  }
  
  /**
   * Return the repository disk interface to be used to create shares
   * 
   * @return DiskInterface
   */
  public final ExtendedDiskInterface getRepoDiskInterface()
  {
      return m_repoDiskInterface;
  }
  
  /**
   * Return the disk interface to be used to create AVM filesystem shares
   * 
   * @return DiskInterface
   */
  public final ExtendedDiskInterface getAvmDiskInterface()
  {
      return m_avmDiskInterface;
  }
  
  /**
   * Initialize the configuration using the configuration service
   */
  public void init()
  {
      // Check that all required properties have been set
	  
      if (m_authenticationComponent == null)
      {
          throw new AlfrescoRuntimeException("Property 'authenticationComponent' not set");
      }
      else if (m_authenticationService == null)
      {
          throw new AlfrescoRuntimeException("Property 'authenticationService' not set");
      }
      else if (m_nodeService == null)
      {
          throw new AlfrescoRuntimeException("Property 'nodeService' not set");
      }
      else if (m_personService == null)
      {
          throw new AlfrescoRuntimeException("Property 'personService' not set");
      }
      else if (m_transactionService == null)
      {
          throw new AlfrescoRuntimeException("Property 'transactionService' not set");
      }
      else if (m_repoDiskInterface == null)
      {
          throw new AlfrescoRuntimeException("Property 'diskInterface' not set");
      }
      else if (m_authorityService == null)
      {
      	throw new AlfrescoRuntimeException("Property 'authorityService' not set");
      }
      
      // Set the platform type

      determinePlatformType();

      // Create the debug output configuration using a logger for all file server debug output
      
      DebugConfigSection debugConfig = new DebugConfigSection( this);
      try
      {
          debugConfig.setDebug("org.alfresco.filesys.debug.FileServerDebugInterface", new GenericConfigElement( "params"));
      }
      catch ( InvalidConfigurationException ex)
      {
      }
      
      // Create the global configuration and Alfresco configuration sections
      
      new GlobalConfigSection( this);
      new AlfrescoConfigSection( this);
      
      // Install the Alfresco client information factory
      
      ClientInfo.setFactory( new AlfrescoClientInfoFactory());
      
      // We need to check for a WINS server configuration in the CIFS server config section to initialize
      // the NetBIOS name lookups to use WINS rather broadcast lookups, which may be used to get the local
      // domain
      
      try {

    	  // Get the CIFS server config section and extract the WINS server config, if available
    	  
          processWINSServerConfig();
      }
      catch (Exception ex) {
    	  
          // Configuration error

          logger.error("File server configuration error (WINS), " + ex.getMessage(), ex);
      }
      
      // Initialize the filesystems
      
      try
      {
    	  // Process the core server configuration
    	  processCoreServerConfig();
    	  
          // Process the security configuration
          processSecurityConfig();
          
          // Process the Cluster  configuration
          processClusterConfig();

          // Process the filesystems configuration
          processFilesystemsConfig();
      }
      catch (Exception ex)
      {
          // Configuration error
          throw new AlfrescoRuntimeException("File server configuration error, " + ex.getMessage(), ex);
      }

      // Initialize the CIFS and FTP servers, if the filesystem(s) initialized successfully
      
      // Initialize the CIFS server

      try
      {

          // Process the CIFS server configuration
          processCIFSServerConfig();

          // Log the successful startup
          logger.info("CIFS server " + (isSMBServerEnabled() ? "" : "NOT ") + "started");
      }
      catch (UnsatisfiedLinkError ex)
      {
          // Error accessing the Win32NetBIOS DLL code

          logger.error("Error accessing Win32 NetBIOS, check DLL is on the path");

          // Disable the CIFS server

          removeConfigSection( CIFSConfigSection.SectionName);
      }
      catch (Throwable ex)
      {
          // Configuration error

          logger.error("CIFS server configuration error, " + ex.getMessage(), ex);

          // Disable the CIFS server

          removeConfigSection( CIFSConfigSection.SectionName);
      }

      // Initialize the FTP server

      try
      {
          // Process the FTP server configuration
          processFTPServerConfig();
          
          // Log the successful startup
          
          logger.info("FTP server " + (isFTPServerEnabled() ? "" : "NOT ") + "started");
      }
      catch (Exception ex)
      {
          // Configuration error
        
          logger.error("FTP server configuration error, " + ex.getMessage(), ex);
      }           

      // Initialize the NFS server

      try
      {
          // Process the NFS server configuration
          processNFSServerConfig();
          
          // Log the successful startup
          
          logger.info("NFS server " + (isNFSServerEnabled() ? "" : "NOT ") + "started");
      }
      catch (Exception ex)
      {
          // Configuration error
        
          logger.error("NFS server configuration error, " + ex.getMessage(), ex);
      }           
  }

  protected abstract void processCoreServerConfig() throws InvalidConfigurationException;

  protected abstract void processSecurityConfig();
  
  protected abstract void processFilesystemsConfig();

  protected abstract void processCIFSServerConfig();

  protected abstract void processNFSServerConfig();

  protected abstract void processFTPServerConfig();
  
  protected abstract void processClusterConfig() throws InvalidConfigurationException;
  
  protected void processWINSServerConfig() {}

  /**
   * Close the configuration bean
   */
  public final void closeConfiguration()
  {
      super.closeConfiguration();
  }
  
  /**
   * Determine the platform type
   */
  private final void determinePlatformType()
  {
    if ( m_platform == Platform.Type.Unchecked)
      m_platform = Platform.isPlatformType();
  }
  
    /**
     * Parse the platforms attribute returning the set of platform ids
     * 
     * @param platformStr String
     * @return EnumSet<PlatformType>
     */
    protected final EnumSet<Platform.Type> parsePlatformString(String platformStr)
    {
        // Split the platform string and build up a set of platform types
  
        EnumSet<Platform.Type> platformTypes = EnumSet.noneOf(Platform.Type.class);
        if (platformStr == null || platformStr.length() == 0)
            return platformTypes;
  
        StringTokenizer token = new StringTokenizer(platformStr.toUpperCase(Locale.ENGLISH), ",");
        String typ = null;
  
        try
        {
            while (token.hasMoreTokens())
            {
  
                // Get the current platform type string and validate
  
                typ = token.nextToken().trim();
                Platform.Type platform = Platform.Type.valueOf(typ);
  
                if (platform != Platform.Type.Unknown)
                    platformTypes.add(platform);
                else
                    throw new AlfrescoRuntimeException("Invalid platform type, " + typ);
            }
        }
        catch (IllegalArgumentException ex)
        {
            throw new AlfrescoRuntimeException("Invalid platform type, " + typ);
        }
  
        // Return the platform types
  
        return platformTypes;
    }
    
    /**
     * Get the local server name and optionally trim the domain name
     * 
     * @param trimDomain boolean
     * @return String
     */
    public final String getLocalServerName(boolean trimDomain)
    {
        // Use cached untrimmed version if necessary
        if (!trimDomain)
        {
            return getLocalServerName();
        }
        
        // Check if the name has already been set
        if (m_localName != null)
            return m_localName;

        // Find the local server name
        String srvName = getLocalServerName();

        // Strip the domain name

        if (trimDomain && srvName != null)
        {
            int pos = srvName.indexOf(".");
            if (pos != -1)
                srvName = srvName.substring(0, pos);
        }

        // Save the local server name

        m_localName = srvName;

        // Return the local server name

        return srvName;
    }

    /**
     * Get the local server name (untrimmed)
     * 
     * @return String
     */
    private String getLocalServerName()
    {
        // Check if the name has already been set

        if (m_localNameFull != null)
            return m_localNameFull;

        // Find the local server name

        String srvName = null;

        if (getPlatformType() == Platform.Type.WINDOWS && !isNativeCodeDisabled())
        {
            // Get the local name via JNI

            srvName = Win32NetBIOS.GetLocalNetBIOSName();
        }
        else
        {
            // Get the DNS name of the local system

            try
            {
                srvName = InetAddress.getLocalHost().getHostName();
            }
            catch (UnknownHostException ex)
            {
            }
        }

        // Save the local server name

        m_localNameFull = srvName;

        // Return the local server name

        return srvName;
    }

    /**
     * Get the local domain/workgroup name
     * 
     * @return String
     */
    public final String getLocalDomainName()
    {
        // Check if the local domain has been set

        if (m_localDomain != null)
            return m_localDomain;

        // Find the local domain name

        String domainName = null;

        if (getPlatformType() == Platform.Type.WINDOWS && !isNativeCodeDisabled())
        {
            // Get the local domain/workgroup name via JNI

            domainName = Win32NetBIOS.GetLocalDomainName();

            // Debug

            if (logger.isDebugEnabled())
                logger.debug("Local domain name is " + domainName + " (via JNI)");
        }
        else
        {
            NetBIOSName nbName = null;

            try
            {
                // Try and find the browse master on the local network

                nbName = NetBIOSSession.FindName(NetBIOSName.BrowseMasterName, NetBIOSName.BrowseMasterGroup, 5000);

                // Log the browse master details

                if (logger.isDebugEnabled())
                    logger.debug("Found browse master at " + nbName.getIPAddressString(0));

                // Get the NetBIOS name list from the browse master

                NetBIOSNameList nbNameList = NetBIOSSession.FindNamesForAddress(nbName.getIPAddressString(0));
                if (nbNameList != null)
                {
                    nbName = nbNameList.findName(NetBIOSName.MasterBrowser, false);
                    // Set the domain/workgroup name
                    if (nbName != null)
                        domainName = nbName.getName();
                }
            }
            catch (IOException ex)
            {
            }
        }

        // Save the local domain name

        m_localDomain = domainName;

        // Return the local domain/workgroup name

        return domainName;
    }
    
    /**
     * Parse an adapter name string and return the matching address
     * 
     * @param adapter String
     * @return InetAddress
     * @exception InvalidConfigurationException
     */
    protected final InetAddress parseAdapterName(String adapter)
      throws InvalidConfigurationException {

      NetworkInterface ni = null;
      
      try {
        ni = NetworkInterface.getByName( adapter);
      }
      catch (SocketException ex) {
        throw new InvalidConfigurationException( "Invalid adapter name, " + adapter);
      }
      
      if ( ni == null)
        throw new InvalidConfigurationException( "Invalid network adapter name, " + adapter);
      
      // Get the IP address for the adapter

      InetAddress adapAddr = null;
      Enumeration<InetAddress> addrEnum = ni.getInetAddresses();
      
      while ( addrEnum.hasMoreElements() && adapAddr == null) {
        
        // Get the current address
        
        InetAddress addr = addrEnum.nextElement();
        if ( IPAddress.isNumericAddress( addr.getHostAddress()))
          adapAddr = addr;
      }
      
      // Check if we found the IP address to bind to
      
      if ( adapAddr == null)
        throw new InvalidConfigurationException( "Adapter " + adapter + " does not have a valid IP address");

      // Return the adapter address
      
      return adapAddr;
    }
    
    private ApplicationContext applicationContext = null;
    
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (event instanceof ContextRefreshedEvent)
        {
            ContextRefreshedEvent refreshEvent = (ContextRefreshedEvent)event;
            ApplicationContext refreshContext = refreshEvent.getApplicationContext();
            if (refreshContext != null && refreshContext.equals(applicationContext))
            {
                // Initialize the bean
              
                init();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    /**
     * Return the authentication service
     * 
     * @return AuthenticationService
     */
    protected final AuthenticationService getAuthenticationService()
    {
        return m_authenticationService;
    }
    
    /**
     * Return the authentication component
     * 
     * @return AuthenticationComponent
     */
    protected final AuthenticationComponent getAuthenticationComponent()
    {
        return m_authenticationComponent;
    }
    
    /**
     * Return the node service
     * 
     * @return NodeService
     */
    protected final NodeService getNodeService()
    {
        return m_nodeService;
    }
    
    /**
     * Return the person service
     * 
     * @return PersonService
     */
    protected final PersonService getPersonService()
    {
        return m_personService;
    }
    
    /**
     * Return the transaction service
     * 
     * @return TransactionService
     */
    protected final TransactionService getTransactionService()
    {
        return m_transactionService;
    }
    
    /**
     * Return the tenant service
     * 
     * @return TenantService
     */
    protected final TenantService getTenantService()
    {
    	return m_tenantService;
    }
    
    /**
     * Return the search service
     * 
     * @return SearchService
     */
    protected final SearchService getSearchService()
    {
    	return m_searchService;
    }
    
    /**
     * Return the namespace service
     * 
     * @return NamespaceService
     */
    protected final NamespaceService getNamespaceService()
    {
    	return m_namespaceService;
    }
    
    /**
     * Check if native code calls are disabled
     * 
     * @return boolean
     */
    public final boolean isNativeCodeDisabled()
    {
    	return m_disableNativeCode;
    }
    
    /**
     * Return the named bean
     * 
     * @param beanName String
     * @return Object
     */
    public final Object getBean( String beanName)
    {
    	return applicationContext.getBean( beanName);
    }
    
    /**
     * Return the applicatin context
     * 
     * @return ApplicationContext
     */
    public final ApplicationContext getApplicationsContext()
    {
    	return applicationContext;
    }

    /**
     * Return the authority service
     * 
     * @return AuthorityService
     */
    public final AuthorityService getAuthorityService()
    {
    	return m_authorityService;
    }
}
