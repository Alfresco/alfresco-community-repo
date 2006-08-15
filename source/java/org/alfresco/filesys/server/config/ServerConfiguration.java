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
package org.alfresco.filesys.server.config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import net.sf.acegisecurity.AuthenticationManager;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigLookupContext;
import org.alfresco.config.ConfigService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.ftp.FTPPath;
import org.alfresco.filesys.ftp.InvalidPathException;
import org.alfresco.filesys.netbios.NetBIOSName;
import org.alfresco.filesys.netbios.NetBIOSNameList;
import org.alfresco.filesys.netbios.NetBIOSSession;
import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.netbios.win32.Win32NetBIOS;
import org.alfresco.filesys.server.NetworkServer;
import org.alfresco.filesys.server.NetworkServerList;
import org.alfresco.filesys.server.auth.CifsAuthenticator;
import org.alfresco.filesys.server.auth.acl.ACLParseException;
import org.alfresco.filesys.server.auth.acl.AccessControl;
import org.alfresco.filesys.server.auth.acl.AccessControlList;
import org.alfresco.filesys.server.auth.acl.AccessControlManager;
import org.alfresco.filesys.server.auth.acl.AccessControlParser;
import org.alfresco.filesys.server.auth.acl.DefaultAccessControlManager;
import org.alfresco.filesys.server.auth.acl.InvalidACLTypeException;
import org.alfresco.filesys.server.core.DeviceContext;
import org.alfresco.filesys.server.core.DeviceContextException;
import org.alfresco.filesys.server.core.ShareMapper;
import org.alfresco.filesys.server.core.ShareType;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.core.SharedDeviceList;
import org.alfresco.filesys.server.filesys.DefaultShareMapper;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.DiskSharedDevice;
import org.alfresco.filesys.server.filesys.HomeShareMapper;
import org.alfresco.filesys.smb.ServerType;
import org.alfresco.filesys.smb.TcpipSMB;
import org.alfresco.filesys.smb.server.repo.ContentContext;
import org.alfresco.filesys.smb.server.repo.DesktopAction;
import org.alfresco.filesys.smb.server.repo.DesktopActionException;
import org.alfresco.filesys.smb.server.repo.DesktopActionTable;
import org.alfresco.filesys.util.IPAddress;
import org.alfresco.filesys.util.X64;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * <p>
 * Provides the configuration parameters for the network file servers.
 * 
 * @author Gary K. Spencer
 */
public class ServerConfiguration implements ApplicationListener
{
    // Debug logging

    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol");

    // Filesystem configuration constants
    
    private static final String ConfigArea        = "file-servers";
    private static final String ConfigCIFS        = "CIFS Server";
    private static final String ConfigFTP         = "FTP Server";
    private static final String ConfigFilesystems = "Filesystems";
    private static final String ConfigSecurity    = "Filesystem Security";

    // Server configuration bean name
    
    public static final String SERVER_CONFIGURATION = "fileServerConfiguration";
    
    // SMB/CIFS session debug type strings
    //
    // Must match the bit mask order
    
    private static final String m_sessDbgStr[] = { "NETBIOS", "STATE", "NEGOTIATE", "TREE", "SEARCH", "INFO", "FILE",
            "FILEIO", "TRANSACT", "ECHO", "ERROR", "IPC", "LOCK", "PKTTYPE", "DCERPC", "STATECACHE", "NOTIFY",
            "STREAMS", "SOCKET" };

    // FTP server debug type strings

    private static final String m_ftpDebugStr[] = { "STATE", "SEARCH", "INFO", "FILE", "FILEIO", "ERROR", "PKTTYPE",
            "TIMING", "DATAPORT", "DIRECTORY" };

    // Default FTP server port
    
    private static final int DefaultFTPServerPort = 21;
    
    // Default FTP anonymous account name
    
    private static final String DefaultFTPAnonymousAccount = "anonymous";
    
    // Platform types

    public enum PlatformType
    {
        Unknown, WINDOWS, LINUX, SOLARIS, MACOSX
    };

    // Token name to substitute current server name into the CIFS server name

    private static final String TokenLocalName = "${localname}";

    // Authentication manager

    private AuthenticationManager authenticationManager;

    // Configuration service

    private ConfigService configService;

    // Disk interface to use for shared filesystems
    
    private DiskInterface diskInterface;

    // Runtime platform type

    private PlatformType m_platform = PlatformType.Unknown;

    // Main server enable flags, to enable SMB, FTP and/or NFS server components

    private boolean m_smbEnable = true;
    private boolean m_ftpEnable = true;

    // Server name

    private String m_name;

    // Server type, used by the host announcer

    private int m_srvType = ServerType.WorkStation + ServerType.Server + ServerType.NTServer;

    // Active server list

    private NetworkServerList m_serverList;

    // Server comment

    private String m_comment;

    // Server domain

    private String m_domain;

    // Network broadcast mask string

    private String m_broadcast;

    //	NetBIOS ports

    private int m_nbNamePort     = RFCNetBIOSProtocol.NAME_PORT;
    private int m_nbSessPort     = RFCNetBIOSProtocol.PORT;
    private int m_nbDatagramPort = RFCNetBIOSProtocol.DATAGRAM;

  	//	Native SMB port
  	
  	private int m_tcpSMBPort = TcpipSMB.PORT;

    // Announce the server to network neighborhood, announcement interval in
    // minutes

    private boolean m_announce;
    private int m_announceInterval;

    // List of shared devices

    private SharedDeviceList m_shareList;

    // Authenticator, used to authenticate users and share connections.

    private CifsAuthenticator m_authenticator;

    // Share mapper

    private ShareMapper m_shareMapper;

    // Access control manager

    private AccessControlManager m_aclManager;

    // Global access control list, applied to all shares that do not have access controls

    private AccessControlList m_globalACLs;

    private boolean m_nbDebug = false;

    private boolean m_announceDebug = false;

    // Default session debugging setting

    private int m_sessDebug;

    // Flags to indicate if NetBIOS, native TCP/IP SMB and/or Win32 NetBIOS
    // should be enabled

    private boolean m_netBIOSEnable = true;
    private boolean m_tcpSMBEnable = false;
    private boolean m_win32NBEnable = false;

    // Address to bind the SMB server to, if null all local addresses are used

    private InetAddress m_smbBindAddress;

    // Address to bind the NetBIOS name server to, if null all addresses are used

    private InetAddress m_nbBindAddress;

    // WINS servers

    private InetAddress m_winsPrimary;
    private InetAddress m_winsSecondary;

    // Enable/disable Macintosh extension SMBs

    private boolean m_macExtensions;

    // --------------------------------------------------------------------------------
    // Win32 NetBIOS configuration
    //
    // Server name to register under Win32 NetBIOS, if not set the main server
    // name is used

    private String m_win32NBName;

    // LANA to be used for Win32 NetBIOS, if not specified the first available
    // is used

