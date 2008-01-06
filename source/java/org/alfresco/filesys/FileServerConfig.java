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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.filesys;

import java.net.InetAddress;

import org.alfresco.filesys.util.CifsMounter;
import org.alfresco.jlan.ftp.FTPConfigSection;
import org.alfresco.jlan.oncrpc.nfs.NFSConfigSection;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.smb.TcpipSMB;
import org.alfresco.jlan.smb.server.CIFSConfigSection;
import org.alfresco.jlan.util.Platform;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * File Server Configuration MBean Class
 * 
 * <p>Implements the file server configuration interface using the fileServerConfigurationBase bean
 * from network-protocol-context.xml.
 * 
 * @author gkspencer
 */
public class FileServerConfig implements FileServerConfigMBean {

	// Debug logging
	
    private static final Log logger = LogFactory.getLog( FileServerConfig.class);
    
	// File server configuration
	
	private ServerConfiguration m_serverConfig;

	/**
	 * Default constructor
	 */
	public FileServerConfig()
	{
	}

	/**
	 * Set the file server configuration
	 * 
	 * @return ServerConfiguration
	 */
	public ServerConfiguration getFileServerConfiguration()
	{
		return m_serverConfig;
	}
	
	/**
	 * Set the file server configuration
	 * 
	 * @param serverConfig ServerConfiguration
	 */
	public void setFileServerConfiguration(ServerConfiguration serverConfig)
	{
		m_serverConfig = serverConfig;
	}
	
	/**
	 * Check if the CIFS server is enabled
	 * 
	 * @return boolean
	 */
	public boolean isCIFSServerEnabled()
	{
		return m_serverConfig.hasConfigSection(CIFSConfigSection.SectionName);
	}
	
	/**
	 * Check if the FTP server is enabled
	 * 
	 * @return boolean
	 */
	public boolean isFTPServerEnabled()
	{
		return m_serverConfig.hasConfigSection(FTPConfigSection.SectionName);
	}
	
	/**
	 * Check if the NFS server is enabled
	 * 
	 * @return boolean
	 */
	public boolean isNFSServerEnabled()
	{
		return m_serverConfig.hasConfigSection(NFSConfigSection.SectionName);
	}
	
	/**
	 * Return the CIFS server name
	 * 
	 * @return String
	 */
	public String getCIFSServerName()
	{
		return m_serverConfig.getServerName();
	}
	
	/**
	 * Return the CIFS server IP address
	 * 
	 * @return String
	 */
	public String getCIFSServerAddress()
	{
		return null;
	}

	/**
	 * Create a mounter to mount/unmount a share on the CIFS server
	 * 
	 * @return CifsMounter
	 */
	public CifsMounter createMounter() {
		
		// Check if the CIFS server is enabled
		
		if ( isCIFSServerEnabled() == false)
			return null;

		// Access the CIFS configuration
		
		CIFSConfigSection cifsConfig = (CIFSConfigSection) m_serverConfig.getConfigSection(CIFSConfigSection.SectionName);
		
		// Create the CIFS mounter
		
		CifsMounter cifsMounter = new CifsMounter();
		cifsMounter.setServerName( getCIFSServerName());
		
		// Set the server address if the global bind address has been set
		
		if ( cifsConfig.hasSMBBindAddress())
		{
			// Use the global CIFS server bind address
			
			cifsMounter.setServerAddress( cifsConfig.getSMBBindAddress().getHostAddress());
		}
		
		// Get the local platform type
		
		Platform.Type platform = Platform.isPlatformType();
		
		// Figure out which CIFS sub-protocol to use to connect to the server
		
		if ( platform == Platform.Type.LINUX && cifsConfig.hasTcpipSMB())
		{
			// Connect using native SMB, this defaults to port 445 but may be running on a different port
			
			cifsMounter.setProtocolType( CifsMounter.NativeSMB);
			
			// Check if the native SMB server is listening on a non-standard port
			
			if ( cifsConfig.getTcpipSMBPort() != TcpipSMB.PORT)
				cifsMounter.setProtocolPort( cifsConfig.getTcpipSMBPort());
		}
		else
		{
			// Check if the server is using Win32 NetBIOS
			
			if ( cifsConfig.hasWin32NetBIOS())
				cifsMounter.setProtocolType( CifsMounter.Win32NetBIOS);
			else if ( cifsConfig.hasNetBIOSSMB())
			{
				// Set the protocol type for Java socket based NetBIOS
				
				cifsMounter.setProtocolType( CifsMounter.NetBIOS);
				
				// Check if the socket NetBIOS is bound to a particular address
				
				if ( cifsConfig.hasNetBIOSBindAddress())
					cifsMounter.setServerAddress( cifsConfig.getNetBIOSBindAddress().getHostAddress());
			}
		}
		
		// Check if the CIFS mounter server address has been set, if not then get the local address
		
		if ( cifsMounter.getServerAddress() == null)
		{
			// Set the CIFS mounter server address
			
			try
			{
				cifsMounter.setServerAddress( InetAddress.getLocalHost().getHostAddress());
			}
			catch ( java.net.UnknownHostException ex)
			{
				logger.error( "Failed to get local IP address", ex);
			}
		}
		
		// Return the CIFS mounter
		
		return cifsMounter;
	}
}
