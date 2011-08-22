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

import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import org.springframework.extensions.config.element.GenericConfigElement;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.AbstractServerConfigurationBean;
import org.alfresco.filesys.alfresco.AlfrescoContext;
import org.alfresco.filesys.alfresco.ExtendedDiskInterface;
import org.alfresco.filesys.avm.AVMContext;
import org.alfresco.filesys.avm.AVMDiskDriver;
import org.alfresco.filesys.config.acl.AccessControlListBean;
import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.jlan.ftp.FTPAuthenticator;
import org.alfresco.jlan.ftp.FTPConfigSection;
import org.alfresco.jlan.ftp.FTPPath;
import org.alfresco.jlan.ftp.InvalidPathException;
import org.alfresco.jlan.netbios.NetBIOSSession;
import org.alfresco.jlan.netbios.RFCNetBIOSProtocol;
import org.alfresco.jlan.netbios.win32.Win32NetBIOS;
import org.alfresco.jlan.oncrpc.RpcAuthenticator;
import org.alfresco.jlan.oncrpc.nfs.NFSConfigSection;
import org.alfresco.jlan.server.auth.ICifsAuthenticator;
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
import org.alfresco.jlan.server.core.ShareType;
import org.alfresco.jlan.server.filesys.DiskDeviceContext;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FilesystemsConfigSection;
import org.alfresco.jlan.server.filesys.cache.FileStateLockManager;
import org.alfresco.jlan.server.filesys.cache.StandaloneFileStateCache;
import org.alfresco.jlan.server.filesys.cache.hazelcast.ClusterConfigSection;
import org.alfresco.jlan.server.filesys.cache.hazelcast.HazelCastClusterFileStateCache;
import org.alfresco.jlan.server.thread.ThreadRequestPool;
import org.alfresco.jlan.smb.server.CIFSConfigSection;
import org.alfresco.jlan.util.IPAddress;
import org.alfresco.jlan.util.MemorySize;
import org.alfresco.jlan.util.Platform;
import org.alfresco.jlan.util.StringList;
import org.alfresco.jlan.util.X64;
import org.alfresco.repo.management.subsystems.ActivateableBean;

import com.hazelcast.core.HazelcastInstance;

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
public class ServerConfigurationBean extends AbstractServerConfigurationBean
{
    private CIFSConfigBean cifsConfigBean;
    private FTPConfigBean ftpConfigBean;
    private NFSConfigBean nfsConfigBean;
    private List<DeviceContext> filesystemContexts;
    private boolean avmAllStores;
    private SecurityConfigBean securityConfigBean;
    private CoreServerConfigBean coreServerConfigBean;
    private ClusterConfigBean clusterConfigBean;

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

    public void setCifsConfigBean(CIFSConfigBean cifsConfigBean)
    {
        this.cifsConfigBean = cifsConfigBean;
    }

    public void setFtpConfigBean(FTPConfigBean ftpConfigBean)
    {
        this.ftpConfigBean = ftpConfigBean;
    }

    public void setNfsConfigBean(NFSConfigBean nfsConfigBean)
    {
        this.nfsConfigBean = nfsConfigBean;
    }
    
    public void setFilesystemContexts(List<DeviceContext> filesystemContexts)
    {
        this.filesystemContexts = filesystemContexts;
    }

    public void setAvmAllStores(boolean avmAllStores)
    {
        this.avmAllStores = avmAllStores;
    }
    
    public void setSecurityConfigBean(SecurityConfigBean securityConfigBean)
    {
        this.securityConfigBean = securityConfigBean;
    }

    public void setCoreServerConfigBean(CoreServerConfigBean coreServerConfigBean)
    {
        this.coreServerConfigBean = coreServerConfigBean;
    }
    
    public void setClusterConfigBean(ClusterConfigBean clusterConfigBean)
    {
        this.clusterConfigBean = clusterConfigBean;
    }

