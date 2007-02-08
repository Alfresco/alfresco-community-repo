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