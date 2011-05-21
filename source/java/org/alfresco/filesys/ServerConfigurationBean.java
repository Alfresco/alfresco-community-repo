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
import java.util.StringTokenizer;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigLookupContext;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.config.element.GenericConfigElement;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.alfresco.DesktopAction;
import org.alfresco.filesys.alfresco.DesktopActionException;
import org.alfresco.filesys.alfresco.DesktopActionTable;
import org.alfresco.filesys.avm.AVMContext;
import org.alfresco.filesys.avm.AVMDiskDriver;
import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.jlan.ftp.FTPConfigSection;
import org.alfresco.jlan.ftp.FTPPath;
import org.alfresco.jlan.ftp.InvalidPathException;
import org.alfresco.jlan.netbios.NetBIOSSession;
import org.alfresco.jlan.netbios.RFCNetBIOSProtocol;
import org.alfresco.jlan.netbios.win32.Win32NetBIOS;
import org.alfresco.jlan.oncrpc.nfs.NFSConfigSection;
import org.alfresco.jlan.server.auth.ICifsAuthenticator;
import org.alfresco.jlan.server.auth.acl.ACLParseException;
import org.alfresco.jlan.server.auth.acl.AccessControl;
import org.alfresco.jlan.server.auth.acl.AccessControlList;
import org.alfresco.jlan.server.auth.acl.AccessControlParser;
import org.alfresco.jlan.server.auth.acl.InvalidACLTypeException;
import org.alfresco.jlan.server.auth.passthru.DomainMapping;
import org.alfresco.jlan.server.auth.passthru.RangeDomainMapping;
import org.alfresco.jlan.server.auth.passthru.SubnetDomainMapping;
import org.alfresco.jlan.server.config.CoreServerConfigSection;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.SecurityConfigSection;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.core.ShareType;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FilesystemsConfigSection;
import org.alfresco.jlan.server.thread.ThreadRequestPool;
import org.alfresco.jlan.smb.server.CIFSConfigSection;
import org.alfresco.jlan.util.IPAddress;
import org.alfresco.jlan.util.MemorySize;
import org.alfresco.jlan.util.Platform;
import org.alfresco.jlan.util.StringList;
import org.alfresco.jlan.util.X64;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.repo.security.authentication.ntlm.NLTMAuthenticator;

/**
 * Alfresco File Server Configuration Bean Class
 * 
 * @author gkspencer
 */
public class ServerConfigurationBean extends AbstractServerConfigurationBean {

  // Filesystem configuration constants
  
  private static final String ConfigArea        = "file-servers";
  private static final String ConfigCIFS        = "CIFS Server";
  private static final String ConfigFTP         = "FTP Server";
  private static final String ConfigNFS         = "NFS Server";
  private static final String ConfigFilesystems = "Filesystems";
  private static final String ConfigSecurity    = "Filesystem Security";
  private static final String ConfigCoreServer  = "Server Core";

  // Server configuration bean name
  
  public static final String SERVER_CONFIGURATION = "fileServerConfiguration";
  
  // Configuration service
  private ConfigService m_configService;
  
  private ConfigLookupContext configCtx;
  
  
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
   * Set the configuration service
   * 
   * @param configService ConfigService
   */
  public void setConfigService(ConfigService configService)
  {
      m_configService = configService;
  }

  /**
   * Initialize the configuration using the configuration service
   */
  public void init()
  {
      // Check that all required properties have been set
      if (m_configService == null)
      {
          throw new AlfrescoRuntimeException("Property 'configService' not set");
      }
      
      // Create the configuration context
      configCtx = new ConfigLookupContext(ConfigArea);
      
      super.init();
  }
  
  /**
   * Process the CIFS server configuration
   */
  protected void processCIFSServerConfig()
  {
      processCIFSServerConfig(m_configService.getConfig(ConfigCIFS, configCtx));
  }

  /**
   * Process the CIFS server configuration
   * 
   * @param config Config
   */
  protected void processCIFSServerConfig(Config config)
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
        // Check if native code calls should be disabled on Windows
          
