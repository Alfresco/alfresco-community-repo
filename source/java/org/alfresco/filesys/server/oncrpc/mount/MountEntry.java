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
package org.alfresco.filesys.server.oncrpc.mount;

/**
 * Mount Entry Class
 * 
 * <p>Contains the details of an active NFS mount.
 * 
 * @author GKSpencer
 */
public class MountEntry {

	//	Remote host name/address
		
	private String m_host;
		
	//	Mount path
		
	private String m_path;
				
	/**
	 * Class constructor
	 * 
	 * @param host String
	 * @param path String
	 */
	public MountEntry(String host, String path) {
		m_host = host;
		m_path = path;
	}
	
	/**
	 * Return the host name/address
	 * 
	 * @return String
	 */
	public final String getHost() {
		return m_host;
	}
	
	/**
	 * Return the mount path
	 *
	 * @return String
	 */
	public final String getPath() {
		return m_path;
	}
	
	/**
	 * Return the mount entry as a string
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();
		
		str.append("[");
		str.append(getHost());
		str.append(":");
		str.append(getPath());
		str.append("]");
		
		return str.toString();
	}
}
