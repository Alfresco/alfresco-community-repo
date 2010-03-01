/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.filesys.state;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.alfresco.jlan.debug.Debug;
import org.alfresco.jlan.locking.FileLock;
import org.alfresco.jlan.locking.LockConflictException;
import org.alfresco.jlan.locking.NotLockedException;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.filesys.ExistingOpLockException;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.server.filesys.pseudo.MemoryNetworkFile;
import org.alfresco.jlan.server.locking.LockManager;
import org.alfresco.jlan.server.locking.OpLockDetails;
import org.alfresco.jlan.server.locking.OpLockManager;
import org.alfresco.jlan.smb.OpLock;
import org.alfresco.jlan.smb.SMBStatus;
import org.alfresco.jlan.smb.server.SMBSrvPacket;
import org.alfresco.jlan.smb.server.SMBSrvSession;
import org.alfresco.filesys.alfresco.AlfrescoNetworkFile;

/**
 * File State Lock Manager Class
 * 
 * <p>Implementation of a lock manager that uses the file state cache to track locks on a file.
 * 
 * @author gkspencer
 */
public class FileStateLockManager implements LockManager, OpLockManager, Runnable {

	// Oplock break timeout
	
	private static final long OpLockBreakTimeout	= 5000L;	// 5 seconds
	
	// File state cache used for byte range locks/oplocks
	
	private FileStateTable m_stateCache;
	
	// Oplock breaks in progress
	
	private Hashtable<String, OpLockDetails> m_oplockQueue;
	
	// Oplock break timeout thread
	
	private Thread m_expiryThread;
	private boolean m_shutdown;
	
	/**
	 * Lock a byte range within a file, or the whole file. 
	 *
	 * @param sess SrvSession
	 * @param tree TreeConnection
	 * @param file NetworkFile
	 * @param lock FileLock
	 * @exception LockConflictException
	 * @exception IOException
	 */
	public void lockFile(SrvSession sess, TreeConnection tree, NetworkFile file, FileLock lock)
		throws LockConflictException, IOException {
			
		//	Get the file state associated with the file
		
		FileState fstate = null;
		
		if ( file instanceof AlfrescoNetworkFile) {
		  AlfrescoNetworkFile alfFile = (AlfrescoNetworkFile) file;
		  fstate = alfFile.getFileState();
		}
		else if ( file instanceof MemoryNetworkFile) {
		  file.addLock(lock);
		  return;
		}
		
		if ( fstate == null)
			throw new IOException("Open file without state (lock)");
			
		//	Add the lock to the active lock list for the file, check if the new lock conflicts with
		//	any existing locks. Add the lock to the file instance so that locks can be removed if the
		//	file is closed/session abnormally terminates.
		
		fstate.addLock(lock);
		file.addLock(lock);
	}

	/**
	 * Unlock a byte range within a file, or the whole file
	 *
	 * @param sess SrvSession
	 * @param tree TreeConnection
	 * @param file NetworkFile
	 * @param lock FileLock
	 * @exception NotLockedException
	 * @exception IOException
	 */
	public void unlockFile(SrvSession sess, TreeConnection tree, NetworkFile file, FileLock lock)
		throws NotLockedException, IOException {
			
		//	Get the file state associated with the file
	
		FileState fstate = null;
    
	    if ( file instanceof AlfrescoNetworkFile) {
	      AlfrescoNetworkFile alfFile = (AlfrescoNetworkFile) file;
	      fstate = alfFile.getFileState();
	    }
	    else if ( file instanceof MemoryNetworkFile) {
	      file.removeLock(lock);
	      return;
	    }
	
		if ( fstate == null)
			throw new IOException("Open file without state (unlock)");
		
		//	Remove the lock from the active lock list for the file, and the file instance
	
		fstate.removeLock(lock);
		file.removeLock(lock);
	}
	
	/**
	 * Create a lock object, use the standard FileLock object.
	 * 
	 * @param sess SrvSession
	 * @param tree TreeConnection
	 * @param file NetworkFile
	 * @param offset long
	 * @param len long
	 * @param pid int
	 */
	public FileLock createLockObject(SrvSession sess, TreeConnection tree, NetworkFile file, long offset, long len, int pid) {

		//	Create a lock object to represent the file lock
		
		return new FileLock(offset, len, pid);
	}
	
