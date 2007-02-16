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

package org.alfresco.filesys.smb.server;

import java.util.Enumeration;
import java.util.Hashtable;

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.auth.ClientInfo;
import org.alfresco.filesys.server.core.DeviceInterface;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.filesys.SearchContext;
import org.alfresco.filesys.server.filesys.TooManyConnectionsException;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Virtual Circuit Class
 * 
 * <p>
 * Represents an authenticated circuit on an SMB/CIFS session. There may be
 * multiple virtual circuits opened on a single session/socket connection.
 */
public class VirtualCircuit {

	// Debug logging

	private static Log logger = LogFactory.getLog("org.alfresco.smb.protocol");

	// Default and maximum number of connection slots

	public static final int DefaultConnections = 4;
	public static final int MaxConnections     = 16;

	// Tree ids are 16bit values

	private static final int TreeIdMask = 0x0000FFFF;

	// Default and maximum number of search slots

	private static final int DefaultSearches = 8;
	private static final int MaxSearches     = 256;

	// Invalid UID value

	public static final int InvalidUID = -1;

	// Virtual circuit UID value
	//
	// Allocated by the server and sent by the client to identify the virtual circuit

	private int m_uid = -1;

	// Virtual circuit number

	private int m_vcNum;

	// Client information for this virtual circuit

	private ClientInfo m_clientInfo;

	// Active tree connections

	private Hashtable<Integer, TreeConnection> m_connections;

	private int m_treeId;

	// List of active searches

	private SearchContext[] m_search;

	private int m_searchCount;

	// Active transaction details

	private SrvTransactBuffer m_transact;

	/**
	 * Class constructor
	 * 
	 * @param vcNum
	 *            int
	 * @param cInfo
	 *            ClientInfo
	 */
	public VirtualCircuit(int vcNum, ClientInfo cInfo) {
		m_vcNum = vcNum;
		m_clientInfo = cInfo;
	}

	/**
	 * Return the virtual circuit UID
	 * 
	 * @return int
	 */
	public final int getUID() {
		return m_uid;
	}

	/**
	 * Return the virtual circuit number
	 * 
	 * @return int
	 */
	public final int getVCNumber() {
		return m_vcNum;
	}

	/**
	 * Return the client information
	 * 
	 * @return ClientInfo
	 */
	public final ClientInfo getClientInformation() {
		return m_clientInfo;
	}

	/**
	 * Add a new connection to this virtual circuit. Return the allocated tree
	 * id for the new connection.
	 * 
	 * @param shrDev SharedDevice
	 * @return int Allocated tree id (connection id).
	 */
	public int addConnection(SharedDevice shrDev)
			throws TooManyConnectionsException {

		// Check if the connection array has been allocated

		if (m_connections == null)
			m_connections = new Hashtable<Integer, TreeConnection>(DefaultConnections);

		// Allocate an id for the tree connection

		int treeId = 0;

		synchronized (m_connections) {

			// Check if the tree connection table is full

			if (m_connections.size() == MaxConnections)
				throw new TooManyConnectionsException();

			// Find a free slot in the connection array

			treeId = (m_treeId++ & TreeIdMask);
			Integer key = new Integer(treeId);

			while (m_connections.contains(key)) {

				// Try another tree id for the new connection

				treeId = (m_treeId++ & TreeIdMask);
				key = new Integer(treeId);
			}

			// Store the new tree connection

			m_connections.put(key, new TreeConnection(shrDev));
		}

		// Return the allocated tree id

		return treeId;
	}

	/**
	 * Return the tree connection details for the specified tree id.
	 * 
	 * @return com.starla.smbsrv.TreeConnection
	 * @param treeId
	 *            int
	 */
	public final TreeConnection findConnection(int treeId) {

		// Check if the tree id and connection array are valid

		if (m_connections == null)
			return null;

		// Get the required tree connection details

		return m_connections.get(new Integer(treeId));
	}

	/**
	 * Remove the specified tree connection from the active connection list.
	 * 
	 * @param treeId
	 *            int
	 * @param srvSession
	 *            SrvSession
	 */
	protected void removeConnection(int treeId, SrvSession sess) {

		// Check if the tree id is valid

		if (m_connections == null)
			return;

		// Close the connection and remove from the connection list

		synchronized (m_connections) {

			// Get the connection

			Integer key = new Integer(treeId);
			TreeConnection tree = m_connections.get(key);

			// Close the connection, release resources

			if (tree != null) {

				// Close the connection

				tree.closeConnection(sess);

				// Remove the connection from the connection list

				m_connections.remove(key);
			}
		}
	}

	/**
	 * Return the active tree connection count
	 * 
	 * @return int
	 */
	public final int getConnectionCount() {
		return m_connections != null ? m_connections.size() : 0;
	}

