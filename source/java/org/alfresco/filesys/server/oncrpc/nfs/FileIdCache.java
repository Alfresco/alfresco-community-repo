/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 
import java.util.*;

/**
 * File Id Cache Class
 * 
 * <p>Converts a file/directory id to a share relative path.
 * 
 * @author GKSpencer
 */
public class FileIdCache {

	//	File id to path cache
	
	private Hashtable<Integer, String> m_idCache;
	
	/**
	 * Default constructor
	 */
	public FileIdCache() {
		m_idCache = new Hashtable<Integer, String>();
	}
	
	/**
	 * Add an entry to the cache
	 * 
	 * @param fid int
	 * @param path String
	 */
	public final void addPath(int fid, String path) {
		m_idCache.put(new Integer(fid), path);
	}
	
	/**
	 * Convert a file id to a path
	 * 
	 * @param fid int
	 * @return String
	 */
	public final String findPath(int fid) {
		return (String) m_idCache.get(new Integer(fid));
	}
	
	/**
	 * Delete an entry from the cache
	 * 
	 * @param fid int
	 */
	public final void deletePath(int fid) {
		m_idCache.remove(new Integer(fid));
	}
}
