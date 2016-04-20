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
