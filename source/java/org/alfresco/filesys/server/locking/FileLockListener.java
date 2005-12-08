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

import org.alfresco.filesys.locking.FileLock;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.filesys.NetworkFile;

/**
 * File Lock Listener Interface.
 * <p>
 * The file lock listener receives events when file locks are granted, released and denied.
 */
public interface FileLockListener
{

    /**
     * Lock has been granted on the specified file.
     * 
     * @param sess SrvSession
     * @param file NetworkFile
     * @param lock FileLock
     */
    void lockGranted(SrvSession sess, NetworkFile file, FileLock lock);

    /**
     * Lock has been released on the specified file.
     * 
     * @param sess SrvSession
     * @param file NetworkFile
     * @param lock FileLock
     */
    void lockReleased(SrvSession sess, NetworkFile file, FileLock lock);

    /**
     * Lock has been denied on the specified file.
     * 
     * @param sess SrvSession
     * @param file NetworkFile
     * @param lock FileLock
     */
    void lockDenied(SrvSession sess, NetworkFile file, FileLock lock);
}