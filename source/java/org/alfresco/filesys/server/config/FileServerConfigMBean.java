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

import org.alfresco.filesys.util.CifsMounter;

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
	 * Check if the FTP server is enabled
	 * 
	 * @return boolean
	 */
	public boolean isFTPServerEnabled();
	
	/**
	 * Check if the NFS server is enabled
	 * 
	 * @return boolean
	 */
	public boolean isNFSServerEnabled();
	
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
	
	/**
	 * Create a mounter to mount/unmount a share on the CIFS server
	 * 
	 * @return CifsMounter
	 */
	public CifsMounter createMounter();
}
