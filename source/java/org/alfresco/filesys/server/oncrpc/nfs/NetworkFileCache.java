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

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.alfresco.filesys.server.oncrpc.RpcAuthenticator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Network File Cache Class
 * 
 * <p>Caches the network files that are currently being accessed by the NFS server.
 * 
 * @author GKSpencer
 */
public class NetworkFileCache {

	// Debug logging

	private static final Log logger = LogFactory.getLog(NetworkFileCache.class);
	
	// Default file timeout

	public static final long DefaultFileTimeout = 5000L;	// 5 seconds

	// Network file cache, key is the file id

	private Hashtable<Integer, FileEntry> m_fileCache;

	// File expiry thread

	private FileExpiry m_expiryThread;

	// RPC authenticator
	
	private RpcAuthenticator m_rpcAuthenticator;
	
	// File timeout

	private long m_fileTmo = DefaultFileTimeout;

	// Debug enable flag

	private boolean m_debug = false;

	/**
	 * File Entry Class
	 */
	protected class FileEntry {

		// Network file

		private NetworkFile m_file;

		// Disk share connection

		private TreeConnection m_conn;

		// File timeout

		private long m_timeout;
		
		// Session that last accessed the file
		
		private SrvSession m_sess;

		/**
		 * Class constructor
		 * 
		 * @param file NetworkFile
		 * @param conn TreeConnection
		 * @param sess SrvSession
		 */
		public FileEntry(NetworkFile file, TreeConnection conn, SrvSession sess) {
			m_file = file;
			m_conn = conn;
			setSession(sess);
			
			updateTimeout();
		}

		/**
		 * Return the file timeout
		 * 
		 * @return long
		 */
		public final long getTimeout() {
			return m_timeout;
		}

		/**
		 * Return the network file
		 * 
		 * @return NetworkFile
		 */
		public final NetworkFile getFile() {
			return m_file;
		}

		/**
		 * Return the disk share connection
		 * 
		 * @return TreeConnection
		 */
		public final TreeConnection getConnection() {
			return m_conn;
		}

		/**
		 * Get the session that last accessed the file
		 * 
		 * @return SrvSession
		 */
		public final SrvSession getSession()
		{
			return m_sess;
		}
		
		/**
		 * Update the file timeout
		 */
		public final void updateTimeout() {
			m_timeout = System.currentTimeMillis() + m_fileTmo;
		}

		/**
		 * Update the file timeout
		 * 
		 * @param tmo
		 *            long
		 */
		public final void updateTimeout(long tmo) {
			m_timeout = tmo;
		}
		
		/**
		 * Set the session that last accessed the file
		 * 
		 * @param sess SrvSession
		 */
		public final void setSession( SrvSession sess)
		{
			m_sess = sess;
		}
	};

	/**
	 * File Expiry Thread Class
	 */
	protected class FileExpiry implements Runnable {

		// Expiry thread

		private Thread m_thread;

		// Wakeup interval

		private long m_wakeup;

		// Shutdown flag

		private boolean m_shutdown;

		/**
		 * Class Constructor
		 * 
		 * @param wakeup
		 *            long
		 * @param name
		 *            String
		 */
		public FileExpiry(long wakeup, String name) {

			// Set the wakeup interval

			m_wakeup = wakeup;

			// Create and start the file expiry thread

			m_thread = new Thread(this);
			m_thread.setDaemon(true);
			m_thread.setName("NFSFileExpiry_" + name);
			m_thread.start();
		}

		/**
		 * Main thread method
		 */
		public void run() {

			// Loop until shutdown

			while (m_shutdown == false) {

				// Sleep for a while

				try {
					Thread.sleep(m_wakeup);
				} catch (InterruptedException ex) {
				}

				// Get the current system time

				long timeNow = System.currentTimeMillis();

				// Check for expired files

				synchronized (m_fileCache) {

					// Enumerate the cache entries

					Enumeration enm = m_fileCache.keys();

					while (enm.hasMoreElements()) {

						// Get the current key

						Integer fileId = (Integer) enm.nextElement();

						// Get the file entry and check if it has expired

						FileEntry fentry = (FileEntry) m_fileCache.get(fileId);

						if (fentry != null && fentry.getTimeout() < timeNow) {

							// Get the network file

							NetworkFile netFile = fentry.getFile();

							// Check if the file has an I/O request pending, if
							// so then reset the file expiry time
							// for the file

							if (netFile.hasIOPending()) {

								// Update the expiry time for the file entry

								fentry.updateTimeout();

								// DEBUG

								if (logger.isDebugEnabled())
									logger.debug("NFSFileExpiry: I/O pending file=" + fentry.getFile().getFullName() + ", fid=" + fileId);
							} else {

								// File entry has expired, remove it from the
								// cache

								m_fileCache.remove(fileId);

								// Close the file via the disk interface

								try {

									// Set the current user using the session that last accessed the file
									
									if ( m_rpcAuthenticator != null)
										m_rpcAuthenticator.setCurrentUser( fentry.getSession(), fentry.getSession().getClientInformation());
									
									// Get the disk interface

									DiskInterface disk = (DiskInterface) fentry.getConnection().getInterface();

									// Close the file

									disk.closeFile( fentry.getSession(), fentry.getConnection(), netFile);

									// Commit any transactions
									
									fentry.getSession().endTransaction();
									
									// DEBUG

									if (logger.isDebugEnabled())
										logger.debug("NFSFileExpiry: Closed file=" + fentry.getFile().getFullName() + ", fid=" + fileId);
								}
								catch (Exception ex) {
									logger.error( "File expiry exception", ex);
								}
							}
						}
					}
				}
			}
		}

