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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.state;

import java.io.IOException;

import org.alfresco.jlan.locking.FileLock;
import org.alfresco.jlan.locking.LockConflictException;
import org.alfresco.jlan.locking.NotLockedException;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.server.locking.LockManager;
import org.alfresco.filesys.alfresco.AlfrescoNetworkFile;

/**
 * File State Lock Manager Class
 * 
 * <p>Implementation of a lock manager that uses the file state cache to track locks on a file.
 * 
 * @author gkspencer
 */
public class FileStateLockManager implements LockManager {

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
}