        elem = config.getConfigElement( "disableNativeCode");
        if ( elem != null)
        {
          	// Disable native code calls so that the JNI DLL is not required
        	
          	cifsConfig.setNativeCodeDisabled( true);
          	m_disableNativeCode = true;
          	
          	// Warning
          	
          	logger.warn("CIFS server native calls disabled, JNI code will not be used");
        }
          
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
            
            if ( localDomain == null && ( getPlatformType() != Platform.Type.WINDOWS || isNativeCodeDisabled()))
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
            else if (!elem.getValue().equals(BIND_TO_IGNORE))
            {
  
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
            
            NTLMMode ntlmMode = NTLMMode.NONE;
            if (m_authenticationComponent instanceof NLTMAuthenticator)
            {
                ntlmMode = ((NLTMAuthenticator)m_authenticationComponent).getNTLMMode();
            }
            
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
            else if ( authType.equalsIgnoreCase( "custom"))
            {
            	// Get the authenticator class
            	
            	ConfigElement authClassElem = authElem.getChild("class");
            	authClass = authClassElem.getValue();
            }
            else
                throw new AlfrescoRuntimeException("Invalid authenticator type, " + authType);
  
            // Get the allow guest and map unknown user to guest settings
  
            boolean allowGuest = authElem.getChild("allowGuest") != null ? true : false;
  
            // Initialize and set the authenticator class
  
            cifsConfig.setAuthenticator(authClass, authElem, ICifsAuthenticator.USER_MODE, allowGuest);
        }
        else
          throw new AlfrescoRuntimeException("CIFS authenticator not specified");
        
        // Check if the host announcer should be enabled
  
        elem = config.getConfigElement("hostAnnounce");
        if (elem != null)
        {
        	// Check if the host announcer has been disabled
        	
        	String enabled = elem.getAttribute("enabled");
            if ( enabled != null && enabled.equalsIgnoreCase( "false"))
            {
            	// Switch off the host announcer
            	
            	cifsConfig.setHostAnnouncer( false);
            	
            	// Log that host announcements are not enabled
            	
            	logger.info("Host announcements not enabled");
            }
            else
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
              if (bindto != null && bindto.length() > 0 && !bindto.equals(BIND_TO_IGNORE))
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
        if ( attr != null && attr.length() > 0 && !attr.equals(BIND_TO_IGNORE)) {
        
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
            
            // Check if IPv6 support should be enabled
            
        	String ipv6 = elem.getAttribute("ipv6");
            if ( ipv6 != null && ipv6.equalsIgnoreCase( "enabled"))
            {
            	try
            	{
	            	// Use the IPv6 bind all address
	            	
	            	cifsConfig.setSMBBindAddress( InetAddress.getByName( "::"));
	            	
	            	// DEBUG
	            	
	            	if ( logger.isInfoEnabled())
	            		logger.info("Enabled CIFS IPv6 bind address for native SMB");
            	}
            	catch ( UnknownHostException ex)
            	{
            		throw new AlfrescoRuntimeException("Failed to enable IPv6 bind address, " + ex.getMessage());
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
                // Debug
                
            	if ( logger.isDebugEnabled())
            		logger.debug("Using older Netbios() API code");
                
                // Use the older NetBIOS API code
                
                cifsConfig.setWin32WinsockNetBIOS( false);
            }
            
            // Check if the current operating system is supported by the Win32
            // NetBIOS handler
  
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Windows")
                    && (osName.endsWith("95") == false && osName.endsWith("98") == false && osName.endsWith("ME") == false)
                    && isNativeCodeDisabled() == false)
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
        	// Check if the Win32 host announcer has been disabled
        	
        	String enabled = elem.getAttribute("enabled");
            if ( enabled != null && enabled.equalsIgnoreCase( "false"))
            {
            	// Switch off the Win32 host announcer
            	
            	cifsConfig.setWin32HostAnnouncer( false);
            	
            	// Log that host announcements are not enabled
            	
            	logger.info("Win32 host announcements not enabled");
            }
            else
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
  
        else if (cifsConfig.hasNetBIOSSMB() && getPlatformType() == Platform.Type.WINDOWS && isNativeCodeDisabled() == false)
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
        
        // Check if NIO based socket code should be disabled
        
        if ( config.getConfigElement( "disableNIO") != null) {
        		
        	// Disable NIO based code
        	
        	cifsConfig.setDisableNIOCode( true);
        		
        	// DEBUG
        		
        	if ( logger.isDebugEnabled())
        		logger.debug("NIO based code disabled for CIFS server");
        }
        
		// Check if a session timeout is configured
		
		elem = config.getConfigElement("sessionTimeout");
		if ( elem != null) {
			
			// Validate the session timeout value

			String sessTmo = elem.getValue();
			if ( sessTmo != null && sessTmo.length() > 0) {
				try {
					
					// Convert the timeout value to milliseconds
					
					int tmo = Integer.parseInt(sessTmo);
					if ( tmo < 0 || tmo > MaxSessionTimeout)
						throw new AlfrescoRuntimeException("Session timeout out of range (0 - " + MaxSessionTimeout + ")");
					
					// Convert the session timeout to milliseconds
					
					cifsConfig.setSocketTimeout( tmo * 1000);
				}
				catch (NumberFormatException ex) {
					throw new AlfrescoRuntimeException("Invalid session timeout value, " + sessTmo);
				}
			}
			else
				throw new AlfrescoRuntimeException("Session timeout value not specified");
		}
      }
      catch ( InvalidConfigurationException ex)
      {
        throw new AlfrescoRuntimeException( ex.getMessage());
      }
  }

