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
package org.alfresco.filesys;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.alfresco.jlan.debug.DebugConfigSection;
import org.alfresco.jlan.ftp.FTPConfigSection;
import org.alfresco.jlan.ftp.FTPPath;
import org.alfresco.jlan.ftp.InvalidPathException;
import org.alfresco.jlan.netbios.NetBIOSName;
import org.alfresco.jlan.netbios.NetBIOSNameList;
import org.alfresco.jlan.netbios.NetBIOSSession;
import org.alfresco.jlan.netbios.RFCNetBIOSProtocol;
import org.alfresco.jlan.netbios.win32.Win32NetBIOS;
import org.alfresco.jlan.oncrpc.nfs.NFSConfigSection;
import org.alfresco.jlan.server.auth.CifsAuthenticator;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.auth.acl.ACLParseException;
import org.alfresco.jlan.server.auth.acl.AccessControl;
import org.alfresco.jlan.server.auth.acl.AccessControlList;
import org.alfresco.jlan.server.auth.acl.AccessControlParser;
import org.alfresco.jlan.server.auth.acl.InvalidACLTypeException;
import org.alfresco.jlan.server.auth.passthru.DomainMapping;
import org.alfresco.jlan.server.auth.passthru.RangeDomainMapping;
import org.alfresco.jlan.server.auth.passthru.SubnetDomainMapping;
import org.alfresco.jlan.server.config.GlobalConfigSection;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.SecurityConfigSection;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.core.ShareType;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FilesystemsConfigSection;
import org.alfresco.jlan.smb.server.CIFSConfigSection;
import org.alfresco.jlan.util.IPAddress;
import org.alfresco.jlan.util.Platform;
import org.alfresco.jlan.util.StringList;
import org.alfresco.jlan.util.X64;
import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigLookupContext;
import org.alfresco.config.ConfigService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.alfresco.AlfrescoClientInfoFactory;
import org.alfresco.filesys.alfresco.DesktopAction;
import org.alfresco.filesys.alfresco.DesktopActionException;
import org.alfresco.filesys.alfresco.DesktopActionTable;
import org.alfresco.filesys.avm.AVMContext;
import org.alfresco.filesys.avm.AVMDiskDriver;
import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
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
import org.w3c.dom.Element;

import net.sf.acegisecurity.AuthenticationManager;

/**
 * Alfresco File Server Configuration Bean Class
 * 
 * @author gkspencer
 */
public class ServerConfigurationBean extends ServerConfiguration implements ApplicationListener, ApplicationContextAware {

  // Debug logging

  private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol");

  // Filesystem configuration constants
  
  private static final String ConfigArea        = "file-servers";
  private static final String ConfigCIFS        = "CIFS Server";
  private static final String ConfigFTP         = "FTP Server";
  private static final String ConfigNFS         = "NFS Server";
  private static final String ConfigFilesystems = "Filesystems";
  private static final String ConfigSecurity    = "Filesystem Security";

  // Server configuration bean name
  
  public static final String SERVER_CONFIGURATION = "fileServerConfiguration";
  
  // SMB/CIFS session debug type strings
  //
  // Must match the bit mask order
  
  private static final String m_sessDbgStr[] = { "NETBIOS", "STATE", "RXDATA", "TXDATA", "DUMPDATA", "NEGOTIATE", "TREE", "SEARCH", "INFO", "FILE",
          "FILEIO", "TRANSACT", "ECHO", "ERROR", "IPC", "LOCK", "PKTTYPE", "DCERPC", "STATECACHE", "TIMING", "NOTIFY",
          "STREAMS", "SOCKET" };

  // FTP server debug type strings

  private static final String m_ftpDebugStr[] = { "STATE", "SEARCH", "INFO", "FILE", "FILEIO", "ERROR", "PKTTYPE",
          "TIMING", "DATAPORT", "DIRECTORY" };

  // Default FTP server port
  
  private static final int DefaultFTPServerPort = 21;
  
  // Default FTP anonymous account name
  
  private static final String DefaultFTPAnonymousAccount = "anonymous";
  
  //  NFS server debug type strings
  
  private static final String m_nfsDebugStr[] = { "RXDATA", "TXDATA", "DUMPDATA", "SEARCH", "INFO", "FILE",
    "FILEIO", "ERROR", "TIMING", "DIRECTORY", "SESSION" };
  
  // Token name to substitute current server name into the CIFS server name
  
  private static final String TokenLocalName = "${localname}";
  
  // Authentication manager
  
  private AuthenticationManager m_authenticationManager;
  
  // Configuration service
  
  private ConfigService m_configService;
  
  // Disk interface to use for shared filesystems
  
  private DiskInterface m_repoDiskInterface;
  
  // AVM filesystem interface
  
  private DiskInterface m_avmDiskInterface;
  
  // Runtime platform type
  
  private Platform.Type m_platform = Platform.Type.Unchecked;

  // flag to indicate successful initialization
  
  private boolean m_initialised;

  // Main authentication service, public API
  
  private AuthenticationService m_authenticationService;

  // Authentication component, for internal functions
  
  private AuthenticationComponent m_authenticationComponent;
  
  // Various services
  
  private NodeService m_nodeService;
  private PersonService m_personService;
  private TransactionService m_transactionService;
  private TenantService m_tenantService;
  private SearchService m_searchService;
  private NamespaceService m_namespaceService;
  
  // Local server name and domain/workgroup name

  private String m_localName;
  private String m_localDomain;
  
  /**
   * Default constructor
   */
  public ServerConfigurationBean()
  {
    super ( "");
  }
  
  /**
   * Class constructor
   * 
   * @param srvName String
   */
  public ServerConfigurationBean( String srvName)
  {
      super( srvName);
  }
  
