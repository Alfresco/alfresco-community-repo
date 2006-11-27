/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.filesys.avm;

import org.alfresco.filesys.server.filesys.FileName;

/**
 * AVM Path Class
 * 
 * <p>Parses a share relative path into store, version and remaining path values.
 *
 * @author gkspencer
 */
public class AVMPath {

	// Constants
	//
	// Invalid version id value
	
	private static final int InvalidVersionId	= -2;
	
	// Version id string for the head version
	
	public static final String VersionNameHead	= "Head";
	
    // AVM path seperator
    
    public static final char AVM_SEPERATOR			= '/';
    public static final String AVM_SEPERATOR_STR	= "/";

    // Store name
	
	private String m_storeName;
	
	// Version id
	
	private int m_version = InvalidVersionId;
	
	// Remaining path
	
	private String m_path;
	
	// AVM style path
	
	private String m_avmPath;

	/**
	 * Default constructor
	 */
	public AVMPath()
	{
	}
	
	/**
	 * Class constructor
	 *
	 * <p>Construct an AVM path for the virtualization view, with store and version folders
	 * 
	 * @param shrPath String
	 */
	public AVMPath(String shrPath)
	{
		// Parse the path
		
		parsePath( shrPath);
	}

	/**
	 * Class constructor
	 * 
	 * <p>Construct an AVM path for a standard view onto a store/version
	 * 
	 * @param storeName String
	 * @param version int
	 * @param path String
	 */
	public AVMPath(String storeName, int version, String path)
	{
		// Parse the path
		
		parsePath( storeName, version, path);
	}
	
	/**
	 * Return the store name
	 * 
	 * @return String
	 */
	public final String getStoreName()
	{
		return m_storeName;
	}

	/**
	 * Check if the version id was specified in the path
	 * 
	 * @return boolean
	 */
	public final boolean hasVersion()
	{
		return m_version != InvalidVersionId ? true : false;
	}
	
	/**
	 * Return the version id
	 * 
	 * @return int
	 */
	public final int getVersion()
	{
		return m_version;
	}
	
	/**
	 * Return the version as a string
	 * 
	 * @return String
	 */
	public final String getVersionString()
	{
		if ( m_version == -1)
			return VersionNameHead;
		return "" + m_version;
	}
	
	/**
	 * Check if there is a share relative path
	 * 
	 * @return boolean
	 */
	public final boolean hasRelativePath()
	{
		return m_path != null ? true : false;
	}
	
	/**
	 * Return the share relative path
	 * 
	 * @return String
	 */
	public final String getRelativePath()
	{
		return m_path;
	}
	
	/**
	 * Return the AVM style path, in <store>:/<path> format
	 * 
	 * @return String
	 */
	public final String getAVMPath()
	{
		return m_avmPath;
	}
	
	/**
	 * Check if the path is valid
	 * 
	 * @return boolean
	 */
	public final boolean isValid()
	{
		return m_storeName == null && m_path == null ? false : true;
	}
	
	/**
	 * Check if the path is to a pseudo folder in the virtualization view
	 * 
	 * @return boolean
	 */
	public final boolean isPseudoPath()
	{
		return m_version == InvalidVersionId || m_path == null ? true : false;
	}
	
	/**
	 * Check if the path is the root path
	 * 
	 * @return boolean
	 */
	public final boolean isRootPath()
	{
		if ( m_path != null && m_path.equals( FileName.DOS_SEPERATOR_STR))
			return true;
		return false;
	}
	
	/**
	 * Parse the path, for the virtualization view onto all stores/versions
	 * 
	 * @param path String
	 */
	public final void parsePath( String path)
	{
		// Clear current settings
		
		m_storeName = null;
		m_version   = InvalidVersionId;
		m_path    = null;
		m_avmPath = null;
		
		// Split the path
		
		String[] paths = FileName.splitAllPaths(path);
		
		if ( paths == null || paths.length == 0)
		{
			m_path = FileName.DOS_SEPERATOR_STR;
			return;
		}
		
		// Set the store name
		
		m_storeName = paths[0];
		
		if ( paths.length > 1)
		{
			// Validate the version id
			
			String verStr = paths[1];
			if ( verStr.equalsIgnoreCase( VersionNameHead))
				m_version = -1;
			else
			{
				try
				{
					// Parse the version id
					
					m_version = Integer.parseInt( verStr);
					
					// Validate the version id
					
					if ( m_version < 0)
					{
						// Invalid version id
						
						m_storeName = null;
						return;
					}
				}
				catch ( NumberFormatException ex)
				{
					m_storeName = null;
					return;
				}
			}
			
			// If there additional path elements build the share and AVM relative paths
			
			if ( paths.length > 2)
			{
				// Build the share relative path
				
				StringBuilder pathStr = new StringBuilder();
				
				for ( int i = 2; i < paths.length; i++)
				{
					pathStr.append( FileName.DOS_SEPERATOR);
					pathStr.append( paths[i]);
				}
				
				m_path = pathStr.toString();
				
				// Build the AVM path, in <store>:/<path> format
				
				pathStr.setLength( 0);
				
				pathStr.append( m_storeName);
				pathStr.append( ":");
				pathStr.append( m_path.replace( FileName.DOS_SEPERATOR, AVM_SEPERATOR));
				
				m_avmPath = pathStr.toString();
			}
			else
			{
				// Share relative path is the root of the share
				
				m_path = FileName.DOS_SEPERATOR_STR;
				
				// Build the AVM path
				
				StringBuilder pathStr = new StringBuilder();
				
				pathStr.append( m_storeName);
				pathStr.append( ":/");
				
				m_avmPath = pathStr.toString();
			}
		}
	}
	
	/**
	 * Parse the path, to generate a path for a single store/version
	 * 
	 * @param storeName String
	 * @param version int
	 * @param path String
	 */
	public final void parsePath( String storeName, int version, String path)
	{
		// Clear current settings
		
		m_storeName = null;
		m_version   = InvalidVersionId;
		m_path    = null;
		m_avmPath = null;
		
		// Set the store/version
		
		m_storeName = storeName;
		m_version   = version;

		// Save the relative path
		
		m_path = path;
		
    	// Build the store path
    	
    	StringBuilder avmPath = new StringBuilder();
    	avmPath.append( m_storeName);
    	
    	if ( storeName.indexOf( ":") == -1)
    		avmPath.append( ":");
    	
    	if ( path == null || path.length() == 0)
    	{
    		avmPath.append( AVM_SEPERATOR);
    		
    		// Set the share relative path as the root path
    		
    		m_path = FileName.DOS_SEPERATOR_STR;
    	}
    	else
    	{
	    	if ( path.startsWith( FileName.DOS_SEPERATOR_STR) == false)
	    		avmPath.append( AVM_SEPERATOR);
	    	
	    	avmPath.append( path.replace( FileName.DOS_SEPERATOR, AVM_SEPERATOR));
    	}
    	
    	m_avmPath = avmPath.toString();
	}
	
	/**
	 * Return the AVM path details as a string
	 * 
	 * @return String
	 */
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		
		str.append("[");
		str.append(getStoreName());
		str.append(",");
		
		if ( hasVersion())
			str.append(getVersion());
		else
			str.append("NoVersion");
		
		str.append(",");
		str.append(getRelativePath());
		str.append(":");
		str.append(getAVMPath());
		str.append("]");
		
		return str.toString();
	}
}
