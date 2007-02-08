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
package org.alfresco.filesys.smb;

/**
 * Directory Watcher Interface
 */
public interface DirectoryWatcher
{

    // Notification event types

    public final static int FileActionUnknown = -1;
    public final static int FileNoAction = 0;
    public final static int FileAdded = 1;
    public final static int FileRemoved = 2;
    public final static int FileModified = 3;
    public final static int FileRenamedOld = 4;
    public final static int FileRenamedNew = 5;

    /**
     * Directory change occurred
     * 
     * @param typ int
     * @param fname String
     */
    public void directoryChanged(int typ, String fname);
}
