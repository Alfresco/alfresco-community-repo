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

import org.alfresco.filesys.smb.TcpipSMB;
import org.alfresco.filesys.util.CifsMounter;
import org.alfresco.filesys.util.Platform;

/**
 * File Server Configuration MBean Class
 * 
 * <p>Implements the file server configuration interface using the fileServerConfigurationBase bean
 * from network-protocol-context.xml.
 * 
 * @author gkspencer
 */
public class FileServerConfig implements FileServerConfigMBean {

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
		return m_serverConfig.isSMBServerEnabled();
	}
	
	/**
	 * Check if the FTP server is enabled
	 * 
	 * @return boolean
	 */
	public boolean isFTPServerEnabled()
	{
		return m_serverConfig.isFTPServerEnabled();
	}
	
	/**
	 * Check if the NFS server is enabled
	 * 
	 * @return boolean
	 */
	public boolean isNFSServerEnabled()
	{
//		return m_serverConfig.isNFSServerEnabled();
		return false;
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

		// Create the CIFS mounter
		
		CifsMounter cifsMounter = new CifsMounter();
		cifsMounter.setServerName( getCIFSServerName());
		
		// Get the local platform type
		
		Platform.Type platform = Platform.isPlatformType();
		
		// Figure out which CIFS sub-protocol to use to connect to the server
		
		if ( platform == Platform.Type.LINUX && m_serverConfig.hasTcpipSMB())
		{
			// Connect using native SMB, this defaults to port 445 but may be running on a different port
			
			cifsMounter.setProtocolType( CifsMounter.NativeSMB);
			
			// Check if the native SMB server is listening on a non-standard port
			
			if ( m_serverConfig.getTcpipSMBPort() != TcpipSMB.PORT)
				cifsMounter.setProtocolPort( m_serverConfig.getTcpipSMBPort());
		}
		else
		{
			// Check if the server is using Win32 NetBIOS
			
			if ( m_serverConfig.hasWin32NetBIOS())
				cifsMounter.setProtocolType( CifsMounter.Win32NetBIOS);
			else if ( m_serverConfig.hasNetBIOSSMB())
				cifsMounter.setProtocolType( CifsMounter.NetBIOS);
		}
		
		// Return the CIFS mounter
		
		return cifsMounter;
	}
}
