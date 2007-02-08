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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.server.locking;

import java.io.IOException;

import org.alfresco.filesys.locking.FileLock;
import org.alfresco.filesys.locking.LockConflictException;
import org.alfresco.filesys.locking.NotLockedException;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.filesys.TreeConnection;

/**
 * Lock Manager Interface
 * <p>
 * A lock manager implementation provides file locking support for a virtual filesystem.
 */
public interface LockManager
{

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
            throws LockConflictException, IOException;

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
            throws NotLockedException, IOException;

    /**
     * Create a lock object, allows the FileLock object to be extended
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param file NetworkFile
     * @param offset long
     * @param len long
     * @param pid int
     * @return FileLock
     */
    public FileLock createLockObject(SrvSession sess, TreeConnection tree, NetworkFile file, long offset, long len,
            int pid);

    /**
     * Release all locks that a session has on a file. This method is called to perform cleanup if a
     * file is closed that has active locks or if a session abnormally terminates.
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param file NetworkFile
     */
    public void releaseLocksForFile(SrvSession sess, TreeConnection tree, NetworkFile file);
}
