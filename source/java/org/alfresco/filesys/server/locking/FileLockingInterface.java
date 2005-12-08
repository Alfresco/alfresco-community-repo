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

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.filesys.TreeConnection;

/**
 * File Locking Interface
 * <p>
 * Optional interface that a DiskInterface driver can implement to provide file locking support.
 */
public interface FileLockingInterface
{

    /**
     * Return the lock manager implementation associated with this virtual filesystem
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @return LockManager
     */
    public LockManager getLockManager(SrvSession sess, TreeConnection tree);
}