    private int m_win32NBLANA = -1;

    // Send out host announcements via the Win32 NetBIOS interface

    private boolean m_win32NBAnnounce = false;
    private int m_win32NBAnnounceInterval;
    
    // Use Winsock NetBIOS interface if true, else use the Netbios() API interface
    
    private boolean m_win32NBUseWinsock = true;

    // --------------------------------------------------------------------------------
    // FTP specific configuration parameters
    //
    // Bind address and FTP server port.

    private InetAddress m_ftpBindAddress;
    private int m_ftpPort = DefaultFTPServerPort;

    // Allow anonymous FTP access and anonymous FTP account name

    private boolean m_ftpAllowAnonymous;
    private String m_ftpAnonymousAccount;

    // FTP root path, if not specified defaults to listing all shares as the root

    private String m_ftpRootPath;

    // FTP server debug flags

    private int m_ftpDebug;

    // --------------------------------------------------------------------------------
    // Global server configuration
    //
    // Timezone name and offset from UTC in minutes

    private String m_timeZone;
    private int m_tzOffset;

    // JCE provider class name

    private String m_jceProviderClass;

    // Local server name and domain/workgroup name

    private String m_localName;
    private String m_localDomain;

    // flag to indicate successful initialization
    
    private boolean initialised;

    // Main authentication service, public API
    
    private AuthenticationService authenticationService;

    // Authentication component, for internal functions
    
    private AuthenticationComponent m_authenticationComponent;
    
    // Various services
    
    private NodeService m_nodeService;
    private PersonService m_personService;
    private TransactionService m_transactionService;

    /**
     * Class constructor
     */
    public ServerConfiguration()
    {
        // Allocate the shared device list

        m_shareList = new SharedDeviceList();

        // Use the default share mapper

        m_shareMapper = new DefaultShareMapper();

        try
        {
            m_shareMapper.initializeMapper(this, null);
        }
        catch (InvalidConfigurationException ex)
        {
            throw new AlfrescoRuntimeException("Failed to initialise share mapper", ex);
        }

        // Set the default access control manager

        m_aclManager = new DefaultAccessControlManager();
        m_aclManager.initialize(this, null);

        // Use the default timezone

        try
        {
            setTimeZone(TimeZone.getDefault().getID());
        }
        catch (Exception ex)
        {
            throw new AlfrescoRuntimeException("Failed to set timezone", ex);
        }

        // Allocate the active server list

        m_serverList = new NetworkServerList();
    }
    
    public void setAuthenticationManager(AuthenticationManager authenticationManager)
    {
        this.authenticationManager = authenticationManager;
    }

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setConfigService(ConfigService configService)
    {
        this.configService = configService;
    }

    public void setDiskInterface(DiskInterface diskInterface)
    {
        this.diskInterface = diskInterface;
    }

    public void setAuthenticationComponent(AuthenticationComponent component)
    {
        m_authenticationComponent = component;
    }

    public void setNodeService(NodeService service)
    {
        m_nodeService = service;
    }

    public void setPersonService(PersonService service)
    {
        m_personService = service;
    }

    public void setTransactionService(TransactionService service)
    {
        m_transactionService = service;
    }

    /**
     * @return Returns true if the configuration was fully initialised
     */
    public boolean isInitialised()
    {
        return initialised;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (event instanceof ContextRefreshedEvent)
        {
            init();
        }
    }
    