	/**
	 * Release all locks that a session has on a file. This method is called to perform cleanup if a file
	 * is closed that has active locks or if a session abnormally terminates.
	 *
	 * @param sess SrvSession
	 * @param tree TreeConnection
	 * @param file NetworkFile
	 */
	public void releaseLocksForFile(SrvSession sess, TreeConnection tree, NetworkFile file) {
		
		//	Check if the file has active locks
		
		if ( file.hasLocks())
		{
			
			synchronized ( file)
			{
				
				//	Enumerate the locks and remove
				
				while ( file.numberOfLocks() > 0)
				{
					//	Get the current file lock
					
					FileLock curLock = file.getLockAt(0);
					
					//	Remove the lock, ignore errors
					
					try
					{
						
						//	Unlock will remove the lock from the global list and the local files list
						
						unlockFile(sess, tree, file, curLock);
					}
					catch (Exception ex)
					{
					}
				}
			}
		}
	}
	
	/**
	 * Enable oplock support by setting the file state table
	 * 
	 * @param stateTable FileStateTable
	 */
	public final void setStateTable(FileStateTable stateTable) {
		
		m_stateCache = stateTable;
		
		// Create the oplock break queue
		
		m_oplockQueue = new Hashtable<String, OpLockDetails>();
		
		// Start the oplock break expiry thread
		
        m_expiryThread = new Thread(this);
        m_expiryThread.setDaemon(true);
        m_expiryThread.setName("OpLockExpire");
        m_expiryThread.start();
}
	
	/**
	 * Check if there is an oplock for the specified path, return the oplock type.
	 * 
	 * @param path String
	 * @return int
	 */
	public int hasOpLock(String path) {
		
		// Check if oplocks/state cache are enabled
		
		if ( m_stateCache == null)
			return OpLock.TypeNone;
		
		// Get the file state
		
		FileState fstate = m_stateCache.findFileState(path);
		if ( fstate != null && fstate.hasOpLock()) {
		
			// Return the oplock type
			
			OpLockDetails oplock = fstate.getOpLock();
			if ( oplock != null)
				return oplock.getLockType();
		}
		
		// No oplock
		
		return OpLock.TypeNone;
	}
	
	/**
	 * Return the oplock details for a path, or null if there is no oplock on the path
	 * 
	 * @param path String
	 * @return OpLockDetails
	 */
	public OpLockDetails getOpLockDetails(String path) {
		
		// Check if oplocks/state cache are enabled
		
		if ( m_stateCache == null)
			return null;
		
		// Get the file state
		
		FileState fstate = m_stateCache.findFileState(path);
		if ( fstate != null)
			return fstate.getOpLock();

		// No oplock
		
		return null;
	}
	
	/**
	 * Grant an oplock, store the oplock details
	 * 
	 * @param path String
	 * @param oplock OpLockDetails
	 * @return boolean
	 * @exception ExistingOpLockException	If the file already has an oplock
	 */
	public boolean grantOpLock(String path, OpLockDetails oplock)
		throws ExistingOpLockException {

		// Check if oplocks/state cache are enabled
		
		if ( m_stateCache == null)
			return false;
		
		// Get, or create, a file state
		
		FileState fstate = m_stateCache.findFileState(path, false, true);
		
		// Check if the file is already in use
		
		if ( fstate.getOpenCount() != 1)
			return false;

		// Set the oplock
		
		fstate.setOpLock( oplock);
		return true;
	}
	
	/**
	 * Inform the oplock manager that an oplock break is in progress for the specified file/oplock
	 * 
	 * @param path String
	 * @param oplock OpLockDetails
	 */
	public void informOpLockBreakInProgress(String path, OpLockDetails oplock) {
		
		// Check if oplocks/state cache are enabled
		
		if ( m_stateCache == null)
			return;
		
		// Add the oplock to the break in progress queue
		
		synchronized ( m_oplockQueue) {
			m_oplockQueue.put( path, oplock);
			m_oplockQueue.notify();
		}
	}
	
