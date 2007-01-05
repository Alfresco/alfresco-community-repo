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
