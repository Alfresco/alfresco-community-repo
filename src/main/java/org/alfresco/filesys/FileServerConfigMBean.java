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

/**
 * File Server Configuration MBean Interface
 * 
 * <p>Provides file server configuration details to remote virtualization servers.
 * 
 * @author gkspencer
 */
public interface FileServerConfigMBean {

	/**
	 * Check if the CIFS server is enabled
	 * 
	 * @return boolean
	 */
	public boolean isCIFSServerEnabled();
	
	/**
	 * Enable/disable CIFS server
	 * 
	 * @param enabled  true to enable, false to disable
	 */	
	public void setCIFSServerEnabled(boolean enabled) throws Exception;

	/**
	 * Check if the FTP server is enabled
	 * 
	 * @return boolean
	 */
	public boolean isFTPServerEnabled();
	
	/**
	 * Enable/disable FTP server
	 * 
	 * @param enabled  true to enable, false to disable
	 */	
	public void setFTPServerEnabled(boolean enabled) throws Exception;
		
	/**
	 * Return the CIFS server name
	 * 
	 * @return String
	 */
	public String getCIFSServerName();
	
	/**
	 * Return the CIFS server IP address
	 * 
	 * @return String
	 */
	public String getCIFSServerAddress();
}
