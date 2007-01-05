/*
 * Copyright (C) 2005 Alfresco, Inc.
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