		/**
		 * Request the file expiry thread to shutdown
		 */
		public final void requestShutdown() {

			// Set the shutdown flag

			m_shutdown = true;

			// Wakeup the thread

			try {
				m_thread.interrupt();
			} catch (Exception ex) {
			}

			// Wait for the expiry thread to complete

			try {
				m_thread.join(DefaultFileTimeout);
			} catch (Exception ex) {
			}
		}
	};

	/**
	 * Class constructor
	 * 
	 * @param name String
	 * @param rpcAuth RpcAuthenticator
	 */
	public NetworkFileCache(String name, RpcAuthenticator rpcAuth) {

		// Create the file cache

		m_fileCache = new Hashtable<Integer, FileEntry>();

		// Start the file expiry thread

		m_expiryThread = new FileExpiry(DefaultFileTimeout / 4, name);
		
		// Set the RPC authenticator
		
		m_rpcAuthenticator = rpcAuth;
	}

	/**
	 * Determine if debug output is enabled
	 * 
	 * @return boolean
	 */
	public final boolean hasDebug() {
		return m_debug;
	}

	/**
	 * Add a file to the cache
	 * 
	 * @param file NetworkFile
	 * @param conn TreeConnection
	 * @param sess SrvSession
	 */
	public synchronized final void addFile(NetworkFile file, TreeConnection conn, SrvSession sess) {

		// Add the file id mapping
		
		synchronized (m_fileCache) {
			m_fileCache.put(new Integer(file.getFileId()), new FileEntry(file, conn, sess));
		}
		
		// DEBUG
		
		if ( logger.isDebugEnabled())
			logger.debug("Added file " + file.getName() + ", fid=" + file.getFileId());
	}

	/**
	 * Remove a file from the cache
	 * 
	 * @param id
	 */
	public synchronized final void removeFile(int id) {

		// Create the search key

		Integer fileId = new Integer(id);

		synchronized (m_fileCache) {
			m_fileCache.remove(fileId);
		}
	}

	/**
	 * Find a file via the file id
	 * 
	 * @param id int
	 * @param sess SrvSession
	 * @return NetworkFile
	 */
	public synchronized final NetworkFile findFile(int id, SrvSession sess) {

		// Create the search key

		Integer fileId = new Integer(id);
		FileEntry fentry = null;

		synchronized (m_fileCache) {
			fentry = (FileEntry) m_fileCache.get(fileId);
		}

		// Return the file, or null if not found

		if (fentry != null) {

			// Update the file timeout and return the file

			fentry.updateTimeout();
			fentry.setSession(sess);
			
			return fentry.getFile();
		}

		// Invalid file id

		return null;
	}

	/**
	 * Return the count of entries in the cache
	 * 
	 * @return int
	 */
	public final int numberOfEntries() {
		return m_fileCache.size();
	}

	/**
	 * Close the expiry cache, close and remove all files from the cache and
	 * stop the expiry thread.
	 */
	public final void closeAllFiles() {

		// Enumerate the cache entries

		Enumeration keys = m_fileCache.keys();

		while (keys.hasMoreElements()) {

			// Get the current key and lookup the matching value

			Integer key = (Integer) keys.nextElement();
			FileEntry entry = (FileEntry) m_fileCache.get(key);

			// Expire the file entry

			entry.updateTimeout(0L);
		}

		// Shutdown the expiry thread, this should close the files

		m_expiryThread.requestShutdown();
	}

	/**
	 * Dump the cache entries to the debug device
	 */
	public final void dumpCache() {

		// Dump the count of entries in the cache

		logger.debug("NetworkFileCache entries=" + numberOfEntries());

		// Enumerate the cache entries

		Enumeration keys = m_fileCache.keys();

		while (keys.hasMoreElements()) {

			// Get the current key and lookup the matching value

			Integer key = (Integer) keys.nextElement();
			FileEntry entry = (FileEntry) m_fileCache.get(key);

			// Dump the entry details

			logger.debug("fid=" + key + ": " + entry);
		}
	}
}