    /**
     * Initialize the configuration using the configuration service
     */
    public void init()
    {
        // check that all required properties have been set
        if (authenticationManager == null)
        {
            throw new AlfrescoRuntimeException("Property 'authenticationManager' not set");
        }
        else if (m_authenticationComponent == null)
        {
            throw new AlfrescoRuntimeException("Property 'authenticationComponent' not set");
        }
        else if (authenticationService == null)
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
        else if (diskInterface == null)
        {
            throw new AlfrescoRuntimeException("Property 'diskInterface' not set");
        }
        else if (configService == null)
        {
            throw new AlfrescoRuntimeException("Property 'configService' not set");
        }
        
        // Create the configuration context

        ConfigLookupContext configCtx = new ConfigLookupContext(ConfigArea);

        // Set the platform type

        determinePlatformType();

        // Initialize the filesystems

        boolean filesysInitOK = false;
        Config config = null;
        
        try
        {
            // Process the filesystems configuration

            config = configService.getConfig(ConfigFilesystems, configCtx);
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
	
	            config = configService.getConfig(ConfigCIFS, configCtx);
	            processCIFSServerConfig(config);
	
	            // Process the security configuration
	
	            config = configService.getConfig(ConfigSecurity, configCtx);
	            processSecurityConfig(config);
	
	            // Log the successful startup
	            
	            logger.info("CIFS server started");
	        }
	        catch (UnsatisfiedLinkError ex)
	        {
	            // Error accessing the Win32NetBIOS DLL code
	
	            logger.error("Error accessing Win32 NetBIOS, check DLL is on the path");
	
	            // Disable the CIFS server
	
	            setNetBIOSSMB(false);
	            setTcpipSMB(false);
	            setWin32NetBIOS(false);
	
	            setSMBServerEnabled(false);
	        }
	        catch (Throwable ex)
	        {
	            // Configuration error
	
	            logger.error("CIFS server configuration error, " + ex.getMessage(), ex);
	
	            // Disable the CIFS server
	
	            setNetBIOSSMB(false);
	            setTcpipSMB(false);
	            setWin32NetBIOS(false);
	
	            setSMBServerEnabled(false);
	        }

	        // Initialize the FTP server

        	try
	        {
	            // Process the FTP server configuration
	
	            config = configService.getConfig(ConfigFTP, configCtx);
	            processFTPServerConfig(config);
	            
	            // Log the successful startup
	            
	            logger.info("FTP server started");
	        }
        	catch (Exception ex)
        	{
	            // Configuration error
        		
	            logger.error("FTP server configuration error, " + ex.getMessage(), ex);
        	}        		
    	}
        else
        {
        	// Log the error
        	
        	logger.error("CIFS and FTP servers not started due to filesystem initialization error");
        }
    }

    /**
     * Determine the platform type
     */
    private final void determinePlatformType()
    {
        // Get the operating system type

        String osName = System.getProperty("os.name");

        if (osName.startsWith("Windows"))
            m_platform = PlatformType.WINDOWS;
        else if (osName.equalsIgnoreCase("Linux"))
            m_platform = PlatformType.LINUX;
        else if (osName.startsWith("Mac OS X"))
            m_platform = PlatformType.MACOSX;
        else if (osName.startsWith("Solaris") || osName.startsWith("SunOS"))
            m_platform = PlatformType.SOLARIS;
    }

    /**
     * Return the platform type
     * 
     * @return PlatformType
     */
    public final PlatformType getPlatformType()
    {
        return m_platform;
    }

    /**
     * Process the CIFS server configuration
     * 
     * @param config Config
     */
    private final void processCIFSServerConfig(Config config)
    {
        // If the configuration section is not valid then CIFS is disabled
        
        if ( config == null || config.getConfigElements().isEmpty())
        {
            setSMBServerEnabled(false);
            return;
        }
            
        // Get the network broadcast address
        //
        // Note: We need to set this first as the call to getLocalDomainName() may use a NetBIOS
        // name lookup, so the broadcast mask must be set before then.

        ConfigElement elem = config.getConfigElement("broadcast");
        if (elem != null)
        {

            // Check if the broadcast mask is a valid numeric IP address

            if (IPAddress.isNumericAddress(elem.getValue()) == false)
                throw new AlfrescoRuntimeException("Invalid broadcast mask, must be n.n.n.n format");

            // Set the network broadcast mask

            setBroadcastMask(elem.getValue());
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

            if (hostName.equals(srvName))
                throw new AlfrescoRuntimeException("CIFS server name must be unique");
        }

        // Set the CIFS server name

        setServerName(hostName.toUpperCase());

        // Get the domain/workgroup name

        String domain = elem.getAttribute("domain");
        if (domain != null && domain.length() > 0)
        {
            // Set the domain/workgroup name

            setDomainName(domain.toUpperCase());
        }
        else
        {
            // Get the local domain/workgroup name

            String localDomain = getLocalDomainName();
            
            if ( localDomain == null && getPlatformType() != PlatformType.WINDOWS)
            {
                // Use a default domain/workgroup name
                
                localDomain = "WORKGROUP";
                
                // Output a warning
                
                logger.error("Failed to get local domain/workgroup name, using default of " + localDomain);
                logger.error("(This may be due to firewall settings or incorrect <broadcast> setting)");
            }
            
            // Set the local domain/workgroup that the CIFS server belongs to
            
            setDomainName( localDomain);
        }

        // Check for a server comment

        elem = config.getConfigElement("comment");
        if (elem != null)
            setComment(elem.getValue());

        // Check for a bind address

        elem = config.getConfigElement("bindto");
        if (elem != null)
        {

            // Validate the bind address

            String bindText = elem.getValue();

            try
            {

                // Check the bind address

                InetAddress bindAddr = InetAddress.getByName(bindText);

                // Set the bind address for the server

                setSMBBindAddress(bindAddr);
            }
            catch (UnknownHostException ex)
            {
                throw new AlfrescoRuntimeException("Invalid CIFS server bind address");
            }
        }

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
                    setHostAnnounceInterval(Integer.parseInt(interval));
                }
                catch (NumberFormatException ex)
                {
                    throw new AlfrescoRuntimeException("Invalid host announcement interval");
                }
            }

            // Check if the domain name has been set, this is required if the
            // host announcer is enabled

            if (getDomainName() == null)
                throw new AlfrescoRuntimeException("Domain name must be specified if host announcement is enabled");

            // Enable host announcement

            setHostAnnouncer(true);
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

                EnumSet<PlatformType> enabledPlatforms = parsePlatformString(platformsStr);
                if (enabledPlatforms.contains(getPlatformType()))
                    platformOK = true;
            }
            else
            {
                // No restriction on platforms

                platformOK = true;
            }

            // Enable the NetBIOS SMB support, if enabled for this platform

            setNetBIOSSMB(platformOK);

            // Parse/check NetBIOS settings, if enabled
            
            if ( hasNetBIOSSMB())
            {
	            // Check if the broadcast mask has been specified
	
	            if (getBroadcastMask() == null)
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
	
	                    setNetBIOSBindAddress(bindAddr);
	                }
	                catch (UnknownHostException ex)
	                {
	                    throw new AlfrescoRuntimeException("Invalid NetBIOS bind address");
	                }
	            }
	            else if (hasSMBBindAddress())
	            {
	
	                // Use the SMB bind address for the NetBIOS name server
	
	                setNetBIOSBindAddress(getSMBBindAddress());
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
	
	            //	Check if the session port has been specified
				
				String portNum = elem.getAttribute("sessionPort");
				if ( portNum != null && portNum.length() > 0) {
					try {
						setNetBIOSSessionPort(Integer.parseInt(portNum));
						if ( getNetBIOSSessionPort() <= 0 || getNetBIOSSessionPort() >= 65535)
							throw new AlfrescoRuntimeException("NetBIOS session port out of valid range");
					}
					catch (NumberFormatException ex) {
						throw new AlfrescoRuntimeException("Invalid NetBIOS session port");
					}
				}
	
				//	Check if the name port has been specified
				
				portNum = elem.getAttribute("namePort");
				if ( portNum != null && portNum.length() > 0) {
					try {
						setNetBIOSNamePort(Integer.parseInt(portNum));
						if ( getNetBIOSNamePort() <= 0 || getNetBIOSNamePort() >= 65535)
							throw new AlfrescoRuntimeException("NetBIOS name port out of valid range");
					}
					catch (NumberFormatException ex) {
						throw new AlfrescoRuntimeException("Invalid NetBIOS name port");
					}
				}
	
				//	Check if the datagram port has been specified
				
				portNum = elem.getAttribute("datagramPort");
				if ( portNum != null && portNum.length() > 0) {
					try {
						setNetBIOSDatagramPort(Integer.parseInt(portNum));
						if ( getNetBIOSDatagramPort() <= 0 || getNetBIOSDatagramPort() >= 65535)
							throw new AlfrescoRuntimeException("NetBIOS datagram port out of valid range");
					}
					catch (NumberFormatException ex) {
						throw new AlfrescoRuntimeException("Invalid NetBIOS datagram port");
					}
				}
            }
        }
        else
        {

            // Disable NetBIOS SMB support

            setNetBIOSSMB(false);
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

                EnumSet<PlatformType> enabledPlatforms = parsePlatformString(platformsStr);
                if (enabledPlatforms.contains(getPlatformType()))
                    platformOK = true;
            }
            else
            {
                // No restriction on platforms

                platformOK = true;
            }

            // Enable the TCP/IP SMB support, if enabled for this platform

            setTcpipSMB(platformOK);
            
			//	Check if the port has been specified
			
			String portNum = elem.getAttribute("port");
			if ( portNum != null && portNum.length() > 0) {
				try {
					setTcpipSMBPort(Integer.parseInt(portNum));
					if ( getTcpipSMBPort() <= 0 || getTcpipSMBPort() >= 65535)
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

            setTcpipSMB(false);
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

                setWin32NetBIOSName(win32Name);
            }

            // Check if the Win32 NetBIOS LANA has been specified

            String lanaStr = elem.getAttribute("lana");
            if (lanaStr != null && lanaStr.length() > 0)
            {

                // Validate the LANA number

                int lana = -1;

                try
                {
                    lana = Integer.parseInt(lanaStr);
                }
                catch (NumberFormatException ex)
                {
                    throw new AlfrescoRuntimeException("Invalid win32 NetBIOS LANA specified");
                }

                // LANA should be in the range 0-255

                if (lana < 0 || lana > 255)
                    throw new AlfrescoRuntimeException("Invalid Win32 NetBIOS LANA number, " + lana);

                // Set the LANA number

                setWin32LANA(lana);
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
                
                setWin32WinsockNetBIOS( useWinsock);
            }
            
            // Force the older NetBIOS API code to be used on 64Bit Windows
            
            if ( useWinsockNetBIOS() == true && X64.isWindows64())
            {
                // Log a warning
                
                logger.warn("Using older Netbios() API code");
                
                // Use the older NetBIOS API code
                
                setWin32WinsockNetBIOS( false);
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
    
                    setWin32NetBIOS(true);
                }
                else
                {
                    logger.warn("No NetBIOS LANAs available");
                }
            }
            else
            {

                // Win32 NetBIOS not supported on the current operating system

                setWin32NetBIOS(false);
            }
        }
        else
        {

            // Disable Win32 NetBIOS

            setWin32NetBIOS(false);
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
                    setWin32HostAnnounceInterval(Integer.parseInt(interval));
                }
                catch (NumberFormatException ex)
                {
                    throw new AlfrescoRuntimeException("Invalid host announcement interval");
                }
            }

            // Check if the domain name has been set, this is required if the
            // host announcer is enabled

            if (getDomainName() == null)
                throw new AlfrescoRuntimeException("Domain name must be specified if host announcement is enabled");

            // Enable Win32 NetBIOS host announcement

            setWin32HostAnnouncer(true);
        }

        // Check if NetBIOS and/or TCP/IP SMB have been enabled

        if (hasNetBIOSSMB() == false && hasTcpipSMB() == false && hasWin32NetBIOS() == false)
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

            setPrimaryWINSServer(primaryWINS);
            if (secondaryWINS != null)
                setSecondaryWINSServer(secondaryWINS);

            // Pass the setting to the NetBIOS session class

            NetBIOSSession.setWINSServer(primaryWINS);
        }

        // Check if WINS is configured, if we are running on Windows and socket based NetBIOS is enabled

        else if (hasNetBIOSSMB() && getPlatformType() == PlatformType.WINDOWS)
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
                    
                    setPrimaryWINSServer(winsAddr);

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

            setSessionDebugFlags(sessDbg);
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
            setFTPServerEnabled(false);
            return;
        }
            
        //  Check for a bind address
        
        ConfigElement elem = config.getConfigElement("bindto");
        if ( elem != null) {
        
            //  Validate the bind address

            String bindText = elem.getValue();
        
            try {
            
                //  Check the bind address
            
                InetAddress bindAddr = InetAddress.getByName(bindText);
        
                //  Set the bind address for the FTP server
            
                setFTPBindAddress(bindAddr);
            }
            catch (UnknownHostException ex) {
                throw new AlfrescoRuntimeException("Invalid FTP bindto address, " + elem.getValue());
            }
        }

        //  Check for an FTP server port
    
        elem = config.getConfigElement("port");
        if ( elem != null) {
            try {
                setFTPPort(Integer.parseInt(elem.getValue()));
                if ( getFTPPort() <= 0 || getFTPPort() >= 65535)
                    throw new AlfrescoRuntimeException("FTP server port out of valid range");
            }
            catch (NumberFormatException ex) {
                throw new AlfrescoRuntimeException("Invalid FTP server port");
            }
        }
        else {
        
            //  Use the default FTP port
        
            setFTPPort(DefaultFTPServerPort);
        }
    
        //  Check if anonymous login is allowed
    
        elem = config.getConfigElement("allowAnonymous");
        if ( elem != null) {
        
            //  Enable anonymous login to the FTP server
        
            setAllowAnonymousFTP(true);
        
            //  Check if an anonymous account has been specified
        
            String anonAcc = elem.getAttribute("user");
            if ( anonAcc != null && anonAcc.length() > 0) {
            
                //  Set the anonymous account name
            
                setAnonymousFTPAccount(anonAcc);
            
                //  Check if the anonymous account name is valid
            
                if ( getAnonymousFTPAccount() == null || getAnonymousFTPAccount().length() == 0)
                    throw new AlfrescoRuntimeException("Anonymous FTP account invalid");
            }
            else {
            
                //  Use the default anonymous account name
            
                setAnonymousFTPAccount(DefaultFTPAnonymousAccount);
            }
        }
        else {
        
            //  Disable anonymous logins
        
            setAllowAnonymousFTP(false);
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
                
                setFTPRootPath(rootPath);
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
        
            setFTPDebug(ftpDbg);
        }
    }

    /**
     * Process the filesystems configuration
     * 
     * @param config Config
     */
    private final void processFilesystemsConfig(Config config)
    {
        // Check for the home folder filesystem
        
        ConfigElement homeElem = config.getConfigElement("homeFolder");
        
        if ( homeElem != null)
        {
            try
            {
                // Create the home folder share mapper
                
                HomeShareMapper shareMapper = new HomeShareMapper();
                shareMapper.initializeMapper( this, homeElem);
                
                // Use the home folder share mapper
                
                m_shareMapper = shareMapper;
                
                // Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("Using home folder share mapper");
            }
            catch (InvalidConfigurationException ex)
            {
                throw new AlfrescoRuntimeException("Failed to initialize home folder share mapper", ex);
            }
        }
        
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

        // Process the filesystems list
        
        if (filesysElems != null)
        {

            // Add the filesystems

            for (int i = 0; i < filesysElems.size(); i++)
            {

                // Get the current filesystem configuration

                ConfigElement elem = filesysElems.get(i);
                String filesysName = elem.getAttribute("name");

                try
                {
                    // Create a new filesystem driver instance and create a context for
                    // the new filesystem
                	
                    DiskInterface filesysDriver = this.diskInterface;
                    ContentContext filesysContext = (ContentContext) filesysDriver.createContext(elem);

                    // Check if an access control list has been specified

                    AccessControlList acls = null;
                    ConfigElement aclElem = elem.getChild("accessControl");

                    if (aclElem != null)
                    {

                        // Parse the access control list

                        acls = processAccessControlList(aclElem);
                    }
                    else if (hasGlobalAccessControls())
                    {

                        // Use the global access control list for this disk share

                        acls = getGlobalAccessControls();
                    }

                    // Check if change notifications are disabled

                    boolean changeNotify = elem.getChild("disableChangeNotification") == null ? true : false;

                    // Create the shared filesystem

                    DiskSharedDevice filesys = new DiskSharedDevice(filesysName, filesysDriver, filesysContext);

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

                    // Create the shared device and add to the list of available
                    // shared filesystems

                    addShare(filesys);
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
    }

    /**
     * Process the security configuration
     * 
     * @param config Config
     */
    private final void processSecurityConfig(Config config)
    {

        // Check if global access controls have been specified

        ConfigElement globalACLs = config.getConfigElement("globalAccessControl");
        if (globalACLs != null)
        {

            // Parse the access control list

            AccessControlList acls = processAccessControlList(globalACLs);
            if (acls != null)
                setGlobalAccessControls(acls);
        }

        // Check if a JCE provider class has been specified

        ConfigElement jceElem = config.getConfigElement("JCEProvider");
        if (jceElem != null)
        {

            // Set the JCE provider

            setJCEProvider(jceElem.getValue());
        }
        else
        {
            // Use the default Cryptix JCE provider
            
            setJCEProvider("cryptix.jce.provider.CryptixCrypto");
        }
        
        // Check if an authenticator has been specified

        ConfigElement authElem = config.getConfigElement("authenticator");
        if (authElem != null)
        {

            // Get the authenticator type, should be either 'local' or 'passthru'

            String authType = authElem.getAttribute("type");
            if (authType == null)
                authType = "alfresco";

            // Get the authentication component type
            
            NTLMMode ntlmMode = m_authenticationComponent.getNTLMMode();
            
            // Set the authenticator class to use

            CifsAuthenticator auth = null;

            if (authType.equalsIgnoreCase("passthru"))
            {
                // Check if the appropriate authentication component type is configured
                
                if ( ntlmMode != NTLMMode.NONE)
                    throw new AlfrescoRuntimeException("Wrong authentication setup for passthru authenticator");
                
                // Load the passthru authenticator dynamically
                
                auth = loadAuthenticatorClass("org.alfresco.filesys.server.auth.passthru.PassthruAuthenticator");
                if ( auth == null)
                    throw new AlfrescoRuntimeException("Failed to load passthru authenticator");
            }
            else if (authType.equalsIgnoreCase("alfresco"))
            {
                // Standard authenticator requires MD4 or passthru based authentication
                
                if ( ntlmMode == NTLMMode.NONE)
                    throw new AlfrescoRuntimeException("Wrong authentication setup for alfresco authenticator");
                
                // Load the Alfresco authenticator dynamically
                
                auth = loadAuthenticatorClass("org.alfresco.filesys.server.auth.ntlm.AlfrescoAuthenticator");
                if ( auth == null)
                    auth = loadAuthenticatorClass("org.alfresco.filesys.server.auth.AlfrescoAuthenticator");
                
                if ( auth == null)
                    throw new AlfrescoRuntimeException("Failed to load Alfresco authenticator");
            }
            else if( authType.equalsIgnoreCase("enterprise"))
            {
                // Load the Enterprise authenticator dynamically
                
                auth = loadAuthenticatorClass("org.alfresco.filesys.server.auth.EnterpriseCifsAuthenticator");
                
                if ( auth == null)
                    throw new AlfrescoRuntimeException("Failed to load Enterprise authenticator");
            }
            else
                throw new AlfrescoRuntimeException("Invalid authenticator type, " + authType);

            // Get the allow guest and map unknown user to guest settings

            boolean allowGuest = authElem.getChild("allowGuest") != null ? true : false;
            boolean mapGuest   = authElem.getChild("mapUnknownUserToGuest") != null ? true : false;

            // Initialize and set the authenticator class

            setAuthenticator(auth, authElem, allowGuest);
            auth.setMapToGuest( mapGuest);
        }
        else
        	throw new AlfrescoRuntimeException("Authenticator not specified");
    }

    /**
     * Process an access control sub-section and return the access control list
     * 
     * @param aclsElem ConfigElement
     */
    private final AccessControlList processAccessControlList(ConfigElement aclsElem)
    {

        // Check if there is an access control manager configured

        if (getAccessControlManager() == null)
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

                    acls.addControl(getAccessControlManager().createAccessControl(curAclElem.getName(), curAclElem));
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
    private final EnumSet<PlatformType> parsePlatformString(String platformStr)
    {
        // Split the platform string and build up a set of platform types

        EnumSet<PlatformType> platformTypes = EnumSet.noneOf(PlatformType.class);
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
                PlatformType platform = PlatformType.valueOf(typ);

                if (platform != PlatformType.Unknown)
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
     * Add a shared device to the server configuration.
     * 
     * @param shr SharedDevice
     * @return boolean
     */
    public final boolean addShare(SharedDevice shr)
    {
        return m_shareList.addShare(shr);
    }

    /**
     * Add a server to the list of active servers
     * 
     * @param srv NetworkServer
     */
    public synchronized final void addServer(NetworkServer srv)
    {
        m_serverList.addServer(srv);
    }

    /**
     * Find an active server using the protocol name
     * 
     * @param proto String
     * @return NetworkServer
     */
    public final NetworkServer findServer(String proto)
    {
        return m_serverList.findServer(proto);
    }

    /**
     * Remove an active server
     * 
     * @param proto String
     * @return NetworkServer
     */
    public final NetworkServer removeServer(String proto)
    {
        return m_serverList.removeServer(proto);
    }

    /**
     * Return the number of active servers
     * 
     * @return int
     */
    public final int numberOfServers()
    {
        return m_serverList.numberOfServers();
    }

    /**
     * Return the server at the specified index
     * 
     * @param idx int
     * @return NetworkServer
     */
    public final NetworkServer getServer(int idx)
    {
        return m_serverList.getServer(idx);
    }

    /**
     * Check if there is an access control manager configured
     * 
     * @return boolean
     */
    public final boolean hasAccessControlManager()
    {
        return m_aclManager != null ? true : false;
    }

    /**
     * Get the access control manager that is used to control per share access
     * 
     * @return AccessControlManager
     */
    public final AccessControlManager getAccessControlManager()
    {
        return m_aclManager;
    }

    /**
     * Return the associated Acegi authentication manager
     * 
     * @return AuthenticationManager
     */
    public final AuthenticationManager getAuthenticationManager()
    {
        return authenticationManager;
    }

    /**
     * Check if the global access control list is configured
     * 
     * @return boolean
     */
    public final boolean hasGlobalAccessControls()
    {
        return m_globalACLs != null ? true : false;
    }

    /**
     * Return the global access control list
     * 
     * @return AccessControlList
     */
    public final AccessControlList getGlobalAccessControls()
    {
        return m_globalACLs;
    }

    /**
     * Get the authenticator object that is used to provide user and share connection
     * authentication for CIFS.
     * 
     * @return CifsAuthenticator
     */
    public final CifsAuthenticator getAuthenticator()
    {
        return m_authenticator;
    }

    /**
     * Get the alfreso authentication service.
     * 
     * @return
     */
    public final AuthenticationService getAuthenticationService()
    {
        return authenticationService;
    }
    
    /**
     * Return the authentication component, for access to internal functions
     * 
     * @return AuthenticationComponent
     */
    public final AuthenticationComponent getAuthenticationComponent()
    {
        return m_authenticationComponent;
    }
    
    /**
     * Return the node service
     * 
     * @return NodeService
     */
    public final NodeService getNodeService()
    {
        return m_nodeService;
    }
    
    /**
     * Return the person service
     * 
     * @return PersonService
     */
    public final PersonService getPersonService()
    {
        return m_personService;
    }
    
    /**
     * Return the transaction service
     * 
     * @return TransactionService
     */
    public final TransactionService getTransactionService()
    {
        return m_transactionService;
    }
    
    /**
     * Return the local address that the SMB server should bind to.
     * 
     * @return java.net.InetAddress
     */
    public final InetAddress getSMBBindAddress()
    {
        return m_smbBindAddress;
    }

    /**
     * Return the local address that the NetBIOS name server should bind to.
     * 
     * @return java.net.InetAddress
     */
    public final InetAddress getNetBIOSBindAddress()
    {
        return m_nbBindAddress;
    }

    /**
     * Return the NetBIOS name server port
     * 
     * @return int
     */
    public final int getNetBIOSNamePort()
    {
    	return m_nbNamePort;
    }
    
    /**
     * Return the NetBIOS session port
     * 
     * @return int
     */
    public final int getNetBIOSSessionPort()
    {
    	return m_nbSessPort;
    }
    
    /**
     * Return the NetBIOS datagram port
     * 
     * @return int
     */
    public final int getNetBIOSDatagramPort()
    {
    	return m_nbDatagramPort;
    }
    
    /**
     * Return the network broadcast mask to be used for broadcast datagrams.
     * 
     * @return java.lang.String
     */
    public final String getBroadcastMask()
    {
        return m_broadcast;
    }

    /**
     * Return the server comment.
     * 
     * @return java.lang.String
     */
    public final String getComment()
    {
        return m_comment != null ? m_comment : "";
    }

    /**
     * Return the disk interface to be used to create shares
     * 
     * @return DiskInterface
     */
    public final DiskInterface getDiskInterface()
    {
        return diskInterface;
    }
    
    /**
     * Return the domain name.
     * 
     * @return java.lang.String
     */
    public final String getDomainName()
    {
        return m_domain;
    }

    /**
     * Return the server name.
     * 
     * @return java.lang.String
     */
    public final String getServerName()
    {
        return m_name;
    }

    /**
     * Return the server type flags.
     * 
     * @return int
     */
    public final int getServerType()
    {
        return m_srvType;
    }

    /**
     * Return the server debug flags.
     * 
     * @return int
     */
    public final int getSessionDebugFlags()
    {
        return m_sessDebug;
    }

    /**
     * Return the shared device list.
     * 
     * @return SharedDeviceList
     */
    public final SharedDeviceList getShares()
    {
        return m_shareList;
    }

    /**
     * Return the share mapper
     * 
     * @return ShareMapper
     */
    public final ShareMapper getShareMapper()
    {
        return m_shareMapper;
    }

    /**
     * Return the Win32 NetBIOS server name, if null the default server name will be used
     * 
     * @return String
     */
    public final String getWin32ServerName()
    {
        return m_win32NBName;
    }

    /**
     * Determine if the server should be announced via Win32 NetBIOS, so that it appears under
     * Network Neighborhood.
     * 
     * @return boolean
     */
    public final boolean hasWin32EnableAnnouncer()
    {
        return m_win32NBAnnounce;
    }

    /**
     * Return the Win32 NetBIOS host announcement interval, in minutes
     * 
     * @return int
     */
    public final int getWin32HostAnnounceInterval()
    {
        return m_win32NBAnnounceInterval;
    }

    /**
     * Return the Win3 NetBIOS LANA number to use, or -1 for the first available
     * 
     * @return int
     */
    public final int getWin32LANA()
    {
        return m_win32NBLANA;
    }

    /**
     * Determine if the Win32 Netbios() API or Winsock Netbios calls should be used
     * 
     * @return boolean
     */
    public final boolean useWinsockNetBIOS()
    {
        return m_win32NBUseWinsock;
    }
    
    /**
     * Return the native SMB port
     * 
     * @return int
     */
    public final int getTcpipSMBPort()
    {
    	return m_tcpSMBPort;
    }
    
    /**
     * Return the timezone name
     * 
     * @return String
     */
    public final String getTimeZone()
    {
        return m_timeZone;
    }

    /**
     * Return the timezone offset from UTC in seconds
     * 
     * @return int
     */
    public final int getTimeZoneOffset()
    {
        return m_tzOffset;
    }

    /**
     * Determine if the primary WINS server address has been set
     * 
     * @return boolean
     */
    public final boolean hasPrimaryWINSServer()
    {
        return m_winsPrimary != null ? true : false;
    }

    /**
     * Return the primary WINS server address
     * 
     * @return InetAddress
     */
    public final InetAddress getPrimaryWINSServer()
    {
        return m_winsPrimary;
    }

    /**
     * Determine if the secondary WINS server address has been set
     * 
     * @return boolean
     */
    public final boolean hasSecondaryWINSServer()
    {
        return m_winsSecondary != null ? true : false;
    }

    /**
     * Return the secondary WINS server address
     * 
     * @return InetAddress
     */
    public final InetAddress getSecondaryWINSServer()
    {
        return m_winsSecondary;
    }

    /**
     * Determine if the SMB server should bind to a particular local address
     * 
     * @return boolean
     */
    public final boolean hasSMBBindAddress()
    {
        return m_smbBindAddress != null ? true : false;
    }

    /**
     * Determine if the NetBIOS name server should bind to a particular local address
     * 
     * @return boolean
     */
    public final boolean hasNetBIOSBindAddress()
    {
        return m_nbBindAddress != null ? true : false;
    }

    /**
     * Determine if NetBIOS name server debugging is enabled
     * 
     * @return boolean
     */
    public final boolean hasNetBIOSDebug()
    {
        return m_nbDebug;
    }

    /**
     * Determine if host announcement debugging is enabled
     * 
     * @return boolean
     */
    public final boolean hasHostAnnounceDebug()
    {
        return m_announceDebug;
    }

    /**
     * Determine if the server should be announced so that it appears under Network Neighborhood.
     * 
     * @return boolean
     */
    public final boolean hasEnableAnnouncer()
    {
        return m_announce;
    }

    /**
     * Return the host announcement interval, in minutes
     * 
     * @return int
     */
    public final int getHostAnnounceInterval()
    {
        return m_announceInterval;
    }

    /**
     * Return the JCE provider class name
     * 
     * @return String
     */
    public final String getJCEProvider()
    {
        return m_jceProviderClass;
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

        if (getPlatformType() == PlatformType.WINDOWS)
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

        if (getPlatformType() == PlatformType.WINDOWS)
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
     * Return the primary filesystem shared device, or null if not available
     * 
     * @return DiskSharedDevice
     */
    public final DiskSharedDevice getPrimaryFilesystem()
    {
        // Check if there are any global shares defined

        SharedDeviceList shares = getShares();
        DiskSharedDevice diskShare = null;
        
        if ( shares != null && shares.numberOfShares() > 0)
        {
            // Find the first available filesystem device
            
            Enumeration<SharedDevice> shareEnum = shares.enumerateShares();

            while ( diskShare == null && shareEnum.hasMoreElements())
            {
                SharedDevice curShare = shareEnum.nextElement();
                if ( curShare.getType() == ShareType.DISK)
                    diskShare = (DiskSharedDevice) curShare;
            }
        }
        
        // Return the first filesystem device, or null
        
        return diskShare;
    }
    
    /**
     * Determine if Macintosh extension SMBs are enabled
     * 
     * @return boolean
     */
    public final boolean hasMacintoshExtensions()
    {
        return m_macExtensions;
    }

    /**
     * Determine if NetBIOS SMB is enabled
     * 
     * @return boolean
     */
    public final boolean hasNetBIOSSMB()
    {
        return m_netBIOSEnable;
    }

    /**
     * Determine if TCP/IP SMB is enabled
     * 
     * @return boolean
     */
    public final boolean hasTcpipSMB()
    {
        return m_tcpSMBEnable;
    }

    /**
     * Determine if Win32 NetBIOS is enabled
     * 
     * @return boolean
     */
    public final boolean hasWin32NetBIOS()
    {
        return m_win32NBEnable;
    }

    /**
     * Check if the SMB server is enabled
     * 
     * @return boolean
     */
    public final boolean isSMBServerEnabled()
    {
        return m_smbEnable;
    }

    /**
     * Set the SMB server enabled state
     * 
     * @param ena boolean
     */
    public final void setSMBServerEnabled(boolean ena)
    {
        m_smbEnable = ena;
    }

    /**
     * Set the FTP server enabled state
     * 
     * @param ena boolean
     */
    public final void setFTPServerEnabled(boolean ena)
    {
        m_ftpEnable = ena;
    }
    
    /**
     * Set the authenticator to be used to authenticate users and share connections for CIFS.
     * 
     * @param auth CifsAuthenticator
     * @param params ConfigElement
     * @param allowGuest boolean
     */
    public final void setAuthenticator(CifsAuthenticator auth, ConfigElement params, boolean allowGuest)
    {

        // Set the server authenticator mode and guest access

        auth.setAllowGuest(allowGuest);

        // Initialize the authenticator using the parameter values

        try
        {
            auth.initialize(this, params);
        }
        catch (InvalidConfigurationException ex)
        {
            throw new AlfrescoRuntimeException("Failed to initialize authenticator", ex);
        }

        // Set the server authenticator and initialization parameters

        m_authenticator = auth;
    }

    /**
     * Set the local address that the SMB server should bind to.
     * 
     * @param addr InetAddress
     */
    public final void setSMBBindAddress(InetAddress addr)
    {
        m_smbBindAddress = addr;
    }

    /**
     * Set the local address that the NetBIOS name server should bind to.
     * 
     * @param addr InetAddress
     */
    public final void setNetBIOSBindAddress(InetAddress addr)
    {
        m_nbBindAddress = addr;
    }

    /**
     * Set the broadcast mask to be used for broadcast datagrams.
     * 
     * @param mask String
     */
    public final void setBroadcastMask(String mask)
    {
        m_broadcast = mask;

        // Copy settings to the NetBIOS session class

        NetBIOSSession.setSubnetMask(mask);
    }

    /**
     * Set the server comment.
     * 
     * @param comment String
     */
    public final void setComment(String comment)
    {
        m_comment = comment;
    }

    /**
     * Set the domain that the server belongs to.
     * 
     * @param domain String
     */
    public final void setDomainName(String domain)
    {
        m_domain = domain;
    }

    /**
     * Enable/disable the host announcer.
     * 
     * @param b boolean
     */
    public final void setHostAnnouncer(boolean b)
    {
        m_announce = b;
    }

    /**
     * Set the host announcement interval, in minutes
     * 
     * @param ival int
     */
    public final void setHostAnnounceInterval(int ival)
    {
        m_announceInterval = ival;
    }

    /**
     * Set the JCE provider
     * 
     * @param providerClass String
     */
    public final void setJCEProvider(String providerClass)
    {

        // Validate the JCE provider class

        try
        {

            // Load the JCE provider class and validate

            Object jceObj = Class.forName(providerClass).newInstance();
            if (jceObj instanceof java.security.Provider)
            {

                // Inform listeners, validate the configuration change

                Provider jceProvider = (Provider) jceObj;

                // Save the JCE provider class name

                m_jceProviderClass = providerClass;

                // Add the JCE provider

                Security.addProvider(jceProvider);
            }
            else
            {
                throw new AlfrescoRuntimeException("JCE provider class is not a valid Provider class");
            }
        }
        catch (ClassNotFoundException ex)
        {
            throw new AlfrescoRuntimeException("JCE provider class " + providerClass + " not found");
        }
        catch (Exception ex)
        {
            throw new AlfrescoRuntimeException("JCE provider class error", ex);
        }
    }

    /**
     * Enable/disable NetBIOS name server debug output
     * 
     * @param ena boolean
     */
    public final void setNetBIOSDebug(boolean ena)
    {
        m_nbDebug = ena;
    }

    /**
     * Enable/disable host announcement debug output
     * 
     * @param ena boolean
     */
    public final void setHostAnnounceDebug(boolean ena)
    {
        m_announceDebug = ena;
    }

    /**
     * Set the server name.
     * 
     * @param name String
     */
    public final void setServerName(String name)
    {
        m_name = name;
    }

    /**
     * Set the debug flags to be used by the server.
     * 
     * @param flags int
     */
    public final void setSessionDebugFlags(int flags)
    {
        m_sessDebug = flags;
    }

    /**
     * Set the global access control list
     * 
     * @param acls AccessControlList
     */
    public final void setGlobalAccessControls(AccessControlList acls)
    {
        m_globalACLs = acls;
    }

    /**
     * Enable/disable the NetBIOS SMB support
     * 
     * @param ena boolean
     */
    public final void setNetBIOSSMB(boolean ena)
    {
        m_netBIOSEnable = ena;
    }

    /**
     * Set the NetBIOS name server port
     * 
     * @param port int
     */
    public final void setNetBIOSNamePort(int port)
    {
    	m_nbNamePort = port;
    }
    
    /**
     * Set the NetBIOS session port
     * 
     * @param port int
     */
    public final void setNetBIOSSessionPort(int port)
    {
    	m_nbSessPort = port;
    }
    
    /**
     * Set the NetBIOS datagram port
     * 
     * @param port int
     */
    public final void setNetBIOSDatagramPort(int port)
    {
    	m_nbDatagramPort = port;
    }
    
    /**
     * Enable/disable the TCP/IP SMB support
     * 
     * @param ena boolean
     */
    public final void setTcpipSMB(boolean ena)
    {
        m_tcpSMBEnable = ena;
    }

    /**
     * Set the TCP/IP SMB port
     * 
     * @param port int
     */
    public final void setTcpipSMBPort( int port)
    {
    	m_tcpSMBPort = port;
    }
    
    /**
     * Enable/disable the Win32 NetBIOS SMB support
     * 
     * @param ena boolean
     */
    public final void setWin32NetBIOS(boolean ena)
    {
        m_win32NBEnable = ena;
    }

    /**
     * Set the Win32 NetBIOS file server name
     * 
     * @param name String
     */
    public final void setWin32NetBIOSName(String name)
    {
        m_win32NBName = name;
    }

    /**
     * Enable/disable the Win32 NetBIOS host announcer.
     * 
     * @param b boolean
     */
    public final void setWin32HostAnnouncer(boolean b)
    {
        m_win32NBAnnounce = b;
    }

    /**
     * Set the Win32 LANA to be used by the Win32 NetBIOS interface
     * 
     * @param ival int
     */
    public final void setWin32LANA(int ival)
    {
        m_win32NBLANA = ival;
    }

    /**
     * Set the Win32 NetBIOS host announcement interval, in minutes
     * 
     * @param ival int
     */
    public final void setWin32HostAnnounceInterval(int ival)
    {
        m_win32NBAnnounceInterval = ival;
    }

    /**
     * Set the Win32 NetBIOS interface to use either Winsock NetBIOS or the Netbios() API calls
     * 
     * @param useWinsock boolean
     */
    public final void setWin32WinsockNetBIOS(boolean useWinsock)
    {
        m_win32NBUseWinsock = useWinsock;
    }
    
    /**
     * Set the server timezone name
     * 
     * @param name String
     * @exception InvalidConfigurationException If the timezone is invalid
     */
    public final void setTimeZone(String name) throws InvalidConfigurationException
    {

        // Validate the timezone

        TimeZone tz = TimeZone.getTimeZone(name);
        if (tz == null)
            throw new InvalidConfigurationException("Invalid timezone, " + name);

        // Set the timezone name and offset from UTC in minutes
        //
        // Invert the result of TimeZone.getRawOffset() as SMB/CIFS requires
        // positive minutes west of UTC

        m_timeZone = name;
        m_tzOffset = -(tz.getRawOffset() / 60000);
    }

    /**
     * Set the timezone offset from UTC in seconds (+/-)
     * 
     * @param offset int
     */
    public final void setTimeZoneOffset(int offset)
    {
        m_tzOffset = offset;
    }

    /**
     * Set the primary WINS server address
     * 
     * @param addr InetAddress
     */
    public final void setPrimaryWINSServer(InetAddress addr)
    {
        m_winsPrimary = addr;
    }

    /**
     * Set the secondary WINS server address
     * 
     * @param addr InetAddress
     */
    public final void setSecondaryWINSServer(InetAddress addr)
    {
        m_winsSecondary = addr;
    }

    /**
     * Check if the FTP server is enabled
     * 
     * @return boolean
     */
    public final boolean isFTPServerEnabled()
    {
        return m_ftpEnable;
    }

    /**
     * Return the FTP server bind address, may be null to indicate bind to all available addresses
     * 
     * @return InetAddress
     */
    public final InetAddress getFTPBindAddress()
    {
        return m_ftpBindAddress;
    }

    /**
     * Return the FTP server port to use for incoming connections
     * 
     * @return int
     */
    public final int getFTPPort()
    {
        return m_ftpPort;
    }

    /**
     * Determine if anonymous FTP access is allowed
     * 
     * @return boolean
     */
    public final boolean allowAnonymousFTP()
    {
        return m_ftpAllowAnonymous;
    }

    /**
     * Return the anonymous FTP account name
     * 
     * @return String
     */
    public final String getAnonymousFTPAccount()
    {
        return m_ftpAnonymousAccount;
    }

    /**
     * Return the FTP debug flags
     * 
     * @return int
     */
    public final int getFTPDebug()
    {
        return m_ftpDebug;
    }

    /**
     * Check if an FTP root path has been configured
     * 
     * @return boolean
     */
    public final boolean hasFTPRootPath()
    {
        return m_ftpRootPath != null ? true : false;
    }

    /**
     * Return the FTP root path
     * 
     * @return String
     */
    public final String getFTPRootPath()
    {
        return m_ftpRootPath;
    }

    /**
     * Set the FTP server bind address, may be null to indicate bind to all available addresses
     * 
     * @param addr InetAddress
     */
    public final void setFTPBindAddress(InetAddress addr)
    {
        m_ftpBindAddress = addr;
    }

    /**
     * Set the FTP server port to use for incoming connections, -1 indicates disable the FTP server
     * 
     * @param port int
     */
    public final void setFTPPort(int port)
    {
        m_ftpPort = port;
    }

    /**
     * Set the FTP root path
     * 
     * @param path String
     */
    public final void setFTPRootPath(String path)
    {
        m_ftpRootPath = path;
    }

    /**
     * Enable/disable anonymous FTP access
     * 
     * @param ena boolean
     */
    public final void setAllowAnonymousFTP(boolean ena)
    {
        m_ftpAllowAnonymous = ena;
    }

    /**
     * Set the anonymous FTP account name
     * 
     * @param acc String
     */
    public final void setAnonymousFTPAccount(String acc)
    {
        m_ftpAnonymousAccount = acc;
    }

    /**
     * Set the FTP debug flags
     * 
     * @param dbg int
     */
    public final void setFTPDebug(int dbg)
    {
        m_ftpDebug = dbg;
    }
    
    /**
     * Close the server configuration, used to close various components that are shared between protocol
     * handlers.
     */
    public final void closeConfiguration()
    {
        // Close the authenticator
        
        if ( getAuthenticator() != null)
        {
            getAuthenticator().closeAuthenticator();
            m_authenticator = null;
        }

        // Close the shared filesystems
        
        if ( getShares() != null && getShares().numberOfShares() > 0)
        {
            // Close the shared filesystems
            
            Enumeration<SharedDevice> shareEnum = getShares().enumerateShares();
            
            while ( shareEnum.hasMoreElements())
            {
                SharedDevice share = shareEnum.nextElement();
                DeviceContext devCtx = share.getContext();
                
                if ( devCtx != null)
                    devCtx.CloseContext();
            }
        }
    }
    
    /**
     * Load a CIFS authenticator using dyanmic loading
     * 
     * @param className String
     * @return CifsAuthenticator
     */
    private final CifsAuthenticator loadAuthenticatorClass(String className)
    {
        CifsAuthenticator srvAuth = null;
        
        try
        {
            // Load the authenticator class
            
            Object authObj = Class.forName(className).newInstance();
            
            // Verify that the class is an authenticator
            
            if ( authObj instanceof CifsAuthenticator)
                srvAuth = (CifsAuthenticator) authObj;
        }
        catch (Exception ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Failed to load authenticator class " + className);
        }
        
        // Return the authenticator class, or null if not available or invalid
        
        return srvAuth;
    }
}