  /**
   * Set the authentication manager
   * 
   * @param authenticationManager AuthenticationManager
   */
  public void setAuthenticationManager(AuthenticationManager authenticationManager)
  {
      m_authenticationManager = authenticationManager;
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
   * Set the configuration service
   * 
   * @param configService ConfigService
   */
  public void setConfigService(ConfigService configService)
  {
      m_configService = configService;
  }

  /**
   * Set the filesystem driver for the node service based filesystem
   * 
   * @param diskInterface DiskInterface
   */
  public void setDiskInterface(DiskInterface diskInterface)
  {
      m_repoDiskInterface = diskInterface;
  }

  /**
   * Set the filesystem driver for the AVM based filesystem
   * 
   */
  public void setAvmDiskInterface(DiskInterface diskInterface)
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
  public final DiskInterface getRepoDiskInterface()
  {
      return m_repoDiskInterface;
  }
  
  /**
   * Return the disk interface to be used to create AVM filesystem shares
   * 
   * @return DiskInterface
   */
  public final DiskInterface getAvmDiskInterface()
  {
      return m_avmDiskInterface;
  }
  
  /**
   * Initialize the configuration using the configuration service
   */
  public void init()
  {
      // check that all required properties have been set
      if (m_authenticationManager == null)
      {
          throw new AlfrescoRuntimeException("Property 'authenticationManager' not set");
      }
      else if (m_authenticationComponent == null)
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
      else if (m_configService == null)
      {
          throw new AlfrescoRuntimeException("Property 'configService' not set");
      }
      
      // Create the configuration context

      ConfigLookupContext configCtx = new ConfigLookupContext(ConfigArea);

      // Set the platform type

      determinePlatformType();

      // Create the debug output configuration using a logger for all file server debug output
      
      DebugConfigSection debugConfig = new DebugConfigSection( this);
      try
      {
          debugConfig.setDebug("org.alfresco.filesys.debug.FileServerDebugInterface", null);
      }
      catch ( InvalidConfigurationException ex)
      {
      }
      
      // Create the global configuration and Alfresco configuration sections
      
      new GlobalConfigSection( this);
      new AlfrescoConfigSection( this);
      
      // Install the Alfresco client information factory
      
      ClientInfo.setFactory( new AlfrescoClientInfoFactory());
      
      // Initialize the filesystems

      boolean filesysInitOK = false;
      Config config = null;
      
      try
      {
          // Process the security configuration
  
          config = m_configService.getConfig(ConfigSecurity, configCtx);
          processSecurityConfig(config);

          // Process the filesystems configuration

          config = m_configService.getConfig(ConfigFilesystems, configCtx);
          processFilesystemsConfig(config);
          
          // Indicate that the filesystems were initialized
          
          filesysInitOK = true;
      }
      catch (Exception ex)
      {
          // Configuration error

          logger.error("File server configuration error, " + ex.getMessage(), ex);
      }

      // Initialize the CIFS and FTP servers, if the filesystem(s) initialized successfully
      
      if ( filesysInitOK == true)
      {
        // Initialize the CIFS server

        try
        {

            // Process the CIFS server configuration

            config = m_configService.getConfig(ConfigCIFS, configCtx);
            processCIFSServerConfig(config);

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

            config = m_configService.getConfig(ConfigFTP, configCtx);
            processFTPServerConfig(config);
            
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

            config = m_configService.getConfig(ConfigNFS, configCtx);
            processNFSServerConfig(config);
            
            // Log the successful startup
            
            logger.info("NFS server " + (isNFSServerEnabled() ? "" : "NOT ") + "started");
        }
        catch (Exception ex)
        {
            // Configuration error
          
            logger.error("NFS server configuration error, " + ex.getMessage(), ex);
        }           
    }
      else
      {
        // Log the error
        
        logger.error("CIFS and FTP servers not started due to filesystem initialization error");
      }
  }
  
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
   * Process the CIFS server configuration
   * 
   * @param config Config
   */
  private final void processCIFSServerConfig(Config config)
  {
      // If the configuration section is not valid then CIFS is disabled
      
      if ( config == null)
      {
          removeConfigSection( CIFSConfigSection.SectionName);
          return;
      }
          
      // Check if the server has been disabled
      
      ConfigElement elem = config.getConfigElement( "serverEnable");
      if ( elem != null)
      {
        // Check for the enabled attribute
        
        String srvEnable = elem.getAttribute( "enabled");
        if ( srvEnable != null && srvEnable.equalsIgnoreCase( "false"))
        {
              removeConfigSection( CIFSConfigSection.SectionName);
              return;
        }
      }

      // Create the CIFS server configuration section
      
      CIFSConfigSection cifsConfig = new CIFSConfigSection( this);
      
      try
      {
        // Get the network broadcast address
        //
        // Note: We need to set this first as the call to getLocalDomainName() may use a NetBIOS
        // name lookup, so the broadcast mask must be set before then.
  
        elem = config.getConfigElement("broadcast");
        if (elem != null)
        {
  
            // Check if the broadcast mask is a valid numeric IP address
  
            if (IPAddress.isNumericAddress(elem.getValue()) == false)
                throw new AlfrescoRuntimeException("Invalid broadcast mask, must be n.n.n.n format");
  
            // Set the network broadcast mask
  
            cifsConfig.setBroadcastMask(elem.getValue());
        }
  
        // Get the host configuration
  
        elem = config.getConfigElement("host");
        if (elem == null)
            throw new AlfrescoRuntimeException("CIFS server host settings not specified");
  
        String hostName = elem.getAttribute("name");
        if (hostName == null || hostName.length() == 0)
            throw new AlfrescoRuntimeException("Host name not specified or invalid");
  
        // Check if the host name contains the local name token
  
        int pos = hostName.indexOf(TokenLocalName);
        if (pos != -1)
        {
  
            // Get the local server name
  
            String srvName = getLocalServerName(true);
  
            // Rebuild the host name substituting the token with the local server name
  
            StringBuilder hostStr = new StringBuilder();
  
            hostStr.append(hostName.substring(0, pos));
            hostStr.append(srvName);
  
            pos += TokenLocalName.length();
            if (pos < hostName.length())
                hostStr.append(hostName.substring(pos));
  
            hostName = hostStr.toString();
  
            // Make sure the CIFS server name does not match the local server name
  
            if (hostName.equals(srvName) && getPlatformType() == Platform.Type.WINDOWS)
                throw new AlfrescoRuntimeException("CIFS server name must be unique");
        }
  
        // Check if the host name is longer than 15 characters. NetBIOS only allows a maximum of 16 characters in the
        // server name with the last character reserved for the service type.
        
        if ( hostName.length() > 15)
        {
          // Truncate the CIFS server name
          
          hostName = hostName.substring(0, 15);
          
          // Output a warning
          
          logger.warn("CIFS server name is longer than 15 characters, truncated to " + hostName);
        }
        
        // Set the CIFS server name
  
        cifsConfig.setServerName(hostName.toUpperCase());
        setServerName(hostName.toUpperCase());
  
        // Get the domain/workgroup name
  
        String domain = elem.getAttribute("domain");
        if (domain != null && domain.length() > 0)
        {
            // Set the domain/workgroup name
  
            cifsConfig.setDomainName(domain.toUpperCase());
        }
        else
        {
            // Get the local domain/workgroup name
  
            String localDomain = getLocalDomainName();
            
            if ( localDomain == null && getPlatformType() != Platform.Type.WINDOWS)
            {
                // Use a default domain/workgroup name
                
                localDomain = "WORKGROUP";
                
                // Output a warning
                
                logger.error("Failed to get local domain/workgroup name, using default of " + localDomain);
                logger.error("(This may be due to firewall settings or incorrect <broadcast> setting)");
            }
            
            // Set the local domain/workgroup that the CIFS server belongs to
            
            cifsConfig.setDomainName( localDomain);
        }
  
        // Check for a server comment
  
        elem = config.getConfigElement("comment");
        if (elem != null)
            cifsConfig.setComment(elem.getValue());
  
        // Check for a bind address
  
        elem = config.getConfigElement("bindto");
        if (elem != null)
        {
            //  Check if the network adapter name has been specified
            
            if ( elem.hasAttribute("adapter")) {
              
              // Get the IP address for the adapter

              InetAddress bindAddr = parseAdapterName( elem.getAttribute("adapter"));
              
              //  Set the bind address for the server
              
              cifsConfig.setSMBBindAddress(bindAddr);
            }
            else {
  
              // Validate the bind address
    
              String bindText = elem.getValue();
    
              try
              {
                  // Check the bind address
    
                  InetAddress bindAddr = InetAddress.getByName(bindText);
    
                  // Set the bind address for the server
    
                  cifsConfig.setSMBBindAddress(bindAddr);
              }
              catch (UnknownHostException ex)
              {
                  throw new AlfrescoRuntimeException("Invalid CIFS server bind address");
              }
            }
        }
  
        // Check if an authenticator has been specified
  
        ConfigElement authElem = config.getConfigElement("authenticator");
        if (authElem != null)
        {
            // Get the authenticator type
  
            String authType = authElem.getAttribute("type");
            if (authType == null)
                authType = "alfresco";
  
            // Get the authentication component type
            
            NTLMMode ntlmMode = m_authenticationComponent.getNTLMMode();
            
            // Set the authenticator class to use
  
           String authClass = "org.alfresco.filesys.auth.cifs.AlfrescoCifsAuthenticator";
  
            if (authType.equalsIgnoreCase("passthru"))
            {
                // Check if the appropriate authentication component type is configured
                
                if ( ntlmMode == NTLMMode.MD4_PROVIDER)
                    throw new AlfrescoRuntimeException("Wrong authentication setup for passthru authenticator (cannot be used with Alfresco users)");
                
                // Use the passthru authenticator class
                
                authClass = "org.alfresco.filesys.auth.cifs.PassthruCifsAuthenticator";
            }
            else if (authType.equalsIgnoreCase("alfresco"))
            {
                // Standard authenticator requires MD4 or passthru based authentication
                
                if ( ntlmMode == NTLMMode.NONE)
                    throw new AlfrescoRuntimeException("Wrong authentication setup for alfresco authenticator");
            }
            else if( authType.equalsIgnoreCase("enterprise"))
            {
                // Load the Enterprise authenticator dynamically
                
                authClass = "org.alfresco.filesys.auth.cifs.EnterpriseCifsAuthenticator";
            }
            else
                throw new AlfrescoRuntimeException("Invalid authenticator type, " + authType);
  
            // Get the allow guest and map unknown user to guest settings
  
            boolean allowGuest = authElem.getChild("allowGuest") != null ? true : false;
  
            // Initialize and set the authenticator class
  
            cifsConfig.setAuthenticator(authClass, authElem, CifsAuthenticator.USER_MODE, allowGuest);
        }
        else
          throw new AlfrescoRuntimeException("CIFS authenticator not specified");
        
        // Check if the host announcer should be enabled
  
        elem = config.getConfigElement("hostAnnounce");
        if (elem != null)
        {
  
            // Check for an announcement interval
  
            String interval = elem.getAttribute("interval");
            if (interval != null && interval.length() > 0)
            {
                try
                {
                    cifsConfig.setHostAnnounceInterval(Integer.parseInt(interval));
                }
                catch (NumberFormatException ex)
                {
                    throw new AlfrescoRuntimeException("Invalid host announcement interval");
                }
            }
  
            // Check if the domain name has been set, this is required if the
            // host announcer is enabled
  
            if (cifsConfig.getDomainName() == null)
                throw new AlfrescoRuntimeException("Domain name must be specified if host announcement is enabled");
  
            // Enable host announcement
  
            cifsConfig.setHostAnnouncer(true);
        }
  
        // Check if NetBIOS SMB is enabled
  
        elem = config.getConfigElement("netBIOSSMB");
        if (elem != null)
        {
            // Check if NetBIOS over TCP/IP is enabled for the current platform
  
            String platformsStr = elem.getAttribute("platforms");
            boolean platformOK = false;
  
            if (platformsStr != null)
            {
                // Parse the list of platforms that NetBIOS over TCP/IP is to be enabled for and
                // check if the current platform is included
  
                EnumSet<Platform.Type> enabledPlatforms = parsePlatformString(platformsStr);
                if (enabledPlatforms.contains(getPlatformType()))
                    platformOK = true;
            }
            else
            {
                // No restriction on platforms
  
                platformOK = true;
            }
  
            // Enable the NetBIOS SMB support, if enabled for this platform
  
            cifsConfig.setNetBIOSSMB(platformOK);
  
            // Parse/check NetBIOS settings, if enabled
            
            if ( cifsConfig.hasNetBIOSSMB())
            {
              // Check if the broadcast mask has been specified
  
              if (cifsConfig.getBroadcastMask() == null)
                  throw new AlfrescoRuntimeException("Network broadcast mask not specified");
  
              // Check for a bind address
  
              String bindto = elem.getAttribute("bindto");
              if (bindto != null && bindto.length() > 0)
              {
  
                  // Validate the bind address
  
                  try
                  {
  
                      // Check the bind address
  
                      InetAddress bindAddr = InetAddress.getByName(bindto);
  
                      // Set the bind address for the NetBIOS name server
  
                      cifsConfig.setNetBIOSBindAddress(bindAddr);
                  }
                  catch (UnknownHostException ex)
                  {
                      throw new AlfrescoRuntimeException("Invalid NetBIOS bind address");
                  }
              }
              else if (cifsConfig.hasSMBBindAddress())
              {
  
                  // Use the SMB bind address for the NetBIOS name server
  
                cifsConfig.setNetBIOSBindAddress(cifsConfig.getSMBBindAddress());
              }
              else
              {
                  // Get a list of all the local addresses
  
                  InetAddress[] addrs = null;
                      
                  try
                  {
                      // Get the local server IP address list
  
                      addrs = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
                  }
                  catch (UnknownHostException ex)
                  {
                      logger.error("Failed to get local address list", ex);
                  }
                  
                  // Check the address list for one or more valid local addresses filtering out the loopback address
                  
                  int addrCnt = 0;
  
                  if ( addrs != null)
                  {
                      for (int i = 0; i < addrs.length; i++)
                      {
      
                          // Check for a valid address, filter out '127.0.0.1' and '0.0.0.0' addresses
      
                          if (addrs[i].getHostAddress().equals("127.0.0.1") == false
                                  && addrs[i].getHostAddress().equals("0.0.0.0") == false)
                              addrCnt++;
                      }
                  }
                  
                  // Check if any addresses were found
                  
                  if ( addrCnt == 0)
                  {
                    // Enumerate the network adapter list
                    
                    Enumeration<NetworkInterface> niEnum = null;
                    
                    try
                    {
                      niEnum = NetworkInterface.getNetworkInterfaces();
                    }
                    catch (SocketException ex)
                    {
                    }
                    
                    if ( niEnum != null)
                    {
                      while ( niEnum.hasMoreElements())
                      {
                        // Get the current network interface
                        
                        NetworkInterface ni = niEnum.nextElement();
                        
                        // Enumerate the addresses for the network adapter
                        
                        Enumeration<InetAddress> niAddrs = ni.getInetAddresses();
                        if ( niAddrs != null)
                        {
                          // Check for any valid addresses
                          
                          while ( niAddrs.hasMoreElements())
                          {
                            InetAddress curAddr = niAddrs.nextElement();
                            
                            if ( curAddr.getHostAddress().equals("127.0.0.1") == false &&
                                curAddr.getHostAddress().equals("0.0.0.0") == false)
                              addrCnt++;
                          }
                        }
                      }
                      
                      // DEBUG
                      
                      if ( addrCnt > 0 && logger.isDebugEnabled())
                        logger.debug("Found valid IP address from interface list");
                    }
                    
                    // Check if we found any valid network addresses
                    
                    if ( addrCnt == 0)
                    {
                        // Log the available IP addresses
                        
                        if ( logger.isDebugEnabled())
                        {
                            logger.debug("Local address list dump :-");
                            if ( addrs != null)
                            {
                                for ( int i = 0; i < addrs.length; i++)
                                    logger.debug( "  Address: " + addrs[i]);
                            }
                            else
                                logger.debug("  No addresses");
                        }
                        
                        // Throw an exception to stop the CIFS/NetBIOS name server from starting
                        
                        throw new AlfrescoRuntimeException( "Failed to get IP address(es) for the local server, check hosts file and/or DNS setup");
                    }
                  }
              }
  
              //  Check if the session port has been specified
        
        String portNum = elem.getAttribute("sessionPort");
        if ( portNum != null && portNum.length() > 0) {
          try {
            cifsConfig.setSessionPort(Integer.parseInt(portNum));
            if ( cifsConfig.getSessionPort() <= 0 || cifsConfig.getSessionPort() >= 65535)
              throw new AlfrescoRuntimeException("NetBIOS session port out of valid range");
          }
          catch (NumberFormatException ex) {
            throw new AlfrescoRuntimeException("Invalid NetBIOS session port");
          }
        }
  
        //  Check if the name port has been specified
        
        portNum = elem.getAttribute("namePort");
        if ( portNum != null && portNum.length() > 0) {
          try {
            cifsConfig.setNameServerPort(Integer.parseInt(portNum));
            if ( cifsConfig.getNameServerPort() <= 0 || cifsConfig.getNameServerPort() >= 65535)
              throw new AlfrescoRuntimeException("NetBIOS name port out of valid range");
          }
          catch (NumberFormatException ex) {
            throw new AlfrescoRuntimeException("Invalid NetBIOS name port");
          }
        }
  
        //  Check if the datagram port has been specified
        
        portNum = elem.getAttribute("datagramPort");
        if ( portNum != null && portNum.length() > 0) {
          try {
            cifsConfig.setDatagramPort(Integer.parseInt(portNum));
            if ( cifsConfig.getDatagramPort() <= 0 || cifsConfig.getDatagramPort() >= 65535)
              throw new AlfrescoRuntimeException("NetBIOS datagram port out of valid range");
          }
          catch (NumberFormatException ex) {
            throw new AlfrescoRuntimeException("Invalid NetBIOS datagram port");
          }
        }
        
        //  Check for a bind address
        
        String attr = elem.getAttribute("bindto");
        if ( attr != null && attr.length() > 0) {
        
          //  Validate the bind address
  
          try {
          
            //  Check the bind address
          
            InetAddress bindAddr = InetAddress.getByName(attr);
        
            //  Set the bind address for the NetBIOS name server
          
            cifsConfig.setNetBIOSBindAddress(bindAddr);
          }
          catch (UnknownHostException ex) {
            throw new InvalidConfigurationException(ex.toString());
          }
        }
        
        // Check for a bind address using the adapter name
        
        else if ( elem.hasAttribute("adapter")) {
          
          // Get the bind address via the network adapter name
          
          InetAddress bindAddr = parseAdapterName( elem.getAttribute("adapter"));
          cifsConfig.setNetBIOSBindAddress( bindAddr);
        }
        else if ( cifsConfig.hasSMBBindAddress()) {
          
          //  Use the SMB bind address for the NetBIOS name server
          
          cifsConfig.setNetBIOSBindAddress(cifsConfig.getSMBBindAddress());
        }
        
            }
        }
        else
        {
  
            // Disable NetBIOS SMB support
  
          cifsConfig.setNetBIOSSMB(false);
        }
  
        // Check if TCP/IP SMB is enabled
  
        elem = config.getConfigElement("tcpipSMB");
        if (elem != null)
        {
  
            // Check if native SMB is enabled for the current platform
  
            String platformsStr = elem.getAttribute("platforms");
            boolean platformOK = false;
  
            if (platformsStr != null)
            {
                // Parse the list of platforms that native SMB is to be enabled for and
                // check if the current platform is included
  
                EnumSet<Platform.Type> enabledPlatforms = parsePlatformString(platformsStr);
                if (enabledPlatforms.contains(getPlatformType()))
                    platformOK = true;
            }
            else
            {
                // No restriction on platforms
  
                platformOK = true;
            }
  
            // Enable the TCP/IP SMB support, if enabled for this platform
  
            cifsConfig.setTcpipSMB(platformOK);
            
      //  Check if the port has been specified
      
      String portNum = elem.getAttribute("port");
      if ( portNum != null && portNum.length() > 0) {
        try {
          cifsConfig.setTcpipSMBPort(Integer.parseInt(portNum));
          if ( cifsConfig.getTcpipSMBPort() <= 0 || cifsConfig.getTcpipSMBPort() >= 65535)
            throw new AlfrescoRuntimeException("TCP/IP SMB port out of valid range");
        }
        catch (NumberFormatException ex) {
          throw new AlfrescoRuntimeException("Invalid TCP/IP SMB port");
        }
      }
        }
        else
        {
  
            // Disable TCP/IP SMB support
  
          cifsConfig.setTcpipSMB(false);
        }
  
        // Check if Win32 NetBIOS is enabled
  
        elem = config.getConfigElement("Win32NetBIOS");
        if (elem != null)
        {
  
            // Check if the Win32 NetBIOS server name has been specified
  
            String win32Name = elem.getAttribute("name");
            if (win32Name != null && win32Name.length() > 0)
            {
  
                // Validate the name
  
                if (win32Name.length() > 16)
                    throw new AlfrescoRuntimeException("Invalid Win32 NetBIOS name, " + win32Name);
  
                // Set the Win32 NetBIOS file server name
  
                cifsConfig.setWin32NetBIOSName(win32Name);
            }
  
            // Check if the Win32 NetBIOS LANA has been specified
  
            String lanaStr = elem.getAttribute("lana");
            if (lanaStr != null && lanaStr.length() > 0)
            {
                //  Check if the LANA has been specified as an IP address or adapter name
                
                int lana = -1;
                
                if ( IPAddress.isNumericAddress( lanaStr))
                {
                  
                  //  Convert the IP address to a LANA id
                  
                  lana = Win32NetBIOS.getLANAForIPAddress( lanaStr);
                  if ( lana == -1)
                    throw new AlfrescoRuntimeException( "Failed to convert IP address " + lanaStr + " to a LANA");
                }
                else if ( lanaStr.length() > 1 && Character.isLetter( lanaStr.charAt( 0))) {
  
                  //  Convert the network adapter to a LANA id
                  
                  lana = Win32NetBIOS.getLANAForAdapterName( lanaStr);
                  if ( lana == -1)
                    throw new AlfrescoRuntimeException( "Failed to convert network adapter " + lanaStr + " to a LANA");
                }
                else {
                  
                  try
                  {
                      lana = Integer.parseInt(lanaStr);
                  }
                  catch (NumberFormatException ex)
                  {
                      throw new AlfrescoRuntimeException("Invalid win32 NetBIOS LANA specified");
                  }
                }
                
                // LANA should be in the range 0-255
  
                if (lana < 0 || lana > 255)
                    throw new AlfrescoRuntimeException("Invalid Win32 NetBIOS LANA number, " + lana);
  
                // Set the LANA number
  
                cifsConfig.setWin32LANA(lana);
            }
  
            // Check if the native NetBIOS interface has been specified, either 'winsock' or 'netbios'
            
            String nativeAPI = elem.getAttribute("api");
            if ( nativeAPI != null && nativeAPI.length() > 0)
            {
                // Validate the API type
                
                boolean useWinsock = true;
                
                if ( nativeAPI.equalsIgnoreCase("netbios"))
                    useWinsock = false;
                else if ( nativeAPI.equalsIgnoreCase("winsock") == false)
                    throw new AlfrescoRuntimeException("Invalid NetBIOS API type, spefify 'winsock' or 'netbios'");
                
                // Set the NetBIOS API to use
                
                cifsConfig.setWin32WinsockNetBIOS( useWinsock);
            }
            
            // Force the older NetBIOS API code to be used on 64Bit Windows
            
            if ( cifsConfig.useWinsockNetBIOS() == true && X64.isWindows64())
            {
                // Log a warning
                
                logger.warn("Using older Netbios() API code");
                
                // Use the older NetBIOS API code
                
                cifsConfig.setWin32WinsockNetBIOS( false);
            }
            
            // Check if the current operating system is supported by the Win32
            // NetBIOS handler
  
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Windows")
                    && (osName.endsWith("95") == false && osName.endsWith("98") == false && osName.endsWith("ME") == false))
            {
  
                // Call the Win32NetBIOS native code to make sure it is initialized
  
                if ( Win32NetBIOS.LanaEnumerate() != null)
                {
                    // Enable Win32 NetBIOS
    
                  cifsConfig.setWin32NetBIOS(true);
                }
                else
                {
                    logger.warn("No NetBIOS LANAs available");
                }
            }
            else
            {
  
                // Win32 NetBIOS not supported on the current operating system
  
              cifsConfig.setWin32NetBIOS(false);
            }
        }
        else
        {
  
            // Disable Win32 NetBIOS
  
          cifsConfig.setWin32NetBIOS(false);
        }
  
        // Check if the host announcer should be enabled
  
        elem = config.getConfigElement("Win32Announce");
        if (elem != null)
        {
  
            // Check for an announcement interval
  
            String interval = elem.getAttribute("interval");
            if (interval != null && interval.length() > 0)
            {
                try
                {
                  cifsConfig.setWin32HostAnnounceInterval(Integer.parseInt(interval));
                }
                catch (NumberFormatException ex)
                {
                    throw new AlfrescoRuntimeException("Invalid host announcement interval");
                }
            }
  
            // Check if the domain name has been set, this is required if the
            // host announcer is enabled
  
            if (cifsConfig.getDomainName() == null)
                throw new AlfrescoRuntimeException("Domain name must be specified if host announcement is enabled");
  
            // Enable Win32 NetBIOS host announcement
  
            cifsConfig.setWin32HostAnnouncer(true);
        }
  
        // Check if NetBIOS and/or TCP/IP SMB have been enabled
  
        if (cifsConfig.hasNetBIOSSMB() == false && cifsConfig.hasTcpipSMB() == false && cifsConfig.hasWin32NetBIOS() == false)
            throw new AlfrescoRuntimeException("NetBIOS SMB, TCP/IP SMB or Win32 NetBIOS must be enabled");
  
        // Check if WINS servers are configured
  
        elem = config.getConfigElement("WINS");
  
        if (elem != null)
        {
  
            // Get the primary WINS server
  
            ConfigElement priWinsElem = elem.getChild("primary");
  
            if (priWinsElem == null || priWinsElem.getValue().length() == 0)
                throw new AlfrescoRuntimeException("No primary WINS server configured");
  
            // Validate the WINS server address
  
            InetAddress primaryWINS = null;
  
            try
            {
                primaryWINS = InetAddress.getByName(priWinsElem.getValue());
            }
            catch (UnknownHostException ex)
            {
                throw new AlfrescoRuntimeException("Invalid primary WINS server address, " + priWinsElem.getValue());
            }
  
            // Check if a secondary WINS server has been specified
  
            ConfigElement secWinsElem = elem.getChild("secondary");
            InetAddress secondaryWINS = null;
  
            if (secWinsElem != null)
            {
  
                // Validate the secondary WINS server address
  
                try
                {
                    secondaryWINS = InetAddress.getByName(secWinsElem.getValue());
                }
                catch (UnknownHostException ex)
                {
                    throw new AlfrescoRuntimeException("Invalid secondary WINS server address, "
                            + secWinsElem.getValue());
                }
            }
  
            // Set the WINS server address(es)
  
            cifsConfig.setPrimaryWINSServer(primaryWINS);
            if (secondaryWINS != null)
              cifsConfig.setSecondaryWINSServer(secondaryWINS);
  
            // Pass the setting to the NetBIOS session class
  
            NetBIOSSession.setDefaultWINSServer(primaryWINS);
        }
  
        // Check if WINS is configured, if we are running on Windows and socket based NetBIOS is enabled
  
        else if (cifsConfig.hasNetBIOSSMB() && getPlatformType() == Platform.Type.WINDOWS)
        {
            // Get the WINS server list
  
            String winsServers = Win32NetBIOS.getWINSServerList();
  
            if (winsServers != null)
            {
                // Use the first WINS server address for now
  
                StringTokenizer tokens = new StringTokenizer(winsServers, ",");
                String addr = tokens.nextToken();
  
                try
                {
                    // Convert to a network address and check if the WINS server is accessible
  
                    InetAddress winsAddr = InetAddress.getByName(addr);
  
                    Socket winsSocket = new Socket();
                    InetSocketAddress sockAddr = new InetSocketAddress( winsAddr, RFCNetBIOSProtocol.NAME_PORT);
                    
                    winsSocket.connect(sockAddr, 3000);
                    winsSocket.close();
                    
                    // Set the primary WINS server address
                    
                    cifsConfig.setPrimaryWINSServer(winsAddr);
  
                    // Debug
  
                    if (logger.isDebugEnabled())
                        logger.debug("Configuring to use WINS server " + addr);
                }
                catch (UnknownHostException ex)
                {
                    throw new AlfrescoRuntimeException("Invalid auto WINS server address, " + addr);
                }
                catch (IOException ex)
                {
                    if ( logger.isDebugEnabled())
                        logger.debug("Failed to connect to auto WINS server " + addr);
                }
            }
        }
  
        // Check if session debug is enabled
  
        elem = config.getConfigElement("sessionDebug");
        if (elem != null)
        {
  
            // Check for session debug flags
  
            String flags = elem.getAttribute("flags");
            int sessDbg = 0;
  
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
  
                    while (idx < m_sessDbgStr.length && m_sessDbgStr[idx].equalsIgnoreCase(dbg) == false)
                        idx++;
  
                    if (idx > m_sessDbgStr.length)
                        throw new AlfrescoRuntimeException("Invalid session debug flag, " + dbg);
  
                    // Set the debug flag
  
                    sessDbg += 1 << idx;
                }
            }
  
            // Set the session debug flags
  
            cifsConfig.setSessionDebugFlags(sessDbg);
        }
      }
      catch ( InvalidConfigurationException ex)
      {
        throw new AlfrescoRuntimeException( ex.getMessage());
      }
  }

  /**
   * Process the FTP server configuration
   * 
   * @param config Config
   */
  private final void processFTPServerConfig(Config config)
  {
      // If the configuration section is not valid then FTP is disabled
      
      if ( config == null)
      {
          removeConfigSection( FTPConfigSection.SectionName);
          return;
      }
          
      // Check if the server has been disabled
      
      ConfigElement elem = config.getConfigElement( "serverEnable");
      if ( elem != null)
      {
        // Check for the enabled attribute
        
        String srvEnable = elem.getAttribute( "enabled");
        if ( srvEnable != null && srvEnable.equalsIgnoreCase( "false"))
        {
              removeConfigSection( FTPConfigSection.SectionName);
              return;
        }
      }

      //  Create the FTP configuration section
      
      FTPConfigSection ftpConfig = new FTPConfigSection( this);
      
      try
      {
        //  Check for a bind address
        
        elem = config.getConfigElement("bindto");
        if ( elem != null) {
        
            //  Validate the bind address
  
            String bindText = elem.getValue();
        
            try {
            
                //  Check the bind address
            
                InetAddress bindAddr = InetAddress.getByName(bindText);
        
                //  Set the bind address for the FTP server
            
                ftpConfig.setFTPBindAddress(bindAddr);
            }
            catch (UnknownHostException ex) {
                throw new AlfrescoRuntimeException("Invalid FTP bindto address, " + elem.getValue());
            }
        }
  
        //  Check for an FTP server port
    
        elem = config.getConfigElement("port");
        if ( elem != null) {
            try {
                ftpConfig.setFTPPort(Integer.parseInt(elem.getValue()));
                if ( ftpConfig.getFTPPort() <= 0 || ftpConfig.getFTPPort() >= 65535)
                    throw new AlfrescoRuntimeException("FTP server port out of valid range");
            }
            catch (NumberFormatException ex) {
                throw new AlfrescoRuntimeException("Invalid FTP server port");
            }
        }
        else {
        
            //  Use the default FTP port
        
            ftpConfig.setFTPPort(DefaultFTPServerPort);
        }
    
        //  Check if anonymous login is allowed
    
        elem = config.getConfigElement("allowAnonymous");
        if ( elem != null) {
        
            //  Enable anonymous login to the FTP server
        
            ftpConfig.setAllowAnonymousFTP(true);
        
            //  Check if an anonymous account has been specified
        
            String anonAcc = elem.getAttribute("user");
            if ( anonAcc != null && anonAcc.length() > 0) {
            
                //  Set the anonymous account name
            
                ftpConfig.setAnonymousFTPAccount(anonAcc);
            
                //  Check if the anonymous account name is valid
            
                if ( ftpConfig.getAnonymousFTPAccount() == null || ftpConfig.getAnonymousFTPAccount().length() == 0)
                    throw new AlfrescoRuntimeException("Anonymous FTP account invalid");
            }
            else {
            
                //  Use the default anonymous account name
            
                ftpConfig.setAnonymousFTPAccount(DefaultFTPAnonymousAccount);
            }
        }
        else {
        
            //  Disable anonymous logins
        
            ftpConfig.setAllowAnonymousFTP(false);
        }
  
        //  Check if a root path has been specified
        
        elem = config.getConfigElement("rootDirectory");
        if ( elem != null) {
  
            //  Get the root path
            
            String rootPath = elem.getValue();
                        
            //  Validate the root path
            
            try {
                
                //  Parse the path
                
                new FTPPath(rootPath);
                
                //  Set the root path
                
                ftpConfig.setFTPRootPath(rootPath);
            }
            catch (InvalidPathException ex) {
                throw new AlfrescoRuntimeException("Invalid FTP root directory, " + rootPath);
            }
        }
  
        //  Check if FTP debug is enabled
        
        elem = config.getConfigElement("debug");
        if (elem != null) {
        
            //  Check for FTP debug flags
        
            String flags = elem.getAttribute("flags");
            int ftpDbg = 0;
        
            if ( flags != null) {
            
                //  Parse the flags
            
                flags = flags.toUpperCase();
                StringTokenizer token = new StringTokenizer(flags,",");
            
                while ( token.hasMoreTokens()) {
                
                    //  Get the current debug flag token
                
                    String dbg = token.nextToken().trim();
                
                    //  Find the debug flag name
                
                    int idx = 0;
                
                    while ( idx < m_ftpDebugStr.length && m_ftpDebugStr[idx].equalsIgnoreCase(dbg) == false)
                        idx++;
                    
                    if ( idx >= m_ftpDebugStr.length)
                        throw new AlfrescoRuntimeException("Invalid FTP debug flag, " + dbg);
                    
                    //  Set the debug flag
                
                    ftpDbg += 1 << idx;
                }
            }
  
            //  Set the FTP debug flags
        
            ftpConfig.setFTPDebug(ftpDbg);
        }
        
        // Check if a character set has been specified
        
        elem = config.getConfigElement( "charSet");
        if ( elem != null) {
          
          try {
            
              // Validate the character set name
              
            Charset.forName( elem.getValue());
            
            // Set the FTP character set
            
            ftpConfig.setFTPCharacterSet( elem.getValue());
          }
          catch ( IllegalCharsetNameException ex) {
            throw new AlfrescoRuntimeException("Illegal character set name, " + elem.getValue());
          }
          catch ( UnsupportedCharsetException ex) {
            throw new AlfrescoRuntimeException("Unsupported character set name, " + elem.getValue());
          }
        }

        // Check if an authenticator has been specified
        
        ConfigElement authElem = config.getConfigElement("authenticator");
        if (authElem != null)
        {
            // Get the authenticator type

            String authType = authElem.getAttribute("type");
            if (authType == null)
                authType = "alfresco";

            // Get the authentication component type
            
            NTLMMode ntlmMode = m_authenticationComponent.getNTLMMode();
            
            // Set the authenticator class to use

           String authClass = "org.alfresco.filesys.auth.ftp.AlfrescoFtpAuthenticator";

            if (authType.equalsIgnoreCase("passthru"))
            {
                // Check if the appropriate authentication component type is configured
                
                if ( ntlmMode == NTLMMode.MD4_PROVIDER)
                    throw new AlfrescoRuntimeException("Wrong authentication setup for passthru authenticator (cannot be used with Alfresco users)");
                
                // Use the passthru authenticator class
                
                authClass = "org.alfresco.filesys.auth.ftp.PassthruFtpAuthenticator";
            }
            else if (authType.equalsIgnoreCase("alfresco"))
            {
                // Standard authenticator requires MD4 or passthru based authentication
                
                if ( ntlmMode == NTLMMode.NONE)
                    throw new AlfrescoRuntimeException("Wrong authentication setup for alfresco authenticator");
            }
            else
                throw new AlfrescoRuntimeException("Invalid authenticator type, " + authType);

            // Initialize and set the authenticator class

            ftpConfig.setAuthenticator(authClass, authElem);
        }
        else
          throw new AlfrescoRuntimeException("FTP authenticator not specified");
      
      }
      catch (InvalidConfigurationException ex)
      {
        throw new AlfrescoRuntimeException( ex.getMessage());
      }
      
  }

  /**
   * Process the NFS server configuration
   * 
   * @param config Config
   */
  private final void processNFSServerConfig(Config config)
  {
      // If the configuration section is not valid then NFS is disabled
      
      if ( config == null)
      {
          removeConfigSection( NFSConfigSection.SectionName);
          return;
      }

      // Check if the server has been disabled
      
      ConfigElement elem = config.getConfigElement( "serverEnable");
      if ( elem != null)
      {
        // Check for the enabled attribute
        
        String srvEnable = elem.getAttribute( "enabled");
        if ( srvEnable != null && srvEnable.equalsIgnoreCase( "false"))
        {
            removeConfigSection( NFSConfigSection.SectionName);
            return;
        }
      }

      //  Create the NFS configuration section
      
      NFSConfigSection nfsConfig = new NFSConfigSection( this);

      try
      {
        //  Check if the port mapper is enabled
    
        if ( config.getConfigElement("enablePortMapper") != null)
          nfsConfig.setNFSPortMapper( true);
      
        //  Check for the thread pool size
        
        elem = config.getConfigElement("ThreadPool");
        
        if ( elem != null) {
      
          try {
            
            //  Convert the pool size value
            
            int poolSize = Integer.parseInt(elem.getValue());
            
            //  Range check the pool size value
            
            if ( poolSize < 4)
              throw new AlfrescoRuntimeException("NFS thread pool size is below minimum of 4");
              
            //  Set the thread pool size
  
            nfsConfig.setNFSThreadPoolSize(poolSize);
          }
          catch (NumberFormatException ex) {
            throw new AlfrescoRuntimeException("Invalid NFS thread pool size setting, " + elem.getValue());
          }
        }
      
        //  NFS packet pool size
        
        elem = config.getConfigElement("PacketPool");
        
        if ( elem != null) {
      
          try {
            
            //  Convert the packet pool size value
            
            int pktPoolSize = Integer.parseInt(elem.getValue());
            
            //  Range check the pool size value
            
            if ( pktPoolSize < 10)
              throw new AlfrescoRuntimeException("NFS packet pool size is below minimum of 10");
      
            if ( pktPoolSize < nfsConfig.getNFSThreadPoolSize() + 1)
              throw new AlfrescoRuntimeException("NFS packet pool must be at least thread pool size plus one");
            
            //  Set the packet pool size
            
            nfsConfig.setNFSPacketPoolSize( pktPoolSize);
          }
          catch (NumberFormatException ex) {
            throw new AlfrescoRuntimeException("Invalid NFS packet pool size setting, " + elem.getValue());
          }
        }
      
        //  Check for a port mapper server port
        
        elem = config.getConfigElement("PortMapperPort");
        if ( elem != null) {
          try {
            nfsConfig.setPortMapperPort( Integer.parseInt(elem.getValue()));
            if ( nfsConfig.getPortMapperPort() <= 0 || nfsConfig.getPortMapperPort() >= 65535)
              throw new AlfrescoRuntimeException("Port mapper server port out of valid range");
          }
          catch (NumberFormatException ex) {
            throw new AlfrescoRuntimeException("Invalid port mapper server port");
          }
        }
      
        //  Check for a mount server port
        
        elem = config.getConfigElement("MountServerPort");
        if ( elem != null) {
          try {
            nfsConfig.setMountServerPort( Integer.parseInt(elem.getValue()));
            if ( nfsConfig.getMountServerPort() <= 0 || nfsConfig.getMountServerPort() >= 65535)
              throw new AlfrescoRuntimeException("Mount server port out of valid range");
          }
          catch (NumberFormatException ex) {
            throw new AlfrescoRuntimeException("Invalid mount server port");
          }
        }
        
        //  Check for an NFS server port
        
        elem = config.getConfigElement("NFSServerPort");
        if ( elem != null) {
          try {
            nfsConfig.setNFSServerPort( Integer.parseInt(elem.getValue()));
            if ( nfsConfig.getNFSServerPort() <= 0 || nfsConfig.getNFSServerPort() >= 65535)
              throw new AlfrescoRuntimeException("NFS server port out of valid range");
          }
          catch (NumberFormatException ex) {
            throw new AlfrescoRuntimeException("Invalid NFS server port");
          }
        }
      
        //  Check if NFS debug is enabled
      
        elem = config.getConfigElement("debug");
        if (elem != null) {
      
          //  Check for NFS debug flags
      
          String flags = elem.getAttribute("flags");
          int nfsDbg = 0;
      
          if ( flags != null) {
        
            //  Parse the flags
        
            flags = flags.toUpperCase();
            StringTokenizer token = new StringTokenizer(flags,",");
        
            while ( token.hasMoreTokens()) {
          
              //  Get the current debug flag token
          
              String dbg = token.nextToken().trim();
          
              //  Find the debug flag name
          
              int idx = 0;
          
              while ( idx < m_nfsDebugStr.length && m_nfsDebugStr[idx].equalsIgnoreCase(dbg) == false)
                idx++;
            
              if ( idx >= m_nfsDebugStr.length)
                throw new AlfrescoRuntimeException("Invalid NFS debug flag, " + dbg);
            
              //  Set the debug flag
          
              nfsDbg += 1 << idx;
            }
          }
      
          //  Set the NFS debug flags
  
          nfsConfig.setNFSDebug( nfsDbg);
        }
        
        // Check if mount server debug output is enabled
        
        elem = config.getConfigElement("mountServerDebug");
        if ( elem != null)
          nfsConfig.setMountServerDebug( true);
        
        // Check if portmapper debug output is enabled
        
        elem = config.getConfigElement("portMapperDebug");
        if ( elem != null)
          nfsConfig.setPortMapperDebug(true);
        
        // Create the RPC authenticator
        
        elem = config.getConfigElement("rpcAuthenticator");
        if ( elem != null)
        {
          try
          {
            // Create the RPC authenticator
            
            nfsConfig.setRpcAuthenticator( "org.alfresco.filesys.auth.nfs.AlfrescoRpcAuthenticator", elem);
          }
          catch ( InvalidConfigurationException ex)
          {
            throw new AlfrescoRuntimeException( ex.getMessage());
          }
        }
        else
          throw new AlfrescoRuntimeException("RPC authenticator configuration missing, require user mappings");
      }
      catch ( InvalidConfigurationException ex)
      {
        throw new AlfrescoRuntimeException( ex.getMessage());
      }
  }
  
  /**
   * Process the filesystems configuration
   * 
   * @param config Config
   */
  private final void processFilesystemsConfig(Config config)
  {
      // Get the top level filesystems configuration element
      
      ConfigElement filesystems = config.getConfigElement("filesystems");
      
      // Get the filesystem configuration elements

      List<ConfigElement> filesysElems = null;
      if ( filesystems != null)
      {
          // Get the list of filesystems
          
          filesysElems = filesystems.getChildren();
      }
      else
      {
          // Check for the old style configuration
          
          ConfigElement filesysElem = config.getConfigElement( "filesystem");
          
          if (filesysElem != null)
          {
             // create a list with the single filesys element in
             
             filesysElems = new ArrayList<ConfigElement>(1);
             filesysElems.add(filesysElem);
          }
          
          // Warn that the configuration is using the old format
          
          logger.warn("Old style file-servers.xml configuration being used");
      }

      // Create the filesystems configuration section
      
      FilesystemsConfigSection fsysConfig = new FilesystemsConfigSection( this);
      
      // Access the security configuration section
      
      SecurityConfigSection secConfig = (SecurityConfigSection) getConfigSection( SecurityConfigSection.SectionName);
      
      // Process the filesystems list
      
      if (filesysElems != null)
      {

          // Add the filesystems

          for (int i = 0; i < filesysElems.size(); i++)
          {

              // Get the current filesystem configuration

              ConfigElement elem = filesysElems.get(i);
              
              String filesysType = elem.getName();
              String filesysName = elem.getAttribute("name");

              try
              {
                // Check the filesystem type and use the appropriate driver
                
                DiskSharedDevice filesys = null;
                
                if ( filesysType.equalsIgnoreCase("avmfilesystem"))
                {
                    // Create a new filesystem driver instance and create a context for
                    // the new filesystem
                  
                    DiskInterface filesysDriver = getAvmDiskInterface();
                    AVMContext filesysContext = (AVMContext) filesysDriver.createContext( filesysName, elem);
                  
                    // Create the shared filesystem
                  
                    filesys = new DiskSharedDevice(filesysName, filesysDriver, filesysContext);

                    // Start the filesystem
                  
                    filesysContext.startFilesystem(filesys);
                }
                else
                {
                    // Create a new filesystem driver instance and create a context for
                    // the new filesystem
                  
                    DiskInterface filesysDriver = getRepoDiskInterface();
                    ContentContext filesysContext = (ContentContext) filesysDriver.createContext( filesysName, elem);

                    // Check if an access control list has been specified

                    AccessControlList acls = null;
                    ConfigElement aclElem = elem.getChild("accessControl");

                    if (aclElem != null)
                    {

                        // Parse the access control list

                        acls = processAccessControlList(secConfig, aclElem);
                    }
                    else if (secConfig.hasGlobalAccessControls())
                    {

                        // Use the global access control list for this disk share

                        acls = secConfig.getGlobalAccessControls();
                    }

                    // Check if change notifications are disabled

                    boolean changeNotify = elem.getChild("disableChangeNotification") == null ? true : false;

                    // Create the shared filesystem

                    filesys = new DiskSharedDevice(filesysName, filesysDriver, filesysContext);

                    // Attach desktop actions to the filesystem
                    
                    ConfigElement deskActionsElem = elem.getChild("desktopActions");
                    if ( deskActionsElem != null)
                    {
                      // Get the desktop actions list
                      
                      DesktopActionTable desktopActions = processDesktopActions(deskActionsElem, filesys);
                      if ( desktopActions != null)
                        filesysContext.setDesktopActions( desktopActions, filesysDriver);
                    }
                    
                    // Add any access controls to the share

                    filesys.setAccessControlList(acls);

                    // Enable/disable change notification for this device

                    filesysContext.enableChangeHandler(changeNotify);

                    // Start the filesystem

                    filesysContext.startFilesystem(filesys);
                  }
                
                  // Add the new filesystem

                  fsysConfig.addShare( filesys);
              }
              catch (DeviceContextException ex)
              {
                  throw new AlfrescoRuntimeException("Error creating filesystem " + filesysName, ex);
              }
          }
      }
      else
      {
          // No filesystems defined
          
          logger.warn("No filesystems defined");
      }

      // Check if shares should be added for all AVM stores
      
      ConfigElement avmAllStoresElem = config.getConfigElement( "avmAllStores");
      
      if ( avmAllStoresElem != null && getAvmDiskInterface() != null)
      {
        // Get the list of store names
        
        AVMDiskDriver avmDriver = (AVMDiskDriver) getAvmDiskInterface();
        StringList storeNames = avmDriver.getAVMStoreNames();
        
        // Add shares for each of the store names, if the share name does not already exist
        
        if ( storeNames != null && storeNames.numberOfStrings() > 0)
        {
          // Add a share for each store
          
          for ( int i = 0; i < storeNames.numberOfStrings(); i++)
          {
            String storeName = storeNames.getStringAt( i);
            
            // Check if a share of the same name already exists
            
            if ( fsysConfig.getShares().findShare( storeName, ShareType.DISK, true) == null)
            {
              // Create the new share for the store
              
                    AVMContext avmContext = new AVMContext( storeName, storeName + ":/", AVMContext.VERSION_HEAD);
                    avmContext.enableStateTable( true, avmDriver.getStateReaper());
                  
                    // Create the shared filesystem
                  
                    fsysConfig.addShare( new DiskSharedDevice( storeName, avmDriver, avmContext));
                    
                    // DEBUG
                    
                    if ( logger.isDebugEnabled())
                      logger.debug( "Added AVM share " + storeName);
            }
          }
        }
      }
  }

  /**
   * Process the security configuration
   * 
   * @param config Config
   */
  private final void processSecurityConfig(Config config)
  {
      // Create the security configuration section
    
      SecurityConfigSection secConfig = new SecurityConfigSection( this);
      
      try
      {
        // Check if global access controls have been specified
  
        ConfigElement globalACLs = config.getConfigElement("globalAccessControl");
        if (globalACLs != null)
        {
  
            // Parse the access control list
  
            AccessControlList acls = processAccessControlList(secConfig, globalACLs);
            if (acls != null)
                secConfig.setGlobalAccessControls(acls);
        }
  
        // Check if a JCE provider class has been specified
  
        ConfigElement jceElem = config.getConfigElement("JCEProvider");
        if (jceElem != null)
        {
  
            // Set the JCE provider
  
            secConfig.setJCEProvider(jceElem.getValue());
        }
        else
        {
            // Use the default Bouncy Castle JCE provider
            
            secConfig.setJCEProvider("org.bouncycastle.jce.provider.BouncyCastleProvider");
        }
        
        //  Check if a share mapper has been specified
        
        ConfigElement mapperElem = config.getConfigElement("shareMapper");
        
        if ( mapperElem != null) {

          //  Check if the shre mapper type has been specified
        	
          String mapperType = mapperElem.getAttribute( "type");
          String mapperClass = null;
          
          if ( mapperType.equalsIgnoreCase( "multi-tenant"))
        	  mapperClass = "org.alfresco.filesys.alfresco.MultiTenantShareMapper";
          else
          {
	          //  Get the share mapper class
	          
	          ConfigElement classElem = mapperElem.getChild( "class");
	          if ( classElem == null)
	            throw new InvalidConfigurationException("Share mapper class not specified");
          }
          
          //  Initialize the share mapper
          
          secConfig.setShareMapper(mapperClass, mapperElem);   
        }
        
        // Check if any domain mappings have been specified
        
        ConfigElement domainMappings = config.getConfigElement( "DomainMappings");
        if ( domainMappings != null)
        {
          // Get the domain mapping elements
          
          List<ConfigElement> mappings = domainMappings.getChildren();
          if ( mappings != null)
          {
            DomainMapping mapping = null;
            
            for ( ConfigElement domainMap : mappings)
            {
              if ( domainMap.getName().equals( "Domain"))
              {
                // Get the domain name
                
                String name = domainMap.getAttribute( "name");
                
                // Check if the domain is specified by subnet or range
                
                if ( domainMap.hasAttribute( "subnet"))
                {
                  String subnetStr = domainMap.getAttribute( "subnet");
                  String maskStr   = domainMap.getAttribute( "mask");
                  
                  // Parse the subnet and mask, to validate and convert to int values
                  
                  int subnet = IPAddress.parseNumericAddress( subnetStr);
                  int mask   = IPAddress.parseNumericAddress( maskStr);
                  
                  if ( subnet == 0 || mask == 0)
                    throw new AlfrescoRuntimeException( "Invalid subnet/mask for domain mapping " + name);
                  
                  // Create the subnet domain mapping
                  
                  mapping = new SubnetDomainMapping( name, subnet, mask);
                }
                else if ( domainMap.hasAttribute( "rangeFrom"))
                {
                  String rangeFromStr = domainMap.getAttribute( "rangeFrom");
                  String rangeToStr   = domainMap.getAttribute( "rangeTo");
                  
                  // Parse the range from/to values and convert to int values
                  
                  int rangeFrom = IPAddress.parseNumericAddress( rangeFromStr);
                  int rangeTo   = IPAddress.parseNumericAddress( rangeToStr);
                  
                  if ( rangeFrom == 0 || rangeTo == 0)
                    throw new AlfrescoRuntimeException( "Invalid address range domain mapping " + name);
                  
                  // Create the subnet domain mapping
                  
                  mapping = new RangeDomainMapping( name, rangeFrom, rangeTo);
                }
                else
                  throw new AlfrescoRuntimeException( "Invalid domain mapping specified");
                
                // Add the domain mapping
                
                secConfig.addDomainMapping(mapping);
              }
            }
          }
        }
      }
      catch ( InvalidConfigurationException ex)
      {
        throw new AlfrescoRuntimeException( ex.getMessage());
      }
  }

  /**
   * Process an access control sub-section and return the access control list
   * 
   * @param secConfig SecurityConfigSection
   * @param aclsElem ConfigElement
   */
  private final AccessControlList processAccessControlList(SecurityConfigSection secConfig, ConfigElement aclsElem)
  {

      // Check if there is an access control manager configured

      if (secConfig.getAccessControlManager() == null)
          throw new AlfrescoRuntimeException("No access control manager configured");

      // Create the access control list

      AccessControlList acls = new AccessControlList();

      // Check if there is a default access level for the ACL group

      String attrib = aclsElem.getAttribute("default");

      if (attrib != null && attrib.length() > 0)
      {

          // Get the access level and validate

          try
          {

              // Parse the access level name

              int access = AccessControlParser.parseAccessTypeString(attrib);

              // Set the default access level for the access control list

              acls.setDefaultAccessLevel(access);
          }
          catch (InvalidACLTypeException ex)
          {
              throw new AlfrescoRuntimeException("Default access level error", ex);
          }
          catch (ACLParseException ex)
          {
              throw new AlfrescoRuntimeException("Default access level error", ex);
          }
      }

      // Parse each access control element

      List<ConfigElement> aclElemList = aclsElem.getChildren();

      if (aclElemList != null && aclElemList.size() > 0)
      {

          // Create the access controls

          for (int i = 0; i < aclsElem.getChildCount(); i++)
          {

              // Get the current ACL element

              ConfigElement curAclElem = aclElemList.get(i);

              try
              {
                  // Create the access control and add to the list

                  acls.addControl(secConfig.getAccessControlManager().createAccessControl(curAclElem.getName(), curAclElem));
              }
              catch (InvalidACLTypeException ex)
              {
                  throw new AlfrescoRuntimeException("Invalid access control type - " + curAclElem.getName());
              }
              catch (ACLParseException ex)
              {
                  throw new AlfrescoRuntimeException("Access control parse error (" + curAclElem.getName() + ")", ex);
              }
          }
      }

      // Check if there are no access control rules but the default access level is set to 'None',
      // this is not allowed as the share would not be accessible or visible.

      if (acls.getDefaultAccessLevel() == AccessControl.NoAccess && acls.numberOfControls() == 0)
          throw new AlfrescoRuntimeException("Empty access control list and default access 'None' not allowed");

      // Return the access control list

      return acls;
  }

  /**
   * Process a desktop actions sub-section and return the desktop action table
   * 
   * @param deskActionElem ConfigElement
   * @param fileSys DiskSharedDevice
   */
  private final DesktopActionTable processDesktopActions(ConfigElement deskActionElem, DiskSharedDevice fileSys)
  {
      // Get the desktop action configuration elements

    DesktopActionTable desktopActions = null;
      List<ConfigElement> actionElems = deskActionElem.getChildren();
      
      if ( actionElems != null)
      {
        // Check for the global configuration section
        
        ConfigElement globalConfig = deskActionElem.getChild("global");
        
        // Allocate the actions table
        
        desktopActions = new DesktopActionTable();
        
        // Process the desktop actions list
        
        for ( ConfigElement actionElem : actionElems)
        {
          if ( actionElem.getName().equals("action"))
          {
            // Get the desktop action class name or bean id
            
            ConfigElement className = actionElem.getChild("class");
            if ( className != null)
            {
              // Load the desktop action class, create a new instance
              
              Object actionObj = null;
              
              try
              {
                // Create a new desktop action instance
                
                actionObj = Class.forName(className.getValue()).newInstance();
                
                // Make sure the object is a desktop action
                
                if ( actionObj instanceof DesktopAction)
                {
                  // Initialize the desktop action
                  
                  DesktopAction deskAction = (DesktopAction) actionObj;
                  deskAction.initializeAction(globalConfig, actionElem, fileSys);
                  
                  // Add the action to the list of desktop actions
                  
                  desktopActions.addAction(deskAction);
                  
                  // DEBUG
                  
                  if ( logger.isDebugEnabled())
                    logger.debug("Added desktop action " + deskAction.getName());
                }
                else
                  throw new AlfrescoRuntimeException("Desktop action does not extend DesktopAction class, " + className.getValue());
              }
              catch ( ClassNotFoundException ex)
              {
                throw new AlfrescoRuntimeException("Desktop action class not found, " + className.getValue());
              }
              catch (IllegalAccessException ex)
              {
                throw new AlfrescoRuntimeException("Failed to create desktop action instance, " + className.getValue(), ex);
              }
              catch ( InstantiationException ex)
              {
                throw new AlfrescoRuntimeException("Failed to create desktop action instance, " + className.getValue(), ex);
              }
              catch (DesktopActionException ex)
              {
                throw new AlfrescoRuntimeException("Failed to initialize desktop action", ex);
              }
            }
          }
          else if ( actionElem.getName().equals("global") == false)
            throw new AlfrescoRuntimeException("Invalid configuration element in desktopActions section, " + actionElem.getName());
        }
      }
  
      // Return the desktop actions list
      
      return desktopActions;
  }
  
    /**
     * Parse the platforms attribute returning the set of platform ids
     * 
     * @param platformStr String
     * @return EnumSet<PlatformType>
     */
    private final EnumSet<Platform.Type> parsePlatformString(String platformStr)
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
        // Check if the name has already been set

        if (m_localName != null)
            return m_localName;

        // Find the local server name

        String srvName = null;

        if (getPlatformType() == Platform.Type.WINDOWS)
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

        if (getPlatformType() == Platform.Type.WINDOWS)
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
}
