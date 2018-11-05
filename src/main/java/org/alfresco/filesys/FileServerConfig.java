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

import org.alfresco.jlan.ftp.FTPConfigSection;
import org.alfresco.jlan.server.config.ServerConfiguration;
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
}
