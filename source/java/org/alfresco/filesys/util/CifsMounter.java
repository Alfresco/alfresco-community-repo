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
/**
 * 
 */
package org.alfresco.filesys.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.jlan.util.Platform;
import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CIFS Mounter Class
 * 
 * <p>Mount/map a network drive taking care of platform differences.
 * 
 * @author gkspencer
 */
public class CifsMounter {

	// Debug logging
	
    private static final Log logger = LogFactory.getLog( CifsMounter.class);
	
	// Protocol type constants
	
    public static final int Default     	= 0;
	public static final int NetBIOS			= 1;
	public static final int NativeSMB		= 2;
	public static final int Win32NetBIOS	= 3;
	
	// Windows mount/unmount commands
	
	private static final String WindowsMountCmd 	= "net use ${drive} \\\\${srvname}\\${sharename} ${password} /USER:${username}";
	private static final String WindowsUnMountCmd	= "net use ${drive} /d";

	// Linux mount/unmount commands

	private static final String LinuxMountSmbfsCmd	= "mount -t smbfs //${srvname}/${sharename} ${mountpoint} -o username=${username},password=${password}";
	private static final String LinuxMountCifsCmd	= "mount -t cifs  //${srvname}/${sharename} ${mountpoint} -o username=${username},password=${password}";
	private static final String LinuxMountCifsNBCmd = "mount.cifs //${srvname}/${sharename} ${mountpoint} -o servern=${srvname},port=139,username=${username},password=${password}";
	private static final String LinuxUnMountCmd		= "umount ${mountpoint}";

	// Mac OS X mount/unmount commands
	
	private static final String MacOSXMountCmd		= "mount_smbfs -U ${username} //${password}@${srvname}/${sharename} ${mountpoint}";
	private static final String MacOSXUnMountCmd	= "umount ${mountpoint}";
	
	// Server name and share name
	
	private String m_srvName;
	private String m_shareName;
	
	// Server address
	
	private String m_srvAddr;
	
	// Access details for remote share
	
	private String m_userName;
	private String m_password;
	
	// Protocol to use for connection (non Windows platforms)
	
	private int m_protocolType = Default;
	
	// Port to connect on (non Windows platforms)
	
	private int m_port;
	
	/**
	 * Default constructor
	 */
	public CifsMounter()
	{
	}
	
	/**
	 * Class constructor
	 * 
	 * @param srvName String
	 * @param shareName String
	 * @param userName String
	 * @param password String
	 */
	public CifsMounter(String srvName, String shareName, String userName, String password)
	{
		setServerName( srvName);
		setShareName( shareName);
		
		setUserName( userName);
		setPassword( password);
	}
	
	/**
	 * Mount a remote CIFS shared filesystem
	 * 
	 * @param driveLetter String
	 * @param mountPoint String
	 * @exception CifsMountException
	 */
	public void mountFilesystem( String driveLetter, String mountPoint)
		throws CifsMountException
	{
		// Create the command line launcher
		
        RuntimeExec exec = new RuntimeExec();

        // Create the command map and properties
        
        Map<String, String> commandMap = new HashMap<String, String>( 1);
        Map<String, String> defProperties = new HashMap<String, String>( 10);
		
		// Determine the platform type and build the appropriate command line string
		
		Platform.Type platform = Platform.isPlatformType();
		
		switch ( platform)
		{
		// Windows
		
		case WINDOWS:
			commandMap.put( "Windows.*", WindowsMountCmd);
			break;
			
		// Linux
			
		case LINUX:
			
			// Check the protocol type of the CIFS server
			
			if ( isProtocolType() == NativeSMB || isProtocolType() == Default)
			{
				// Use the CIFS VFS mounter to connect using native SMB
				
				StringBuilder cmd = new StringBuilder( LinuxMountCifsCmd);
				if ( getProtocolPort() != 0)
				{
					cmd.append( ",port=");
					cmd.append( getProtocolPort());
				}

				// Set the command line
				
				commandMap.put( "Linux", cmd.toString());
			}
			else
			{
				// Set the command line to use the CIFS VFS mounter to connect using NetBIOS
				
				StringBuilder cmd = new StringBuilder( LinuxMountCifsNBCmd);
				
				if ( getServerAddress() != null)
				{
					cmd.append( ",ip=");
					cmd.append( getServerAddress());
				}
				
				// Set the command line
				
				commandMap.put( "Linux", cmd.toString());
			}
			break;
			
		// Mac OS X
			
		case MACOSX:
			commandMap.put( "Mac OS X", MacOSXMountCmd);
			break;
		}
		
		// Set the command map
		
		exec.setCommandMap( commandMap);
		
		// Build the command line properties list
		
        defProperties.put( "drive", driveLetter);
        defProperties.put( "srvname", getServerName());
        defProperties.put( "sharename", getShareName());
        defProperties.put( "username", getUserName());
        defProperties.put( "password", getPassword());
        defProperties.put( "mountpoint", mountPoint);
        
        exec.setDefaultProperties(defProperties);
		
        // Get the command to be used on this platform

        if ( logger.isDebugEnabled())
        	logger.debug( "Mount CIFS share, cmdLine=" + Arrays.toString(exec.getCommand()));
        
        // Run the command
        
        ExecutionResult execRes = exec.execute();
        
        if ( logger.isDebugEnabled())
        	logger.debug( "Mount result=" + execRes);
        
        // Check if the command was successful
        
        if ( execRes.getSuccess() == false)
        	throw new CifsMountException( execRes.getExitValue(), execRes.getStdOut(), execRes.getStdErr());
	}
	
