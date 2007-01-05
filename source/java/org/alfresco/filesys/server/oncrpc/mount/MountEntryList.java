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

import java.util.*;

/**
 * Mount Entry List Class
 * 
 * <p>Contains a list of active mount entries.
 * 
 * @author GKSpencer
 */
public class MountEntryList {

	//	Mount entry list
	
	private Vector<MountEntry> m_mounts;
	
	/**
	 * Default constructor
	 */
	public MountEntryList() {
		m_mounts = new Vector<MountEntry>();
	}
	
	/**
	 * Ad an entry to the list
	 * 
	 * @param entry MountEntry
	 */
	public synchronized final void addEntry(MountEntry entry) {
		m_mounts.addElement(entry);
	}
	
	/**
	 * Return the number of entries in the list
	 *
	 * @return int
	 */
	public synchronized final int numberOfEntries() {
		return m_mounts.size();
	}
	
	/**
	 * Return the specified entry
	 * 
	 * @param idx
	 * @return MountEntry
	 */
	public synchronized final MountEntry getEntryAt(int idx) {
		if ( idx < 0 || idx >= m_mounts.size())
			return null;
		return (MountEntry) m_mounts.elementAt(idx);
	}
		
	/**
	 * Find an entry in the list
	 * 
	 * @param path String
	 * @param host String
	 * @return MountEntry
	 */
	public synchronized final MountEntry findEntry(String path, String host) {
		for ( int i = 0; i < m_mounts.size(); i++) {
			MountEntry entry = (MountEntry) m_mounts.elementAt(i);

			if ( host.compareTo(entry.getHost()) == 0 && path.compareTo(entry.getPath()) == 0)
				return entry;
		}
		return null;
	}

	/**
	 * Remove an entry from the list
	 * 
	 * @param path String
	 * @param host String
	 * @return MountEntry
	 */
	public synchronized final MountEntry removeEntry(String path, String host) {
		for ( int i = 0; i < m_mounts.size(); i++) {
			MountEntry entry = (MountEntry) m_mounts.elementAt(i);

			if ( host.compareTo(entry.getHost()) == 0 && path.compareTo(entry.getPath()) == 0) {
				m_mounts.removeElementAt(i);
				return entry;
			}
		}
		return null;
	}

	/**
	 * Remove all entries from the list for the specified host
	 * 
	 * @param host String
	 */
	public synchronized final void removeHostEntries(String host) {
		for ( int i = 0; i < m_mounts.size(); i++) {
			MountEntry entry = (MountEntry) m_mounts.elementAt(i);

			if ( host.compareTo(entry.getHost()) == 0)
				m_mounts.removeElementAt(i);
		}
	}

	/**
	 * Find all items for the specified host and return as a new list
	 * 
	 * @param host String
	 * @return MountEntryList
	 */
	public synchronized final MountEntryList findSessionEntries(String host) {
		
		//	Allocate the list to hold the matching entries
		
		MountEntryList list = new MountEntryList();
		
		//	Find the matching entries
		
		for ( int i = 0; i < m_mounts.size(); i++) {
			MountEntry entry = (MountEntry) m_mounts.elementAt(i);
			if ( host.compareTo(entry.getHost()) == 0)
				list.addEntry(entry);
		}
		
		//	Check if the list is empty, return the list
		
		if ( list.numberOfEntries() == 0)
			list = null;
		return list;
	}
	
	/**
	 * Remote all entries from the list
	 */
	public synchronized final void removeAllItems() {
		m_mounts.removeAllElements();
	}
}