  /**
   * Process the FTP server configuration
   */
  protected void processFTPServerConfig()
  {
      processFTPServerConfig(m_configService.getConfig(ConfigFTP, configCtx));
  }

  /**
   * Process the FTP server configuration
   * 
   * @param config Config
   */
  protected void processFTPServerConfig(Config config)
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
        if ( elem != null && !elem.getValue().equals(BIND_TO_IGNORE)) {
        
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
            
            NTLMMode ntlmMode = NTLMMode.NONE;
            if (m_authenticationComponent instanceof NLTMAuthenticator)
            {
                ntlmMode = ((NLTMAuthenticator)m_authenticationComponent).getNTLMMode();
            }
            
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
            	// Do nothing, uses the authentication component as it has the plaintext password from the user
            }
            else if ( authType.equalsIgnoreCase( "custom"))
            {
            	// Get the authenticator class
            	
            	ConfigElement authClassElem = authElem.getChild("class");
            	authClass = authClassElem.getValue();
            }
            else
                throw new AlfrescoRuntimeException("Invalid authenticator type, " + authType);

            // Initialize and set the authenticator class

            ftpConfig.setAuthenticator(authClass, authElem);
        }
        else
          throw new AlfrescoRuntimeException("FTP authenticator not specified");
        
		// Check if a data port range has been specified

		elem = config.getConfigElement("dataPorts");
		if ( elem != null) {

			// Split the value string into from and to range strings

			StringTokenizer tok = new StringTokenizer( elem.getValue(), ":");
			if ( tok.countTokens() != 2)
				throw new InvalidConfigurationException( "Invalid FTP data port range, specify as 'n:n'");

			String rangeFromStr = tok.nextToken();
			String rangeToStr   = tok.nextToken();
			
			// Validate the from/to data port range values
			
			int rangeFrom = -1;
			int rangeTo = -1;

			if ( rangeFromStr != null && rangeFromStr.length() > 0) {

				// Validate the range string

				try {
					rangeFrom = Integer.parseInt(rangeFromStr);
				}
				catch (NumberFormatException ex) {
					throw new InvalidConfigurationException("Invalid FTP range from value, " + rangeFromStr);
				}
			}

			// Check for the to port range value

			if ( rangeToStr != null && rangeToStr.length() > 0) {

				// Validate the range string

				try {
					rangeTo = Integer.parseInt(rangeToStr);
				}
				catch (NumberFormatException ex) {
					throw new InvalidConfigurationException("Invalid FTP range to value, " + rangeToStr);
				}
			}

			// Validate the data port range values

			if ( rangeFrom != 0 && rangeTo != 0) {

				// Validate the FTp data port range
				
				if ( rangeFrom == -1 || rangeTo == -1)
					throw new InvalidConfigurationException("FTP data port range from/to must be specified");
	
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
      }
      catch (InvalidConfigurationException ex)
      {
        throw new AlfrescoRuntimeException( ex.getMessage());
      }
      
  }

  /**
   * Process the NFS server configuration
   */
  protected void processNFSServerConfig()
  {
      processNFSServerConfig(m_configService.getConfig(ConfigNFS, configCtx));
  }

  /**
   * Process the NFS server configuration
   * 
   * @param config Config
   */
  protected void processNFSServerConfig(Config config)
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
      
        //  Check for an RPC registration port
        
        elem = config.getConfigElement("RPCRegisterPort");
        if ( elem != null) {
          try {
            nfsConfig.setRPCRegistrationPort( Integer.parseInt(elem.getValue()));
            if ( nfsConfig.getRPCRegistrationPort() <= 0 || nfsConfig.getRPCRegistrationPort() >= 65535)
              throw new AlfrescoRuntimeException("RPC registration port out of valid range");
          }
          catch (NumberFormatException ex) {
            throw new AlfrescoRuntimeException("Invalid RPC registration port");
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
        	// Default RPC authenticator class
        	
        	String authClass = "org.alfresco.filesys.auth.nfs.AlfrescoRpcAuthenticator";
        	
           	// Check if a custom NFS authentictor class has been specified
            	
           	ConfigElement authClassElem = elem.getChild("class");
           	if ( authClassElem != null)
           		authClass = authClassElem.getValue();
       	
            // Create the RPC authenticator
            
            nfsConfig.setRpcAuthenticator( authClass, elem);
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
   */
  protected void processFilesystemsConfig()
  {
      processFilesystemsConfig(m_configService.getConfig(ConfigFilesystems, configCtx));
  }

  /**
   * Process the filesystems configuration
   * 
   * @param config Config
   */
  protected void processFilesystemsConfig(Config config)
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
                    filesys.setConfiguration( this);

                    // Check if the filesystem uses the file state cache, if so then add to the file state reaper
                    
                    if ( filesysContext.hasStateCache()) {
                        
                        // Register the state cache with the reaper thread
                        
                        fsysConfig.addFileStateCache( filesysName, filesysContext.getStateCache());
                    }

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

                    // Check if filesyststem debug flags are enabled
                    
                    ConfigElement filesysDbgElem = elem.getChild("debug");
                    if (filesysDbgElem != null)
                    {
                        // Check for filesystem debug flags
              
                        String flags = filesysDbgElem.getAttribute("flags");
              
                        // Set the filesystem debug flags
              
                        filesysContext.setDebug( flags);
                    }
                    
                    // Create the shared filesystem

                    filesys = new DiskSharedDevice(filesysName, filesysDriver, filesysContext);
                    filesys.setConfiguration( this);

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

                    // Check if the filesystem uses the file state cache, if so then add to the file state reaper
                    
                    if ( filesysContext.hasStateCache()) {
                        
                        // Register the state cache with the reaper thread
                        
                        fsysConfig.addFileStateCache( filesysName, filesysContext.getStateCache());
                    }

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
                    avmContext.enableStateCache( true);
                  
                    // Create the shared filesystem
                  
                    DiskSharedDevice filesys = new DiskSharedDevice( storeName, avmDriver, avmContext);
                    filesys.setConfiguration( this);
                    
                    fsysConfig.addShare( filesys);
                    
                    // DEBUG
                    
                    if ( logger.isDebugEnabled())
                      logger.debug( "Added AVM share " + storeName);
            }
          }
        }
      }
      
      // Check for the home folder filesystem
      
      ConfigElement homeElem = config.getConfigElement("homeFolder");
      
      if ( homeElem != null)
      {
          try
          {
              //  Initialize the home folder share mapper
              
              secConfig.setShareMapper( "org.alfresco.filesys.alfresco.HomeShareMapper", homeElem);   
              
              // Debug
              
              if ( logger.isDebugEnabled())
                  logger.debug("Using home folder share mapper");
          }
          catch (InvalidConfigurationException ex)
          {
              throw new AlfrescoRuntimeException("Failed to initialize home folder share mapper", ex);
          }
      }
  }

  /**
   * Process the security configuration
   */
  protected void processSecurityConfig()
  {
      processSecurityConfig(m_configService.getConfig(ConfigSecurity, configCtx));
  }

  /**
   * Process the security configuration
   * 
   * @param config Config
   */
  protected void processSecurityConfig(Config config)
  {
      // Create the security configuration section
    
      SecurityConfigSection secConfig = new SecurityConfigSection( this);
      
      try
      {
    	  // Check if ACL manager debugging is enabled
    	  
    	  GenericConfigElement params = new GenericConfigElement( "");
    	  
    	  if ( config.getConfigElement("aclDebug") != null)
    		  params.addChild( new GenericConfigElement("debug"));
    		  
    	  // Use the default ACL manager
      
    	  secConfig.setAccessControlManager( "org.alfresco.jlan.server.auth.acl.DefaultAccessControlManager", params);
      }
      catch ( Exception ex) {
    	  throw new AlfrescoRuntimeException("Failed to set ACL manager", ex);
      }
      
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

          //  Check if the share mapper type has been specified
        	
          String mapperType = mapperElem.getAttribute( "type");
          String mapperClass = null;
          
          if ( mapperType.equalsIgnoreCase( "multi-tenant"))
        	  mapperClass = "org.alfresco.filesys.alfresco.MultiTenantShareMapper";
          else if ( mapperType.equalsIgnoreCase( "home-folder"))
        	  mapperClass = "org.alfresco.filesys.alfresco.HomeShareMapper";
          else
          {
	          //  Get the share mapper class
	          
	          ConfigElement classElem = mapperElem.getChild( "class");
	          if ( classElem == null)
	            throw new InvalidConfigurationException("Share mapper class not specified");
	          
	          mapperClass = classElem.getValue();
          }
          
          //  Initialize the share mapper
          
          secConfig.setShareMapper(mapperClass, mapperElem);   
        }
        else
        {
      	  // Check if the tenant service is enabled
      	  
      	  if ( m_tenantService != null && m_tenantService.isEnabled())
      	  {
      		  // Initialize the multi-tenancy share mapper
      		  
              secConfig.setShareMapper("org.alfresco.filesys.alfresco.MultiTenantShareMapper", new GenericConfigElement("shareMapper"));   
      	  }
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
   * Process the core server configuration
   * 
   * @param config Config
   * @exception InvalidConfigurationException
   */
  protected void processCoreServerConfig()
    throws InvalidConfigurationException
  {
      processCoreServerConfig(m_configService.getConfig(ConfigCoreServer, configCtx));
  }
  
  /**
   * Process the core server configuration
   * 
   * @param config Config
   * @exception InvalidConfigurationException
   */
  protected void processCoreServerConfig(Config config)
  	throws InvalidConfigurationException
  {
		// Create the core server configuration section

		CoreServerConfigSection coreConfig = new CoreServerConfigSection(this);

		// Check if the server core element has been specified

		if ( config == null) {
			
			// Configure a default memory pool
			
			coreConfig.setMemoryPool( DefaultMemoryPoolBufSizes, DefaultMemoryPoolInitAlloc, DefaultMemoryPoolMaxAlloc);
			
			// Configure a default thread pool size
			
			coreConfig.setThreadPool( DefaultThreadPoolInit, DefaultThreadPoolMax);
			return;
		}

		// Check if the thread pool size has been specified
		
		ConfigElement elem = config.getConfigElement("threadPool");
		if ( elem != null) {
			
			// Get the initial thread pool size
			
			String initSizeStr = elem.getAttribute("init");
			if ( initSizeStr == null || initSizeStr.length() == 0)
				throw new InvalidConfigurationException("Thread pool initial size not specified");
			
			// Validate the initial thread pool size
			
			int initSize = 0;
			
			try {
				initSize = Integer.parseInt( initSizeStr);
			}
			catch (NumberFormatException ex) {
				throw new InvalidConfigurationException("Invalid thread pool size value, " + initSizeStr);
			}
			
			// Range check the thread pool size
			
			if ( initSize < ThreadRequestPool.MinimumWorkerThreads)
				throw new InvalidConfigurationException("Thread pool size below minimum allowed size");
			
			if ( initSize > ThreadRequestPool.MaximumWorkerThreads)
				throw new InvalidConfigurationException("Thread pool size above maximum allowed size");
			
			// Get the maximum thread pool size
			
			String maxSizeStr = elem.getAttribute("max");
			int maxSize = initSize;
			
			if ( maxSizeStr.length() > 0) {
				
				// Validate the maximum thread pool size
				
				try {
					maxSize = Integer.parseInt( maxSizeStr);
				}
				catch (NumberFormatException ex) {
					throw new InvalidConfigurationException(" Invalid thread pool maximum size value, " + maxSizeStr);
				}
				
				// Range check the maximum thread pool size
				
				if ( maxSize < ThreadRequestPool.MinimumWorkerThreads)
					throw new InvalidConfigurationException("Thread pool maximum size below minimum allowed size");
				
				if ( maxSize > ThreadRequestPool.MaximumWorkerThreads)
					throw new InvalidConfigurationException("Thread pool maximum size above maximum allowed size");
				
				if ( maxSize < initSize)
					throw new InvalidConfigurationException("Initial size is larger than maxmimum size");
			}
			else if ( maxSizeStr != null)
				throw new InvalidConfigurationException("Thread pool maximum size not specified");
			
			// Configure the thread pool
			
			coreConfig.setThreadPool( initSize, maxSize);
		}
		else {
			
			// Configure a default thread pool size
			
			coreConfig.setThreadPool( DefaultThreadPoolInit, DefaultThreadPoolMax);
		}
		
		// Check if thread pool debug output is enabled
		
		if ( config.getConfigElement( "threadPoolDebug") != null)
			coreConfig.getThreadPool().setDebug( true);
		
		// Check if the memory pool configuration has been specified
		
		elem = config.getConfigElement( "memoryPool");
		if ( elem != null) {
			
			// Check if the packet sizes/allocations have been specified

			ConfigElement pktElem = elem.getChild( "packetSizes");
			if ( pktElem != null) {

				// Calculate the array size for the packet size/allocation arrays
				
				int elemCnt = pktElem.getChildCount();
				
				// Create the packet size, initial allocation and maximum allocation arrays
				
				int[] pktSizes  = new int[elemCnt];
				int[] initSizes = new int[elemCnt];
				int[] maxSizes  = new int[elemCnt];
				
				int elemIdx = 0;
				
				// Process the packet size elements

				List<ConfigElement> pktSizeList = pktElem.getChildren();
				for ( int i = 0; i < pktSizeList.size(); i++) {
					
					// Get the current element
					
					ConfigElement curChild = pktSizeList.get( i);
					if ( curChild.getName().equals( "packet")) {
						
						// Get the packet size
						
						int pktSize   = -1;
						int initAlloc = -1;
						int maxAlloc  = -1;
						
						String pktSizeStr = curChild.getAttribute("size");
						if ( pktSizeStr == null || pktSizeStr.length() == 0)
							throw new InvalidConfigurationException("Memory pool packet size not specified");
						
						// Parse the packet size
						
						try {
							pktSize = MemorySize.getByteValueInt( pktSizeStr);
						}
						catch ( NumberFormatException ex) {
							throw new InvalidConfigurationException("Memory pool packet size, invalid size value, " + pktSizeStr);
						}

						// Make sure the packet sizes have been specified in ascending order
						
						if ( elemIdx > 0 && pktSizes[elemIdx - 1] >= pktSize)
							throw new InvalidConfigurationException("Invalid packet size specified, less than/equal to previous packet size");
						
						// Get the initial allocation for the current packet size
						
						String initSizeStr = curChild.getAttribute("init");
						if ( initSizeStr == null || initSizeStr.length() == 0)
							throw new InvalidConfigurationException("Memory pool initial allocation not specified");
						
						// Parse the initial allocation
						
						try {
							initAlloc = Integer.parseInt( initSizeStr);
						}
						catch (NumberFormatException ex) {
							throw new InvalidConfigurationException("Invalid initial allocation, " + initSizeStr);
						}
						
						// Range check the initial allocation
						
						if ( initAlloc < MemoryPoolMinimumAllocation)
							throw new InvalidConfigurationException("Initial memory pool allocation below minimum of " + MemoryPoolMinimumAllocation);
						
						if ( initAlloc > MemoryPoolMaximumAllocation)
							throw new InvalidConfigurationException("Initial memory pool allocation above maximum of " + MemoryPoolMaximumAllocation);
						
						// Get the maximum allocation for the current packet size

						String maxSizeStr = curChild.getAttribute("max");
						if ( maxSizeStr == null || maxSizeStr.length() == 0)
							throw new InvalidConfigurationException("Memory pool maximum allocation not specified");
						
						// Parse the maximum allocation
						
						try {
							maxAlloc = Integer.parseInt( maxSizeStr);
						}
						catch (NumberFormatException ex) {
							throw new InvalidConfigurationException("Invalid maximum allocation, " + maxSizeStr);
						}

						// Range check the maximum allocation
						
						if ( maxAlloc < MemoryPoolMinimumAllocation)
							throw new InvalidConfigurationException("Maximum memory pool allocation below minimum of " + MemoryPoolMinimumAllocation);
						
						if ( initAlloc > MemoryPoolMaximumAllocation)
							throw new InvalidConfigurationException("Maximum memory pool allocation above maximum of " + MemoryPoolMaximumAllocation);

						// Set the current packet size elements
						
						pktSizes[elemIdx]  = pktSize;
						initSizes[elemIdx] = initAlloc;
						maxSizes[elemIdx]  = maxAlloc;
						
						elemIdx++;
					}
				}
				
				// Check if all elements were used in the packet size/allocation arrays
				
				if ( elemIdx < pktSizes.length) {
					
					// Re-allocate the packet size/allocation arrays
					
					int[] newPktSizes  = new int[elemIdx];
					int[] newInitSizes = new int[elemIdx];
					int[] newMaxSizes  = new int[elemIdx];
					
					// Copy the values to the shorter arrays
					
					System.arraycopy(pktSizes, 0, newPktSizes, 0, elemIdx);
					System.arraycopy(initSizes, 0, newInitSizes, 0, elemIdx);
					System.arraycopy(maxSizes, 0, newMaxSizes, 0, elemIdx);
					
					// Move the new arrays into place
					
					pktSizes  = newPktSizes;
					initSizes = newInitSizes;
					maxSizes  = newMaxSizes;
				}
				
				// Configure the memory pool
				
				coreConfig.setMemoryPool( pktSizes, initSizes, maxSizes);
			}
		}
		else {
			
			// Configure a default memory pool
			
			coreConfig.setMemoryPool( DefaultMemoryPoolBufSizes, DefaultMemoryPoolInitAlloc, DefaultMemoryPoolMaxAlloc);
		}
  }

  /**
   * Process an access control sub-section and return the access control list
   * 
   * @param secConfig SecurityConfigSection
   * @param aclsElem ConfigElement
   */
  protected AccessControlList processAccessControlList(SecurityConfigSection secConfig, ConfigElement aclsElem)
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
  protected DesktopActionTable processDesktopActions(ConfigElement deskActionElem, DiskSharedDevice fileSys)
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
   * Parse the CIFS server config section to get the WINS server details, if available
   * 
   * @param config Config
   */
  protected void processWINSServerConfig( Config config)
  {
      // Check if WINS servers are configured
	  
      ConfigElement elem = config.getConfigElement("WINS");

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

          // Pass the setting to the NetBIOS session class

          NetBIOSSession.setDefaultWINSServer(primaryWINS);
      }
  }

  /**
   * Parse the CIFS server config section to get the WINS server details, if available
   */
  protected void processWINSServerConfig()
  {
      processWINSServerConfig(m_configService.getConfig(ConfigCIFS, configCtx));
  }
}