	/**
	 * Release an oplock
	 * 
	 * @param path String
	 */
	public void releaseOpLock(String path) {
		
		// Check if oplocks/state cache are enabled
		
		if ( m_stateCache == null)
			return;
		
		// Get the file state
		
		FileState fstate = m_stateCache.findFileState(path);
		if ( fstate != null)
			fstate.clearOpLock();
		
		// Remove from the pending oplock break queue
		
		synchronized ( m_oplockQueue) {
			m_oplockQueue.remove( path);
		}
	}
	
	/**
	 * Check for expired oplock break requests
	 * 
	 * @return int
	 */
	public int checkExpiredOplockBreaks() {
		
		// Check if there are ny oplock breaks in progress
		
		if ( m_oplockQueue.size() == 0)
			return 0;
		
		// Check for oplock break requests that have expired

		int expireCnt = 0;
		
		long timeNow = System.currentTimeMillis();
		Enumeration<String> opBreakKeys = m_oplockQueue.keys();
		
		while ( opBreakKeys.hasMoreElements()) {
			
			// Check the current oplock break
			
			String path = opBreakKeys.nextElement();
			OpLockDetails opLock = m_oplockQueue.get( path);
			if ( opLock != null) {
				
				// Check if the oplock break has timed out
				
				if ( opLock.hasDeferredSession() && (opLock.getOplockBreakTime() + OpLockBreakTimeout) <= timeNow) {
					
					// Get the deferred request details
					
					SMBSrvSession sess = opLock.getDeferredSession();
					SMBSrvPacket  pkt  = opLock.getDeferredPacket();
					
					try {
						
						// Return an error for the deferred file open request
						
						if ( sess.sendAsyncErrorResponseSMB( pkt, SMBStatus.NTAccessDenied, SMBStatus.NTErr) == true) {
						
							// DEBUG
							
							if ( Debug.EnableDbg && sess.hasDebug( SMBSrvSession.DBG_OPLOCK))
								sess.debugPrintln( "Oplock break timeout, oplock=" + opLock);
							
							// Release the packet back to the pool
							
							sess.getPacketPool().releasePacket( pkt);
						}
						else if ( Debug.EnableDbg && sess.hasDebug( SMBSrvSession.DBG_OPLOCK))
							sess.debugPrintln( "Failed to send open reject, oplock break timed out, oplock=" + opLock);
					}
					catch ( IOException ex) {
						
					}
					
					// Remove the oplock break from the queue
					
					m_oplockQueue.remove( path);
					
					// Clear the deferred packet details
					
					opLock.clearDeferredSession();
					
					// Mark the oplock has having a failed oplock break
					
					opLock.setOplockBreakFailed();
					
					// Update the expired oplock break count
					
					expireCnt++;
				}
			}
		}
		
		// Return the count of expired oplock breaks
		
		return expireCnt;
	}
	
	/**
	 * Run the oplock break expiry
	 */
    public void run()
    {
        // Loop forever

    	m_shutdown = false;
    	
        while ( m_shutdown == false)
        {
            // Wait for an oplock break or sleep for a while if there are active oplock break requests

        	try
            {
        		synchronized ( m_oplockQueue) {
        			if ( m_oplockQueue.size() == 0)
        				m_oplockQueue.wait();
        		}
        		
        		// Oplock break added to the queue, wait a while before checking the queue
        		
        		if ( m_oplockQueue.size() > 0)
        			Thread.sleep( OpLockBreakTimeout);
            }
            catch (InterruptedException ex)
            {
            }

            //	Check for shutdown
            
            if ( m_shutdown == true)
            	return;
            
            // Check for expired oplock break requests
            
            checkExpiredOplockBreaks();
        }
    }

	/**
	 * Request the oplock break expiry thread to shutdown
	 */
	public final void shutdownRequest() {
		m_shutdown = true;
		
		if ( m_expiryThread != null)
		{
			try {
				m_expiryThread.interrupt();
			}
			catch (Exception ex) {
			}
		}
	}
}
