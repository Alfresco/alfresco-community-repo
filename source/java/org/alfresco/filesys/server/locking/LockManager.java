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
