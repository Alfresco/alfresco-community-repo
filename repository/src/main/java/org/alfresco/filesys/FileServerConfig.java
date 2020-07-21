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
	
	private FTPServerBean  m_ftpServer;
	private CIFSServerBean m_smbServer;

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
	 * Set the CIFS server
	 * 
	 * @param smbServer  CIFS server
	 */
	public void setCifsServer(CIFSServerBean smbServer)
	{
		m_smbServer = smbServer;
	}
	
	/**
	 * Check if the CIFS server is enabled
	 * 
	 * @return boolean
	 */
	public boolean isCIFSServerEnabled()
	{
		return (m_smbServer.isStarted() && m_serverConfig.hasConfigSection(CIFSConfigSection.SectionName));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.filesys.server.config.FileServerConfigMBean#setCIFSServerEnabled(boolean)
	 */
	public void setCIFSServerEnabled(boolean enabled) throws Exception
	{
		if (!enabled && isCIFSServerEnabled())
		{
			m_smbServer.stopServer();
		}
		
		if (enabled && !isCIFSServerEnabled())
		{
			m_smbServer.startServer();
		}
	}
	
	/**
	 * Set the FTP server
	 * 
	 * @param ftpServer  FTP server
	 */
	public void setFtpServer(FTPServerBean ftpServer)
	{
		m_ftpServer = ftpServer;
	}
	
	/**
	 * Check if the FTP server is enabled
	 * 
	 * @return boolean
	 */
	public boolean isFTPServerEnabled()
	{
		return (m_ftpServer.isStarted() && m_serverConfig.hasConfigSection(FTPConfigSection.SectionName));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.filesys.server.config.FileServerConfigMBean#setFTPServerEnabled(boolean)
	 */
	public void setFTPServerEnabled(boolean enabled) throws Exception
	{
		if (!enabled && isFTPServerEnabled())
		{
			m_ftpServer.stopServer();
		}
		
		if (enabled && !isFTPServerEnabled())
		{
			m_ftpServer.startServer();
		}
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