	/**
	 * Unmount a remote CIFS shared filesystem
	 * 
	 * @param driveLetter String
	 * @param mountPoint String
	 * @exception CifsMountException
	 */
	public void unmountFilesystem( String driveLetter, String mountPoint)
		throws CifsMountException
	{
		// Create the command line launcher
		
        RuntimeExec exec = new RuntimeExec();

        // Create the command map and properties
        
        Map<String, String> commandMap = new HashMap<String, String>( 1);
        Map<String, String> defProperties = new HashMap<String, String>( 10);
		
		// Determine the platform type and build the appropriate command line string
		
		Platform.Type platform = Platform.isPlatformType();
		
		switch ( platform)
		{
		// Windows
		
		case WINDOWS:
			commandMap.put( "Windows.*", WindowsUnMountCmd);
			break;
			
		// Linux
			
		case LINUX:
			commandMap.put( "Linux", LinuxUnMountCmd);
			break;
			
		// Mac OS X
			
		case MACOSX:
			commandMap.put( "Mac OS X", MacOSXUnMountCmd);
			break;
		}
		
		// Set the command map
		
		exec.setCommandMap( commandMap);
		
		// Build the command line properties list
		
        defProperties.put( "drive", driveLetter);
        defProperties.put( "mountpoint", mountPoint);
        
        exec.setDefaultProperties(defProperties);
		
        // Get the command to be used on this platform

        if ( logger.isDebugEnabled())
        	logger.debug( "UnMount CIFS share, cmdLine=" + Arrays.toString(exec.getCommand()));
        
        // Run the command
        
        ExecutionResult execRes = exec.execute();
        
        if ( logger.isDebugEnabled())
        	logger.debug( "UnMount result=" + execRes);
        
        // Check if the command was successful
        
        if ( execRes.getSuccess() == false)
        	throw new CifsMountException( execRes.getExitValue(), execRes.getStdOut(), execRes.getStdErr());
	}
	
	/**
	 * Return the server name
	 * 
	 * @return String
	 */
	public final String getServerName()
	{
		return m_srvName;
	}

	/**
	 * Return hte server address
	 * 
	 * @return String
	 */
	public final String getServerAddress()
	{
		return m_srvAddr;
	}
	
	/**
	 * Return the share name
	 * 
	 * @return String
	 */
	public final String getShareName()
	{
		return m_shareName;
	}
	
	/**
	 * Return the user name
	 * 
	 * @return String
	 */
	public final String getUserName()
	{
		return m_userName;
	}
	
	/**
	 * Return the password
	 * 
	 * @return String
	 */
	public final String getPassword()
	{
		return m_password;
	}
	
	/**
	 * Return the protocol type
	 * 
	 * @return int
	 */
	public final int isProtocolType()
	{
		return m_protocolType;
	}
	
	/**
	 * Return the protocol port
	 * 
	 * @return int
	 */
	public final int getProtocolPort()
	{
		return m_port;
	}
	
	/**
	 * Set the server name
	 * 
	 * @param name String
	 */
	public final void setServerName(String name)
	{
		m_srvName = name;
	}
	
	/**
	 * Set the server address
	 * 
	 * @param srvAddr String
	 */
	public final void setServerAddress(String srvAddr)
	{
		m_srvAddr = srvAddr;
	}
	
	/**
	 * Set the share name
	 * 
	 * @param name String
	 */
	public final void setShareName(String name)
	{
		m_shareName = name;
	}
	
	/**
	 * Set the user name
	 * 
	 * @param user String
	 */
	public final void setUserName(String user)
	{
		m_userName = user;
	}
	
	/**
	 * Set the password
	 * 
	 * @param password String
	 */
	public final void setPassword(String password)
	{
		m_password = password;
	}
	
	/**
	 * Set the protocol type to use
	 * 
	 * @param proto int
	 */
	public final void setProtocolType(int proto)
	{
		m_protocolType = proto;
	}
	
	/**
	 * Set the port to use for the connection
	 * 
	 * @param port int
	 */
	public final void setProtocolPort(int port)
	{
		m_port = port;
	}
	
	/**
	 * Return the CIFS mounter as a string
	 * 
	 * @return String
	 */
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		
		str.append( "[\\\\");
		str.append( getServerName());
		str.append( "\\");
		str.append( getShareName());
		str.append( ",");
		str.append( getUserName());
		str.append( ",");
		str.append( getPassword());
		
		if ( isProtocolType() != Default)
		{
			str.append( " (");
			if ( isProtocolType() == NetBIOS)
				str.append ( "NetBIOS");
			else if ( isProtocolType() == NativeSMB)
				str.append( "NativeSMB");
			else if ( isProtocolType() == Win32NetBIOS)
				str.append( "Win32NetBIOS");
			
			if ( getProtocolPort() != 0)
			{
				str.append( ",");
				str.append( getProtocolPort());
			}
			str.append( ")");
		}
		str.append( "]");
		
		return str.toString();
	}
}