	/**
	 * Allocate a slot in the active searches list for a new search.
	 * 
	 * @return int Search slot index, or -1 if there are no more search slots
	 *         available.
	 */
	public final int allocateSearchSlot() {

		// Check if the search array has been allocated

		if (m_search == null)
			m_search = new SearchContext[DefaultSearches];

		// Find a free slot for the new search

		int idx = 0;

		while (idx < m_search.length && m_search[idx] != null)
			idx++;

		// Check if we found a free slot

		if (idx == m_search.length) {

			// The search array needs to be extended, check if we reached the
			// limit.

			if (m_search.length >= MaxSearches)
				return -1;

			// Extend the search array

			SearchContext[] newSearch = new SearchContext[m_search.length * 2];
			System.arraycopy(m_search, 0, newSearch, 0, m_search.length);
			m_search = newSearch;
		}

		// Return the allocated search slot index

		m_searchCount++;
		return idx;
	}

	/**
	 * Deallocate the specified search context/slot.
	 * 
	 * @param ctxId
	 *            int
	 */
	public final void deallocateSearchSlot(int ctxId) {

		// Check if the search array has been allocated and that the index is
		// valid

		if (m_search == null || ctxId >= m_search.length)
			return;

		// Close the search

		if (m_search[ctxId] != null)
			m_search[ctxId].closeSearch();

		// Free the specified search context slot

		m_searchCount--;
		m_search[ctxId] = null;
	}

	/**
	 * Return the search context for the specified search id.
	 * 
	 * @return com.starla.smbsrv.SearchContext
	 * @param srchId
	 *            int
	 */
	public final SearchContext getSearchContext(int srchId) {

		// Check if the search array is valid and the search index is valid

		if (m_search == null || srchId >= m_search.length)
			return null;

		// Return the required search context

		return m_search[srchId];
	}

	/**
	 * Store the seach context in the specified slot.
	 * 
	 * @param slot
	 *            Slot to store the search context.
	 * @param srch
	 *            com.starla.smbsrv.SearchContext
	 */
	public final void setSearchContext(int slot, SearchContext srch) {

		// Check if the search slot id is valid

		if (m_search == null || slot > m_search.length)
			return;

		// Store the context

		m_search[slot] = srch;
	}

	/**
	 * Return the number of active tree searches.
	 * 
	 * @return int
	 */
	public final int getSearchCount() {
		return m_searchCount;
	}

	/**
	 * Check if there is an active transaction
	 * 
	 * @return boolean
	 */
	public final boolean hasTransaction() {
		return m_transact != null ? true : false;
	}

	/**
	 * Return the active transaction buffer
	 * 
	 * @return TransactBuffer
	 */
	public final SrvTransactBuffer getTransaction() {
		return m_transact;
	}

	/**
	 * Set the active transaction buffer
	 * 
	 * @param buf
	 *            TransactBuffer
	 */
	public final void setTransaction(SrvTransactBuffer buf) {
		m_transact = buf;
	}

	/**
	 * Set the UID for the circuit
	 * 
	 * @param uid
	 *            int
	 */
	public final void setUID(int uid) {
		m_uid = uid;
	}

	/**
	 * Close the virtual circuit, close active tree connections
	 * 
	 * @param sess
	 *            SrvSession
	 */
	public final void closeCircuit(SrvSession sess) {

		// Debug

		if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_STATE))
			logger.debug("Cleanup vc=" + getVCNumber() + ", UID=" + getUID() + ", searches=" + getSearchCount()
					+ ", treeConns=" + getConnectionCount());

		// Check if there are any active searches

		if (m_search != null) {

			// Close all active searches

			for (int idx = 0; idx < m_search.length; idx++) {

				// Check if the current search slot is active

				if (m_search[idx] != null)
					deallocateSearchSlot(idx);
			}

			// Release the search context list, clear the search count

			m_search = null;
			m_searchCount = 0;
		}

		// Check if there are open tree connections

		if (m_connections != null) {

			synchronized (m_connections) {

				// Close all active tree connections

				Enumeration<TreeConnection> enm = m_connections.elements();

				while (enm.hasMoreElements()) {

					// Get the current tree connection

					TreeConnection tree = (TreeConnection) enm.nextElement();
					DeviceInterface devIface = tree.getInterface();

					// Check if there are open files on the share

					if (tree.openFileCount() > 0) {

						// Close the open files, release locks

						for (int i = 0; i < tree.getFileTableLength(); i++) {

							// Get an open file

							NetworkFile curFile = tree.findFile(i);
							if (curFile != null && devIface instanceof DiskInterface) {

								// Access the disk share interface

								DiskInterface diskIface = (DiskInterface) devIface;

								try {

									// Remove the file from the tree connection list

									tree.removeFile(i, sess);

									// Close the file

									diskIface.closeFile(sess, tree, curFile);
								}
								catch (Exception ex) {
								}
							}
						}
					}

					// Inform the driver that the connection has been closed

					if (devIface != null)
						devIface.treeClosed(sess, tree);
				}

				// Clear the tree connection list

				m_connections.clear();
			}
		}
	}

	/**
	 * Return the virtual circuit details as a string
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();

		str.append("[");
		str.append(getVCNumber());
		str.append(":");
		str.append(getUID());
		str.append(",");
		str.append(getClientInformation());
		str.append(",Tree=");
		str.append(getConnectionCount());
		str.append(",Searches=");
		str.append(getSearchCount());
		str.append("]");

		return str.toString();
	}
}