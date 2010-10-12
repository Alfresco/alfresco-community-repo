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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.filesys.avm;

import org.alfresco.jlan.server.filesys.FileName;

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
	
	public static final String VersionNameHead	= "HEAD";
	
	// Folder name for the versions folder
	
	public static final String VersionsFolder	= "VERSION";
	
	// Head and version sub-folders
	
	public static final String DataFolder		= "DATA";
	public static final String MetaDataFolder	= "METADATA";
	
	// Version folder prefix
	
	public static final String VersionFolderPrefix	= "v";
	
    // AVM path seperator
    
    public static final char AVM_SEPERATOR			= '/';
    public static final String AVM_SEPERATOR_STR	= "/";

    // Level identifiers
    
    public enum LevelId { Invalid, Root, StoreRoot, Head, HeadData, HeadMetaData, VersionRoot, Version, VersionData, VersionMetaData, StoreRootPath, StorePath };
    
    // Level identifier for this path
    
    private LevelId m_levelId = LevelId.Invalid;
    
    // Store name
	
	private String m_storeName;
	
	// Version id
	
	private int m_version = InvalidVersionId;
	
	// Remaining path
	
	private String m_path;
	
	// AVM style path
	
	private String m_avmPath;

	// Path is read-only access
	
	private boolean m_readOnly;
	
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
	 * Return the level id for the path
	 * 
	 * @return LevelId
	 */
	public LevelId isLevel()
	{
		return m_levelId;
	}

	/**
	 * Check if the path is read-only
	 * 
	 * @return boolean
	 */
	public final boolean isReadOnlyAccess() {
	    return m_readOnly;
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
		return m_levelId == LevelId.Invalid ? false : true;
	}
	
	/**
	 * Check if the path is to a pseudo folder in the virtualization view
	 * 
	 * @return boolean
	 */
	public final boolean isPseudoPath()
	{
		return m_levelId == LevelId.Invalid || m_levelId == LevelId.StorePath || m_levelId == LevelId.StoreRootPath ? false : true;
	}

	/**
	 * Check if hte path is a read-only part of the pseudo folder tree
	 * 
	 * @return boolean
	 */
	public final boolean isReadOnlyPseudoPath()
	{
		 if ( isLevel() == LevelId.Root || isLevel() == LevelId.StoreRoot || isLevel() == LevelId.VersionRoot ||
				 isLevel() == LevelId.Head || isLevel() == LevelId.Version)
			 return true;
		 return false;
	}
	
	/**
	 * Check if the path is the root path
	 * 
	 * @return boolean
	 */
	public final boolean isRootPath()
	{
		return m_levelId == LevelId.Root ? true : false;
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
			m_levelId = LevelId.Root;
			return;
		}
		
		// Set the store name
		
		m_storeName = paths[0];
		m_levelId = LevelId.StoreRoot;
		
		if ( paths.length > 1)
		{
			// Validate the next element, should be either the HEAD or VERSIONS folder
			
			String levelStr = paths[1];
			
			if ( levelStr.equalsIgnoreCase( VersionNameHead))
			{
				m_version = -1;
				m_levelId = LevelId.Head;
			}
			else if ( levelStr.equalsIgnoreCase( VersionsFolder))
			{
				m_levelId = LevelId.VersionRoot;
			}
			else
			{
				// Invalid folder at the current level
				
				m_levelId = LevelId.Invalid;
				return;
			}
			
			// Check the next level, if available
			
			if ( paths.length > 2)
			{
				// If the previous level is the versions root then the next level should be a
				// version id folder

				String folderName = paths[2];
				int pathIdx = 3;
				
				if ( isLevel() == LevelId.VersionRoot)
				{
					// Check that the folder name starts with the version folder prefix
					
					if ( folderName != null && folderName.startsWith( VersionFolderPrefix) &&
							folderName.length() > VersionFolderPrefix.length())
					{						
						try
						{
							// Parse the version id
							
							m_version = Integer.parseInt( folderName.substring( VersionFolderPrefix.length()));
							m_levelId = LevelId.Version;
						
							// Validate the version id
							
							if ( m_version < -1)
							{
								// Invalid version id
								
								m_levelId = LevelId.Invalid;
								return;
							}
						}
						catch ( NumberFormatException ex)
						{
							m_levelId = LevelId.Invalid;
							return;
						}
						
						// Check for the next level
						
						if ( paths.length > 3)
						{
							// Get the next level name
							
							folderName = paths[3];
							pathIdx++;
							
							// Check for the data folder
							
							if ( folderName.equalsIgnoreCase( DataFolder))
							{
								m_levelId = LevelId.VersionData;

								// Set the path to the root of the store
								
								m_path = FileName.DOS_SEPERATOR_STR;
							}
							else if ( folderName.equalsIgnoreCase( MetaDataFolder))
							{
								m_levelId = LevelId.VersionMetaData;

								// Set the path to the root of the metadata
								
								m_path = FileName.DOS_SEPERATOR_STR;
							}
							else
							{
								m_levelId = LevelId.Invalid;
								return;
							}
						}
					}
					else
					{
						m_levelId = LevelId.Invalid;
						return;
					}
				}
				
				// If the previous level is head the next level should be the data or metadata folder
				
				else if ( isLevel() == LevelId.Head)
				{
					// Check for the data folder
					
					if ( folderName.equalsIgnoreCase( DataFolder))
					{
						m_levelId = LevelId.HeadData;

						// Set the path to the root of the store
						
						m_path = FileName.DOS_SEPERATOR_STR;
					}
					else if ( folderName.equalsIgnoreCase( MetaDataFolder))
					{
						m_levelId = LevelId.HeadMetaData;

						// Set the path to the root of the metadata
						
						m_path = FileName.DOS_SEPERATOR_STR;
					}
					else
					{
						m_levelId = LevelId.Invalid;
						return;
					}
				}
				
				// If there are remaining paths then build a relative path
				if ( paths.length > pathIdx)
				{
                    StringBuilder pathStr = new StringBuilder();
                    
                    for ( int i = pathIdx; i < paths.length; i++)
                    {
                        pathStr.append( FileName.DOS_SEPERATOR);
                        pathStr.append( paths[i]);
                    }
                    
                    m_path = pathStr.toString();

                    // ALF-1719: make "www" and "avm_webapps" read only by setting their level to
                    // StoreRootPath (which is checked in AVMDiskDriver.checkPathAccess).
				    String lastPath = paths[paths.length-1].toLowerCase();
	                if(lastPath.equals("www") || lastPath.equals("avm_webapps"))
	                {
	                    // Set the level to indicate a store root path i.e. "www",
	                    // "avm_webapps"
	                    m_levelId = LevelId.StoreRootPath;
	                }
	                else
	                {
	                    // Set the level to indicate a store relative path
	                    m_levelId = LevelId.StorePath;
	                }
				}

				// Build the AVM path, in <store>:/<path> format
				
				if ( m_path != null)
				{
					StringBuilder pathStr = new StringBuilder();
				
					pathStr.append( m_storeName);
					pathStr.append( ":");
					pathStr.append( m_path.replace( FileName.DOS_SEPERATOR, AVM_SEPERATOR));
					
					m_avmPath = pathStr.toString();
				}
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

		m_levelId   = LevelId.Invalid;
		
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
    	
    	// Indicate that the path is to a store relative path

        String lowerPath = path.toLowerCase();
        String[] paths = FileName.splitAllPaths(lowerPath);
        if(paths[paths.length - 1].equals("www") || paths[paths.length - 1].equals("avm_webapps"))
        {
            // Set the level to indicate a store root path i.e. "www",
            // "avm_webapps"
            m_levelId = LevelId.StoreRootPath;
        }
        else
        {
            m_levelId = LevelId.StorePath;
        }
	}
	
	/**
	 * Generate a file id for the path
	 * 
	 * @return int
	 */
	public final int generateFileId()
	{
		// Check if the path is a store path or pseudo path
		
		int fid = -1;
		
		if ( isLevel() == LevelId.StorePath || isLevel() == LevelId.StoreRootPath)
		{
			// Use the share relative path to generate the file id
			
			fid = getRelativePath().hashCode();
		}
		else if ( isPseudoPath())
		{
			// Create a relative path to the pseudo folder
			
			StringBuilder relStr = new StringBuilder();
			relStr.append( FileName.DOS_SEPERATOR);
			
			switch( isLevel())
			{
			case StoreRoot:
				relStr.append( getStoreName());
				break;
			case Head:
				relStr.append( getStoreName());
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.VersionNameHead);
				break;
			case HeadData:
				relStr.append( getStoreName());
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.VersionNameHead);
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.DataFolder);
				break;
			case HeadMetaData:
				relStr.append( getStoreName());
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.VersionNameHead);
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.MetaDataFolder);
				break;
			case VersionRoot:
				relStr.append( getStoreName());
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.VersionsFolder);
				break;
			case Version:
				relStr.append( getStoreName());
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.VersionsFolder);
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.VersionFolderPrefix);
				relStr.append( getVersion());
				break;
			case VersionData:
				relStr.append( getStoreName());
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.VersionsFolder);
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.VersionFolderPrefix);
				relStr.append( getVersion());
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.DataFolder);
				break;
			case VersionMetaData:
				relStr.append( getStoreName());
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.VersionsFolder);
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.VersionFolderPrefix);
				relStr.append( getVersion());
				relStr.append( FileName.DOS_SEPERATOR);
				relStr.append( AVMPath.MetaDataFolder);
				break;
			}
			
			// Generate the file id using the pseudo folder relative path
			
			fid = relStr.toString().hashCode();
		}
		
		// Return the file id
		
		return fid;
	}

	/**
	 * Set the path access, true for read-only access
	 * 
	 * @param access boolean
	 */
	public final void setReadOnlyAccess( boolean readOnly)
	{
		m_readOnly = readOnly;
	}
	
	/**
	 * Return the AVM path details as a string
	 * 
	 * @return String
	 */
	public String toString()
	{
		StringBuilder str = new StringBuilder();

		switch ( m_levelId)
		{
			case Invalid:
				str.append("[Invalid");
				break;
			case Root:
				str.append("[Root");
				break;
			case StoreRoot:
				str.append("[StoresRoot");
				break;
			case Head:
				str.append("[");
				str.append(getStoreName());
				str.append(":HEAD");
				break;
			case HeadData:
				str.append("[");
				str.append(getStoreName());
				str.append(":HEAD\\");
				str.append( DataFolder);
				break;
			case HeadMetaData:
				str.append("[");
				str.append(getStoreName());
				str.append(":HEAD\\");
				str.append( MetaDataFolder);
				break;
			case VersionRoot:
				str.append("[");
				str.append(getStoreName());
				str.append(":Versions");
				break;
			case Version:
				str.append("[");
				str.append(getStoreName());
				str.append(":");
				str.append(VersionFolderPrefix);
				str.append(getVersion());
				break;
			case VersionData:
				str.append("[");
				str.append(getStoreName());
				str.append(":");
				str.append(VersionFolderPrefix);
				str.append(getVersion());
				str.append("\\");
				str.append( DataFolder);
				break;
			case VersionMetaData:
				str.append("[");
				str.append(getStoreName());
				str.append(":");
				str.append(VersionFolderPrefix);
				str.append(getVersion());
				str.append("\\");
				str.append( MetaDataFolder);
				break;
            case StoreRootPath:
			case StorePath:
				str.append("[");
				str.append(getStoreName());
				str.append(":");
				str.append(VersionFolderPrefix);
				str.append(getVersion());
				str.append(",");
				str.append(getRelativePath());
				str.append(":");
				str.append(getAVMPath());
				break;
		}
		
		if ( isReadOnlyAccess())
			str.append("-ReadOnly");
		
		str.append("]");
		
		return str.toString();
	}
}
