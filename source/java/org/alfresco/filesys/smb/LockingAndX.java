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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.smb;

/**
 * LockingAndX SMB Constants Class
 */
public class LockingAndX
{

    // Lock type flags

    public static final int SharedLock =    0x0001;
    public static final int OplockBreak =   0x0002;
    public static final int ChangeType =    0x0004;
    public static final int Cancel =        0x0008;
    public static final int LargeFiles =    0x0010;

    /**
     * Check if this is a normal lock/unlock, ie. no flags except the LargeFiles flag may be set
     * 
     * @param flags
     * @return boolean
     */
    public final static boolean isNormalLockUnlock(int flags)
    {
        return (flags & 0x000F) == 0 ? true : false;
    }

    /**
     * Check if the large files flag is set
     * 
     * @param flags int
     * @return boolean
     */
    public final static boolean hasLargeFiles(int flags)
    {
        return (flags & LargeFiles) != 0 ? true : false;
    }

    /**
     * Check if the shared lock flag is set
     * 
     * @param flags int
     * @return boolean
     */
    public final static boolean hasSharedLock(int flags)
    {
        return (flags & SharedLock) != 0 ? true : false;
    }

    /**
     * Check if the oplock break flag is set
     * 
     * @param flags int
     * @return boolean
     */
    public final static boolean hasOplockBreak(int flags)
    {
        return (flags & OplockBreak) != 0 ? true : false;
    }

    /**
     * Check if the change type flag is set
     * 
     * @param flags int
     * @return boolean
     */
    public final static boolean hasChangeType(int flags)
    {
        return (flags & ChangeType) != 0 ? true : false;
    }

    /**
     * Check if the cancel flag is set
     * 
     * @param flags int
     * @return boolean
     */
    public final static boolean hasCancel(int flags)
    {
        return (flags & Cancel) != 0 ? true : false;
    }
}