    /**
     * Process the CIFS server configuration
     */
    protected void processCIFSServerConfig()
    {
        // If the configuration section is not valid then CIFS is disabled

        if (cifsConfigBean == null)
        {
            removeConfigSection(CIFSConfigSection.SectionName);
            return;
        }

        // Check if the server has been disabled
        if (!cifsConfigBean.getServerEnabled())
        {
            removeConfigSection(CIFSConfigSection.SectionName);
            return;
        }

        // Before we go any further, let's make sure there's a compatible authenticator in the authentication chain.
        ICifsAuthenticator authenticator = cifsConfigBean.getAuthenticator();
        if (authenticator == null || authenticator instanceof ActivateableBean && !((ActivateableBean)authenticator).isActive())
        {
            logger.warn("No enabled CIFS authenticator found in authentication chain. CIFS Server disabled");
            removeConfigSection(CIFSConfigSection.SectionName);
            return;
        }
            
        // Create the CIFS server configuration section

        CIFSConfigSection cifsConfig = new CIFSConfigSection(this);

        try
        {
            // Check if native code calls should be disabled on Windows
            if (cifsConfigBean.getDisableNativeCode())
            {
                // Disable native code calls so that the JNI DLL is not required

                cifsConfig.setNativeCodeDisabled(true);
                m_disableNativeCode = true;

                // Warning

                logger.warn("CIFS server native calls disabled, JNI code will not be used");
            }

            // Get the network broadcast address
            //
            // Note: We need to set this first as the call to getLocalDomainName() may use a NetBIOS
            // name lookup, so the broadcast mask must be set before then.

            String broadcastAddess = cifsConfigBean.getBroadcastAddress();
            if (broadcastAddess != null && broadcastAddess.length() > 0)
            {

                // Check if the broadcast mask is a valid numeric IP address

                if (IPAddress.isNumericAddress(broadcastAddess) == false)
                    throw new AlfrescoRuntimeException("Invalid broadcast mask, must be n.n.n.n format");

                // Set the network broadcast mask

                cifsConfig.setBroadcastMask(broadcastAddess);
            }

            // Get the host configuration

            String hostName = cifsConfigBean.getServerName();
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

            // Check if the host name is longer than 15 characters. NetBIOS only allows a maximum of 16 characters in
            // the
            // server name with the last character reserved for the service type.

            if (hostName.length() > 15)
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

            String domain = cifsConfigBean.getDomainName();
            if (domain != null && domain.length() > 0)
            {
                // Set the domain/workgroup name

                cifsConfig.setDomainName(domain.toUpperCase());
            }
            else
            {
                // Get the local domain/workgroup name

                String localDomain = getLocalDomainName();

                if (localDomain == null && (getPlatformType() != Platform.Type.WINDOWS || isNativeCodeDisabled()))
                {
                    // Use a default domain/workgroup name

                    localDomain = "WORKGROUP";

                    // Output a warning

                    logger.error("Failed to get local domain/workgroup name, using default of " + localDomain);
                    logger.error("(This may be due to firewall settings or incorrect <broadcast> setting)");
                }

                // Set the local domain/workgroup that the CIFS server belongs to

                cifsConfig.setDomainName(localDomain);
            }

            // Check for a server comment
            String comment = cifsConfigBean.getServerComment();
            if (comment != null && comment.length() > 0)
            {
                cifsConfig.setComment(comment);
            }

            // Check for a bind address

            // Check if the network adapter name has been specified
            String bindToAdapter = cifsConfigBean.getBindToAdapter();
            String bindTo;

            if (bindToAdapter != null && bindToAdapter.length() > 0)
            {

                // Get the IP address for the adapter

                InetAddress bindAddr = parseAdapterName(bindToAdapter);

                // Set the bind address for the server

                cifsConfig.setSMBBindAddress(bindAddr);
            }
            else if ((bindTo = cifsConfigBean.getBindToAddress()) != null && bindTo.length() > 0
                    && !bindTo.equals(BIND_TO_IGNORE))
            {

                // Validate the bind address
                try
                {
                    // Check the bind address

                    InetAddress bindAddr = InetAddress.getByName(bindTo);

                    // Set the bind address for the server

                    cifsConfig.setSMBBindAddress(bindAddr);
                }
                catch (UnknownHostException ex)
                {
                    throw new AlfrescoRuntimeException("Invalid CIFS server bind address");
                }
            }

            // Get the authenticator

            if (authenticator != null)
            {
                cifsConfig.setAuthenticator(authenticator);
            }
            else
                throw new AlfrescoRuntimeException("CIFS authenticator not specified");

            // Check if the host announcer has been disabled
            
            if (!cifsConfigBean.getHostAccouncerEnabled())
            {
                // Switch off the host announcer
                
                cifsConfig.setHostAnnouncer( false);
                
                // Log that host announcements are not enabled
                
                logger.info("Host announcements not enabled");
            }
            else
            {
                // Check for an announcement interval
      
                Integer interval = cifsConfigBean.getHostAccounceInterval();
                if (interval != null)
                {
                    cifsConfig.setHostAnnounceInterval(interval);
                }
      
                // Check if the domain name has been set, this is required if the
                // host announcer is enabled
      
                if (cifsConfig.getDomainName() == null)
                    throw new AlfrescoRuntimeException("Domain name must be specified if host announcement is enabled");
      
                // Enable host announcement
      
                cifsConfig.setHostAnnouncer(true);
            }

            // Check if NetBIOS SMB is enabled
            NetBIOSSMBConfigBean netBIOSSMBConfigBean = cifsConfigBean.getNetBIOSSMB();
            if (netBIOSSMBConfigBean != null)
            {
                // Check if NetBIOS over TCP/IP is enabled for the current platform

                String platformsStr = netBIOSSMBConfigBean.getPlatforms();
                boolean platformOK = false;

                if (platformsStr != null && platformsStr.length() > 0)
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

                if (cifsConfig.hasNetBIOSSMB())
                {
                    // Check if the broadcast mask has been specified

                    if (cifsConfig.getBroadcastMask() == null)
                        throw new AlfrescoRuntimeException("Network broadcast mask not specified");

                    // Check for a bind address

                    String bindto = netBIOSSMBConfigBean.getBindTo();
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

                        // Check the address list for one or more valid local addresses filtering out the loopback
                        // address

                        int addrCnt = 0;

                        if (addrs != null)
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

                        if (addrCnt == 0)
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

                            if (niEnum != null)
                            {
                                while (niEnum.hasMoreElements())
                                {
                                    // Get the current network interface

                                    NetworkInterface ni = niEnum.nextElement();

                                    // Enumerate the addresses for the network adapter

                                    Enumeration<InetAddress> niAddrs = ni.getInetAddresses();
                                    if (niAddrs != null)
                                    {
                                        // Check for any valid addresses

                                        while (niAddrs.hasMoreElements())
                                        {
                                            InetAddress curAddr = niAddrs.nextElement();

                                            if (curAddr.getHostAddress().equals("127.0.0.1") == false
                                                    && curAddr.getHostAddress().equals("0.0.0.0") == false)
                                                addrCnt++;
                                        }
                                    }
                                }

                                // DEBUG

                                if (addrCnt > 0 && logger.isDebugEnabled())
                                    logger.debug("Found valid IP address from interface list");
                            }

                            // Check if we found any valid network addresses

                            if (addrCnt == 0)
                            {
                                // Log the available IP addresses

                                if (logger.isDebugEnabled())
                                {
                                    logger.debug("Local address list dump :-");
                                    if (addrs != null)
                                    {
                                        for (int i = 0; i < addrs.length; i++)
                                            logger.debug("  Address: " + addrs[i]);
                                    }
                                    else
                                        logger.debug("  No addresses");
                                }

                                // Throw an exception to stop the CIFS/NetBIOS name server from starting

                                throw new AlfrescoRuntimeException(
                                        "Failed to get IP address(es) for the local server, check hosts file and/or DNS setup");
                            }
                        }
                    }

                    // Check if the session port has been specified

                    Integer portNum = netBIOSSMBConfigBean.getSessionPort();
                    if (portNum != null)
                    {
                        cifsConfig.setSessionPort(portNum);
                        if (cifsConfig.getSessionPort() <= 0 || cifsConfig.getSessionPort() >= 65535)
                            throw new AlfrescoRuntimeException("NetBIOS session port out of valid range");
                    }

                    // Check if the name port has been specified

                    portNum = netBIOSSMBConfigBean.getNamePort();
                    if (portNum != null)
                    {
                        cifsConfig.setNameServerPort(portNum);
                        if (cifsConfig.getNameServerPort() <= 0 || cifsConfig.getNameServerPort() >= 65535)
                            throw new AlfrescoRuntimeException("NetBIOS name port out of valid range");
                    }

                    // Check if the datagram port has been specified

                    portNum = netBIOSSMBConfigBean.getDatagramPort();
                    if (portNum != null)
                    {
                        cifsConfig.setDatagramPort(portNum);
                        if (cifsConfig.getDatagramPort() <= 0 || cifsConfig.getDatagramPort() >= 65535)
                            throw new AlfrescoRuntimeException("NetBIOS datagram port out of valid range");
                    }

                    // Check for a bind address

                    String attr = netBIOSSMBConfigBean.getBindTo();
                    if (attr != null && attr.length() > 0 && !attr.equals(BIND_TO_IGNORE))
                    {

                        // Validate the bind address

                        try
                        {

                            // Check the bind address

                            InetAddress bindAddr = InetAddress.getByName(attr);

                            // Set the bind address for the NetBIOS name server

                            cifsConfig.setNetBIOSBindAddress(bindAddr);
                        }
                        catch (UnknownHostException ex)
                        {
                            throw new InvalidConfigurationException(ex.toString());
                        }
                    }

                    // Check for a bind address using the adapter name

                    else if ((attr = netBIOSSMBConfigBean.getAdapter()) != null && attr.length() > 0)
                    {

                        // Get the bind address via the network adapter name

                        InetAddress bindAddr = parseAdapterName(attr);
                        cifsConfig.setNetBIOSBindAddress(bindAddr);
                    }
                    else if (cifsConfig.hasSMBBindAddress())
                    {

                        // Use the SMB bind address for the NetBIOS name server

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

            TcpipSMBConfigBean tcpipSMBConfigBean = cifsConfigBean.getTcpipSMB();
            if (tcpipSMBConfigBean != null)
            {

                // Check if native SMB is enabled for the current platform

                String platformsStr = tcpipSMBConfigBean.getPlatforms();
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

                // Check if the port has been specified

                Integer portNum = tcpipSMBConfigBean.getPort();
                if (portNum != null)
                {
                    cifsConfig.setTcpipSMBPort(portNum);
                    if (cifsConfig.getTcpipSMBPort() <= 0 || cifsConfig.getTcpipSMBPort() >= 65535)
                        throw new AlfrescoRuntimeException("TCP/IP SMB port out of valid range");
                }
                
                // Check if IPv6 support should be enabled
                
                if ( tcpipSMBConfigBean.getIpv6Enabled())
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

            Win32NetBIOSConfigBean win32NetBIOSConfigBean = cifsConfigBean.getWin32NetBIOS();
            if (win32NetBIOSConfigBean != null)
            {

                // Check if the Win32 NetBIOS server name has been specified

                String win32Name = win32NetBIOSConfigBean.getName();
                if (win32Name != null && win32Name.length() > 0)
                {

                    // Validate the name

                    if (win32Name.length() > 16)
                        throw new AlfrescoRuntimeException("Invalid Win32 NetBIOS name, " + win32Name);

                    // Set the Win32 NetBIOS file server name

                    cifsConfig.setWin32NetBIOSName(win32Name);
                }

                // Check if the Win32 NetBIOS LANA has been specified

                String lanaStr = win32NetBIOSConfigBean.getLana();
                if (lanaStr != null && lanaStr.length() > 0)
                {
                    // Check if the LANA has been specified as an IP address or adapter name

                    int lana = -1;

                    if (IPAddress.isNumericAddress(lanaStr))
                    {

                        // Convert the IP address to a LANA id

                        lana = Win32NetBIOS.getLANAForIPAddress(lanaStr);
                        if (lana == -1)
                            throw new AlfrescoRuntimeException("Failed to convert IP address " + lanaStr + " to a LANA");
                    }
                    else if (lanaStr.length() > 1 && Character.isLetter(lanaStr.charAt(0)))
                    {

                        // Convert the network adapter to a LANA id

                        lana = Win32NetBIOS.getLANAForAdapterName(lanaStr);
                        if (lana == -1)
                            throw new AlfrescoRuntimeException("Failed to convert network adapter " + lanaStr
                                    + " to a LANA");
                    }
                    else
                    {

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

                String nativeAPI = win32NetBIOSConfigBean.getApi();
                if (nativeAPI != null && nativeAPI.length() > 0)
                {
                    // Validate the API type

                    boolean useWinsock = true;

                    if (nativeAPI.equalsIgnoreCase("netbios"))
                        useWinsock = false;
                    else if (nativeAPI.equalsIgnoreCase("winsock") == false)
                        throw new AlfrescoRuntimeException("Invalid NetBIOS API type, spefify 'winsock' or 'netbios'");

                    // Set the NetBIOS API to use

                    cifsConfig.setWin32WinsockNetBIOS(useWinsock);
                }

                // Force the older NetBIOS API code to be used on 64Bit Windows

                if (cifsConfig.useWinsockNetBIOS() == true && X64.isWindows64())
                {
                    // Debug

                    if (logger.isDebugEnabled())
                        logger.debug("Using older Netbios() API code");

                    // Use the older NetBIOS API code

                    cifsConfig.setWin32WinsockNetBIOS(false);
                }

                // Check if the current operating system is supported by the Win32
                // NetBIOS handler

                String osName = System.getProperty("os.name");
                if (osName.startsWith("Windows")
                        && (osName.endsWith("95") == false && osName.endsWith("98") == false && osName.endsWith("ME") == false)
                        && isNativeCodeDisabled() == false)
                {

                    // Call the Win32NetBIOS native code to make sure it is initialized

                    if (Win32NetBIOS.LanaEnumerate() != null)
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

            // Check if the Win32 host announcer has been disabled
            
            if ( !cifsConfigBean.getWin32HostAnnouncerEnabled())
            {
                // Switch off the Win32 host announcer
                
                cifsConfig.setWin32HostAnnouncer( false);
                
                // Log that host announcements are not enabled
                
                logger.info("Win32 host announcements not enabled");
            }
            else
            {
                // Check for an announcement interval
                Integer interval = cifsConfigBean.getWin32HostAnnounceInterval();
                if (interval != null)
                {
                    cifsConfig.setWin32HostAnnounceInterval(interval);
                }

                // Check if the domain name has been set, this is required if the
                // host announcer is enabled

                if (cifsConfig.getDomainName() == null)
                    throw new AlfrescoRuntimeException("Domain name must be specified if host announcement is enabled");

                // Enable Win32 NetBIOS host announcement

                cifsConfig.setWin32HostAnnouncer(true);
            }

            // Check if NetBIOS and/or TCP/IP SMB have been enabled

            if (cifsConfig.hasNetBIOSSMB() == false && cifsConfig.hasTcpipSMB() == false
                    && cifsConfig.hasWin32NetBIOS() == false)
                throw new AlfrescoRuntimeException("NetBIOS SMB, TCP/IP SMB or Win32 NetBIOS must be enabled");

            // Check if WINS servers are configured

            WINSConfigBean winsConfigBean = cifsConfigBean.getWINSConfig();

            if (winsConfigBean != null && !winsConfigBean.isAutoDetectEnabled())
            {

                // Get the primary WINS server

                String priWins = winsConfigBean.getPrimary();

                if (priWins == null || priWins.length() == 0)
                    throw new AlfrescoRuntimeException("No primary WINS server configured");

                // Validate the WINS server address

                InetAddress primaryWINS = null;

                try
                {
                    primaryWINS = InetAddress.getByName(priWins);
                }
                catch (UnknownHostException ex)
                {
                    throw new AlfrescoRuntimeException("Invalid primary WINS server address, " + priWins);
                }

                // Check if a secondary WINS server has been specified

                String secWins = winsConfigBean.getSecondary();
                InetAddress secondaryWINS = null;

                if (secWins != null && secWins.length() > 0)
                {

                    // Validate the secondary WINS server address

                    try
                    {
                        secondaryWINS = InetAddress.getByName(secWins);
                    }
                    catch (UnknownHostException ex)
                    {
                        throw new AlfrescoRuntimeException("Invalid secondary WINS server address, " + secWins);
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

            else if (cifsConfig.hasNetBIOSSMB() && getPlatformType() == Platform.Type.WINDOWS && !isNativeCodeDisabled())
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
                        InetSocketAddress sockAddr = new InetSocketAddress(winsAddr, RFCNetBIOSProtocol.NAME_PORT);

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
                        if (logger.isDebugEnabled())
                            logger.debug("Failed to connect to auto WINS server " + addr);
                    }
                }
            }

            // Check for session debug flags

            String flags = cifsConfigBean.getSessionDebugFlags();

            int sessDbg = 0;

            if (flags != null && flags.length() > 0)
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

            // Check if NIO based socket code should be disabled

            if (cifsConfigBean.getDisableNIO())
            {

                // Disable NIO based code

                cifsConfig.setDisableNIOCode(true);

                // DEBUG

                if (logger.isDebugEnabled())
                    logger.debug("NIO based code disabled for CIFS server");
            }
            
            
            // Check if a session timeout is configured
            
            Integer tmo = cifsConfigBean.getSessionTimeout();
            if (tmo != null)
            {

                // Validate the session timeout value

                if (tmo < 0 || tmo > MaxSessionTimeout)
                    throw new AlfrescoRuntimeException("Session timeout out of range (0 - " + MaxSessionTimeout + ")");

                // Convert the session timeout to milliseconds

                cifsConfig.setSocketTimeout(tmo * 1000);
            }            
        }
        catch (InvalidConfigurationException ex)
        {
            throw new AlfrescoRuntimeException(ex.getMessage());
        }
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
                    throw new AlfrescoRuntimeException("Invalid FTP bindto address, " + bindText);
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
                        throw new AlfrescoRuntimeException("Anonymous FTP account invalid");
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
                    throw new AlfrescoRuntimeException("Illegal character set name, " + charSet);
                }
                catch (UnsupportedCharsetException ex)
                {
                    throw new AlfrescoRuntimeException("Unsupported character set name, " + charSet);
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
    		}
    		
    		// Check if the store passphrase has been specified
    		
    		if ( ftpConfigBean.getPassphrase() != null && ftpConfigBean.getPassphrase().length() > 0) {

    			// Set the store passphrase
    			
    			ftpConfig.setPassphrase( ftpConfigBean.getPassphrase());
    		}
    		
    		// Check if only secure sessions should be allowed to logon
    		
    		if ( ftpConfigBean.hasRequireSecureSession()) {

    			// Only allow secure sessions to logon to the FTP server

    			ftpConfig.setRequireSecureSession( true);
    		}
    		
    		// Check that all the required FTPS parameters have been set
    		
    		if ( ftpConfig.getKeyStorePath() != null || ftpConfig.getTrustStorePath() != null || ftpConfig.getPassphrase() != null) {
    			
    			// Make sure all parameters are set
    			
    			if ( ftpConfig.getKeyStorePath() == null || ftpConfig.getTrustStorePath() == null || ftpConfig.getPassphrase() == null)
    				throw new InvalidConfigurationException("FTPS configuration requires keyStore, trustStore and storePassphrase to be set");
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
     * Process the NFS server configuration
     */
    protected void processNFSServerConfig()
    {
        // If the configuration section is not valid then NFS is disabled

        if (nfsConfigBean == null)
        {
            removeConfigSection(NFSConfigSection.SectionName);
            return;
        }

        // Check if the server has been disabled

        if (!nfsConfigBean.getServerEnabled())
        {
            removeConfigSection(NFSConfigSection.SectionName);
            return;
        }

        // Create the NFS configuration section

        NFSConfigSection nfsConfig = new NFSConfigSection(this);

        try
        {
            // Check if the port mapper is enabled

            if (nfsConfigBean.getPortMapperEnabled())
                nfsConfig.setNFSPortMapper(true);

            // Check for the thread pool size

            Integer poolSize = nfsConfigBean.getThreadPool();

            if (poolSize != null)
            {

                // Range check the pool size value

                if (poolSize < 4)
                    throw new AlfrescoRuntimeException("NFS thread pool size is below minimum of 4");

                // Set the thread pool size

                nfsConfig.setNFSThreadPoolSize(poolSize);
            }

            // NFS packet pool size

            Integer pktPoolSize = nfsConfigBean.getPacketPool();

            if (pktPoolSize != null)
            {
                // Range check the pool size value

                if (pktPoolSize < 10)
                    throw new AlfrescoRuntimeException("NFS packet pool size is below minimum of 10");

                if (pktPoolSize < nfsConfig.getNFSThreadPoolSize() + 1)
                    throw new AlfrescoRuntimeException("NFS packet pool must be at least thread pool size plus one");

                // Set the packet pool size

                nfsConfig.setNFSPacketPoolSize(pktPoolSize);
            }

            // Check for a port mapper server port

            Integer portMapperPort = nfsConfigBean.getPortMapperPort();
            if (portMapperPort != null)
            {
                nfsConfig.setPortMapperPort(portMapperPort);
                if ( nfsConfig.getPortMapperPort() == -1) {
                	logger.info("NFS portmapper registration disabled");
                }
                else {
                	if (nfsConfig.getPortMapperPort() <= 0 || nfsConfig.getPortMapperPort() >= 65535)
                		throw new AlfrescoRuntimeException("Port mapper server port out of valid range");
                }
            }

            // Check for a mount server port

            Integer mountServerPort = nfsConfigBean.getMountServerPort();
            if (mountServerPort != null)
            {
                nfsConfig.setMountServerPort(mountServerPort);
                if (nfsConfig.getMountServerPort() < 0 || nfsConfig.getMountServerPort() >= 65535)
                    throw new AlfrescoRuntimeException("Mount server port out of valid range");
            }

            // Check for an NFS server port

            Integer nfsServerPort = nfsConfigBean.getNfsServerPort();
            if (nfsServerPort != null)
            {
                nfsConfig.setNFSServerPort(nfsServerPort);
                if (nfsConfig.getNFSServerPort() < 0 || nfsConfig.getNFSServerPort() >= 65535)
                    throw new AlfrescoRuntimeException("NFS server port out of valid range");
            }

            // Check for an RPC registration port
            
            Integer rpcRegisterPort = nfsConfigBean.getRpcRegisterPort();
            if ( rpcRegisterPort != null)
            {
                nfsConfig.setRPCRegistrationPort( rpcRegisterPort);
                if ( nfsConfig.getRPCRegistrationPort() < 0 || nfsConfig.getRPCRegistrationPort() >= 65535)
                    throw new AlfrescoRuntimeException("RPC registrtion port out of valid range");
            }
            
            // Check for NFS debug flags

            String flags = nfsConfigBean.getDebugFlags();
            int nfsDbg = 0;

            if (flags != null && flags.length() > 0)
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

                    while (idx < m_nfsDebugStr.length && m_nfsDebugStr[idx].equalsIgnoreCase(dbg) == false)
                        idx++;

                    if (idx >= m_nfsDebugStr.length)
                        throw new AlfrescoRuntimeException("Invalid NFS debug flag, " + dbg);

                    // Set the debug flag

                    nfsDbg += 1 << idx;
                }

                // Set the NFS debug flags

                nfsConfig.setNFSDebug(nfsDbg);
            }

            // Check if mount server debug output is enabled

            if (nfsConfigBean.getMountServerDebug())
                nfsConfig.setMountServerDebug(true);

            // Check if portmapper debug output is enabled

            if (nfsConfigBean.getPortMapperDebug())
                nfsConfig.setPortMapperDebug(true);

            // Create the RPC authenticator
            RpcAuthenticator rpcAuthenticator = nfsConfigBean.getRpcAuthenticator();
            if (rpcAuthenticator != null)
            {
                nfsConfig.setRpcAuthenticator(rpcAuthenticator);
            }
            else
                throw new AlfrescoRuntimeException("RPC authenticator configuration missing, require user mappings");
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

                    if (filesystem instanceof AVMContext)
                    {
                        // Create a new filesystem driver instance and register a context for
                        // the new filesystem

                        ExtendedDiskInterface filesysDriver = getAvmDiskInterface();
                        DiskDeviceContext diskCtx = (DiskDeviceContext) filesystem;
                        
                        if(clusterConfigBean != null && clusterConfigBean.getClusterEnabled())
                        {
                            if(logger.isDebugEnabled())
                            {
                                logger.debug("start hazelcast cache : " + clusterConfigBean.getClusterName() + ", shareName: "+ diskCtx.getShareName());
                            }
                            GenericConfigElement hazelConfig = createClusterConfig(diskCtx.getShareName()); 
                            HazelCastClusterFileStateCache hazel = new HazelCastClusterFileStateCache();
                            hazel.initializeCache(hazelConfig, this);   
                            diskCtx.setStateCache(hazel);
                        }
                        else
                        {
                            // Check if the filesystem uses the file state cache, if so then add to the file state reaper
                            StandaloneFileStateCache standaloneCache = new StandaloneFileStateCache();
                            standaloneCache.initializeCache( new GenericConfigElement( ""), this);
                            diskCtx.setStateCache(standaloneCache);                  
                        }
                        if ( diskCtx.hasStateCache()) {
                            
                            // Register the state cache with the reaper thread
                            
                            fsysConfig.addFileStateCache( filesystem.getDeviceName(), diskCtx.getStateCache());
                        }
                        
                        filesysDriver.registerContext(filesystem);

                        // Create the shared filesystem

                        filesys = new DiskSharedDevice(filesystem.getDeviceName(), filesysDriver, (AVMContext)filesystem);
                        filesys.setConfiguration( this);
                        // Start the filesystem

                        ((AVMContext)filesystem).startFilesystem(filesys);
                    }
                    else
                    {
                        // Create a new filesystem driver instance and register a context for
                        // the new filesystem

                        ExtendedDiskInterface filesysDriver = getRepoDiskInterface();
                        ContentContext filesysContext = (ContentContext) filesystem;
                        
                        if(clusterConfigBean != null && clusterConfigBean.getClusterEnabled())
                        {
                            if(logger.isDebugEnabled())
                            {
                                logger.debug("start hazelcast cache : " + clusterConfigBean.getClusterName() + ", shareName: "+ filesysContext.getShareName());
                            }
                            GenericConfigElement hazelConfig = createClusterConfig(filesysContext.getShareName()); 
                            HazelCastClusterFileStateCache hazel = new HazelCastClusterFileStateCache();
                            hazel.initializeCache(hazelConfig, this);   
                            filesysContext.setStateCache(hazel);
                        }
                        else
                        {
                            // Create state cache here and inject
                            StandaloneFileStateCache standaloneCache = new StandaloneFileStateCache();
                            standaloneCache.initializeCache( new GenericConfigElement( ""), this);
                            filesysContext.setStateCache(standaloneCache);
                        }
                        
                        if ( filesysContext.hasStateCache()) {
                            
                            // Register the state cache with the reaper thread
                            
                            fsysConfig.addFileStateCache( filesystem.getDeviceName(), filesysContext.getStateCache());
                            
                            // Create the lock manager for the context.
                            FileStateLockManager lockMgr = new FileStateLockManager(filesysContext.getStateCache());
                            filesysContext.setLockManager(lockMgr); 
                            filesysContext.setOpLockManager(lockMgr);
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
                    }

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

        // Check if shares should be added for all AVM stores
        if (this.avmAllStores && getAvmDiskInterface() != null)
        {
            // Get the list of store names

            AVMDiskDriver avmDriver = (AVMDiskDriver) getAvmDiskInterface();
            StringList storeNames = avmDriver.getAVMStoreNames();

            // Add shares for each of the store names, if the share name does not already exist

            if (storeNames != null && storeNames.numberOfStrings() > 0)
            {
                // Add a share for each store

                for (int i = 0; i < storeNames.numberOfStrings(); i++)
                {
                    String storeName = storeNames.getStringAt(i);

                    // Check if a share of the same name already exists

                    if (fsysConfig.getShares().findShare(storeName, ShareType.DISK, true) == null)
                    {
                        // Create the new share for the store

                        AVMContext avmContext = new AVMContext(storeName, storeName + ":/", AVMContext.VERSION_HEAD);
//                        avmContext.enableStateCache(this, true);

                        // Create the shared filesystem

                        DiskSharedDevice filesys = new DiskSharedDevice(storeName, avmDriver, avmContext); 
                        filesys.setConfiguration( this);
                        
                        fsysConfig.addShare( filesys);

                        // DEBUG

                        if (logger.isDebugEnabled())
                            logger.debug("Added AVM share " + storeName);
                    }
                }
            }
        }
        

        // home folder share mapper could be declared in security config
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
            else
            {
                // Check if the tenant service is enabled
                if (m_tenantService != null && m_tenantService.isEnabled())
                {
                    // Initialize the multi-tenancy share mapper

                    secConfig.setShareMapper("org.alfresco.filesys.alfresco.MultiTenantShareMapper",
                            new GenericConfigElement("shareMapper"));
                    
                 }
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

    	// Check if the CIFS server is not enabled, do not create the thread/memory pools
    	
    	if ( cifsConfigBean == null || cifsConfigBean.getServerEnabled() == false)
    		return;
    	
        // Check if the server core element has been specified

        if (coreServerConfigBean == null)
        {

            // Configure a default memory pool

            coreConfig.setMemoryPool(DefaultMemoryPoolBufSizes, DefaultMemoryPoolInitAlloc, DefaultMemoryPoolMaxAlloc);

            // Configure a default thread pool size

            coreConfig.setThreadPool(DefaultThreadPoolInit, DefaultThreadPoolMax);
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
     * @param diskCtx
     */
    public void initialiseRuntimeContext(AlfrescoContext diskCtx)
    {
        if (diskCtx.getStateCache() == null) {
          
          // Set the state cache, use a hard coded standalone cache for now
          FilesystemsConfigSection filesysConfig = (FilesystemsConfigSection) this.getConfigSection( FilesystemsConfigSection.SectionName);
  
          if ( filesysConfig != null) 
          {
              
              try 
              {
                  if(clusterConfigBean != null && clusterConfigBean.getClusterEnabled())
                  {
                      if(logger.isDebugEnabled())
                      {
                          logger.debug("start hazelcast cache : " + clusterConfigBean.getClusterName() + ", shareName: "+ diskCtx.getShareName());
                      }
                      GenericConfigElement hazelConfig = createClusterConfig(diskCtx.getShareName()); 
                      HazelCastClusterFileStateCache hazel = new HazelCastClusterFileStateCache();
                      hazel.initializeCache(hazelConfig, this);   
                      diskCtx.setStateCache(hazel);
                  }
                  else
                  {          
                      // Create a standalone state cache
                      StandaloneFileStateCache standaloneCache = new StandaloneFileStateCache();
                      standaloneCache.initializeCache( new GenericConfigElement( ""), this); 
                      filesysConfig.addFileStateCache( diskCtx.getDeviceName(), standaloneCache);
                      diskCtx.setStateCache( standaloneCache);
                  }
                  
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
    protected void processClusterConfig() throws InvalidConfigurationException
    {

// Done by org.alfresco.jlan.server.config.ServerConfiguration.closeConfiguration        
//        /**
//         * Close the old hazelcast configuration
//         */
//        ClusterConfigSection secConfig = (ClusterConfigSection) getConfigSection(ClusterConfigSection.SectionName);
//        {
//            if(secConfig != null)
//            {
//                secConfig.closeConfig();
//            }
//        }
        
        if (clusterConfigBean  == null || !clusterConfigBean.getClusterEnabled())
        {
            removeConfigSection(ClusterConfigSection.SectionName);
            logger.info("Filesystem cluster cache not enabled");
            return;
        }
        
        String clusterName = clusterConfigBean.getClusterName();
        if (clusterName == null || clusterName.length() == 0)
        {
            throw new InvalidConfigurationException("Cluster name not specified or invalid");
        }
        
        String clusterFile = clusterConfigBean.getConfigFile();
        if (clusterFile == null || clusterFile.length() == 0)
        {
            throw new InvalidConfigurationException("Cluster config file not specified or invalid");
        }
        
        // New Hazelcast instance created here within the ClusterConfigSection       
        ClusterConfigSection jlanClusterConfig = new ClusterConfigSection(this);
        
        try
        {
            //TODO replace config XML file with Hazelcast config bean backed by spring.
            jlanClusterConfig.setConfigFile(clusterFile);
            HazelcastInstance hazelcastInstance = jlanClusterConfig.getHazelcastInstance();
        } 
        catch (FileNotFoundException e)
        {
            throw new InvalidConfigurationException("Unable to start filsystem cluster", e);
        }        
    }
    
    
    private  GenericConfigElement createClusterConfig(String topicName) throws InvalidConfigurationException 
    {
        GenericConfigElement config = new GenericConfigElement("hazelcastStateCache");
        GenericConfigElement clusterNameCfg = new GenericConfigElement("clusterName");
        clusterNameCfg.setValue(clusterConfigBean.getClusterName());
        config.addChild(clusterNameCfg);
    
        GenericConfigElement topicNameCfg = new GenericConfigElement("clusterTopic");
        if(topicName == null || topicName.isEmpty())
        {
            topicName="default";
        }
        topicNameCfg.setValue(topicName);
        config.addChild(topicNameCfg);
    
        if(clusterConfigBean.getDebugFlags() != null)
        {
            GenericConfigElement debugCfg = new GenericConfigElement("cacheDebug");
            debugCfg.addAttribute("flags", clusterConfigBean.getDebugFlags());
            config.addChild(debugCfg);
        }
    
        if(clusterConfigBean.getNearCacheTimeout() > 0)
        {
            GenericConfigElement nearCacheCfg = new GenericConfigElement("nearCache");
            nearCacheCfg.addAttribute("disable", Boolean.FALSE.toString());
            nearCacheCfg.addAttribute("timeout", Integer.toString(clusterConfigBean.getNearCacheTimeout()));
            config.addChild(nearCacheCfg);
        }
        return config;
    }

}
