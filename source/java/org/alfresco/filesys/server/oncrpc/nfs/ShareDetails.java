/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.filesys.server.oncrpc.nfs;

/**
 * Share Details Class
 * 
 * <p>Contains the file id cache, active search cache and tree connection details
 * of a shared filesystem.
 * 
 * @author GKSpencer
 */
public class ShareDetails {

	// Share name

	private String m_name;

	// File id to path conversion cache

	private FileIdCache m_idCache;

	// Flag to indicate if the filesystem driver for this share supports file id
	// lookups
	// via the FileIdInterface

	private boolean m_fileIdLookup;

	/**
	 * Class constructor
	 * 
	 * @param name String
	 * @param fileIdSupport boolean
	 */
	public ShareDetails(String name, boolean fileIdSupport)
	{

		// Save the share name

		m_name = name;

		// Set the file id support flag

		m_fileIdLookup = fileIdSupport;

		// Create the file id and search caches

		m_idCache = new FileIdCache();
	}

	/**
	 * Return the share name
	 * 
	 * @return String
	 */
	public final String getName()
	{
		return m_name;
	}

	/**
	 * Return the file id cache
	 * 
	 * @return FileIdCache
	 */
	public final FileIdCache getFileIdCache()
	{
		return m_idCache;
	}

	/**
	 * Determine if the filesystem driver for this share has file id support
	 * 
	 * @return boolean
	 */
	public final boolean hasFileIdSupport()
	{
		return m_fileIdLookup;
	}
}
